package com.googlecode.jsqlconverter.producer.sql;

import java.io.PrintStream;
import java.util.logging.Level;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ColumnForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.CompoundForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.truncate.table.Truncate;
import com.googlecode.jsqlconverter.definition.type.ApproximateNumericType;
import com.googlecode.jsqlconverter.definition.type.BinaryType;
import com.googlecode.jsqlconverter.definition.type.BooleanType;
import com.googlecode.jsqlconverter.definition.type.DateTimeType;
import com.googlecode.jsqlconverter.definition.type.DecimalType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.definition.type.MonetaryType;
import com.googlecode.jsqlconverter.definition.type.NumericType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.producer.Producer;
import com.googlecode.jsqlconverter.producer.ProducerException;
import com.googlecode.jsqlconverter.producer.interfaces.CreateIndexInterface;
import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;
import com.googlecode.jsqlconverter.producer.interfaces.InsertFromValuesInterface;
import com.googlecode.jsqlconverter.producer.interfaces.TruncateInterface;

public abstract class SQLProducer extends Producer implements CreateIndexInterface, CreateTableInterface, InsertFromValuesInterface, TruncateInterface {
	public SQLProducer(@ParameterName("Output") PrintStream out) {
		super(out);
	}

	@Override
	public void doCreateIndex(CreateIndex createIndex) throws ProducerException {
		out.print("CREATE ");

		if (createIndex.isUnique()) {
			out.print("UNIQUE ");
		}

		out.print("INDEX ");
		out.print(getQuotedName(createIndex.getIndexName(), QuoteType.INSERT));
		out.print(" ON ");
		out.print(getQuotedName(createIndex.getTableName(), QuoteType.INSERT));
		out.print(" (");
		out.print(getColumnList(createIndex.getColumns(), QuoteType.INDEX));
		out.println(");");
	}

	@Override
	public void doCreateTable(CreateTable table) throws ProducerException {
		out.print("CREATE ");

		if (table.containsOption(TableOption.TEMPORARY) && supportsTableOption(TableOption.TEMPORARY)) {
			out.print("TEMPORARY ");
		}

		out.print("TABLE ");
		out.print(getQuotedName(table.getName(), QuoteType.TABLE));
		out.println(" (");

		Column[] columns = table.getColumns();

		for (int i=0; i<columns.length; i++) {
			Column column = columns[i];

			out.print("\t");
			out.print(getQuotedName(column.getName(), QuoteType.TABLE));
			out.print(" ");

			// constraints
			String typeName = getType(column.getType(), column.getSize());

			out.print(typeName);

			if (column.getSize() != 0 && supportsTypeSize(column.getType(), typeName)) {
				out.print("(" + column.getSize() + ")");
			}

			for (ColumnOption option : column.getOptions()) {
				if (!supportsColumnOption(option)) {
					continue;
				}

				switch(option) {
					case AUTO_INCREMENT:
						out.print(" AUTO_INCREMENT");
					break;
					case NULL:
						out.print(" NULL");
					break;
					case NOT_NULL:
						out.print(" NOT NULL");
					break;
					case UNIQUE:
						out.print(" UNIQUE");
					break;
					case PRIMARY_KEY:
						out.print(" PRIMARY KEY");
					break;
					default:
						LOG.log(Level.WARNING, "Unhandled constraint: " + option);
					break;
				}
			}

			// default
			DefaultConstraint defaultConstraint = column.getDefaultConstraint();

			if (defaultConstraint != null) {
				out.print(" ");
				out.print(getDefaultConstraintString(defaultConstraint));
			}

			// reference
			ColumnForeignKeyConstraint reference = column.getForeignKeyConstraint();

			if (reference != null) {
				out.print(" ");
				out.print(getForeignKeyConstraintString(reference));
			}

			if (i + 1 != columns.length || (table.getPrimaryCompoundKeyConstraint() != null || table.getUniqueCompoundKeyConstraint().length != 0)) {
				out.print(",");
			}

			out.println();
		}

		// combined primary key
		KeyConstraint primaryKey = table.getPrimaryCompoundKeyConstraint();

		if (primaryKey != null) {
			out.print("\tPRIMARY KEY (");
			out.print(getColumnList(primaryKey.getColumns(), QuoteType.TABLE));
			out.println(")");
		}

		// compound indexes
		for (KeyConstraint key : table.getUniqueCompoundKeyConstraint()) {
			out.print("\tUNIQUE (");
			out.print(getColumnList(key.getColumns(), QuoteType.TABLE));
			out.println(")");
		}

		// compound foreign keys
		for (CompoundForeignKeyConstraint reference : table.getCompoundForeignKeyConstraints()) {
			out.print("\tFOREIGN KEY (");
			out.print(getColumnList(reference.getColumnNames(), QuoteType.TABLE));
			out.print(") REFERENCES ");
			out.print(getQuotedName(reference.getTableName(), QuoteType.TABLE));
			out.print(" (");
			out.print(getColumnList(reference.getReferenceColumns(), QuoteType.TABLE));
			out.print(")");

			if (reference.getUpdateAction() != null && supportsForeignKeyAction(reference.getUpdateAction())) {
				out.print(" ON UPDATE ");

				out.print(getActionValue(reference.getUpdateAction()));
			}

			if (reference.getDeleteAction() != null && supportsForeignKeyAction(reference.getDeleteAction())) {
				out.print(" ON DELETE ");

				out.print(getActionValue(reference.getDeleteAction()));
			}
		}

		out.println(");");
	}

	@Override
	public void doInsertFromValues(InsertFromValues insert) throws ProducerException {
		out.print("INSERT INTO ");
		out.print(getQuotedName(insert.getTableName(), QuoteType.TABLE));
		out.print(" ");

		Column[] columns = insert.getColumns();

		if (columns != null) {
			out.print("(");
			out.print(getColumnList(columns));
			out.print(") ");
		}

		out.print("VALUES (");

		for (int i=0; i<insert.getColumnCount(); i++) {
			if (i != 0) {
				out.print(", ");
			}

			Type type = insert.getType(i);

			Object value = insert.getObject(i);

			if (value == null) {
				out.print("null");
				continue;
			}

			if (type instanceof StringType || type instanceof BinaryType) {
				out.print(getLeftQuote(QuoteType.INSERT));
				out.print(getEscapedString((String)value));
				out.print(getRightQuote(QuoteType.INSERT));
			} else if (type instanceof NumericType) {
				out.print(value);
			} else if (type instanceof DateTimeType) {
				out.print(getLeftQuote(QuoteType.DATETIME));
				out.print(value);
				out.print(getRightQuote(QuoteType.DATETIME));
			} else if (type instanceof BooleanType) {
				out.print(value);
			} else {
				LOG.log(Level.WARNING, "Unhandled Datatype: " + type);
				out.print(getLeftQuote(QuoteType.INSERT));
				out.print(value);
				out.print(getRightQuote(QuoteType.INSERT));
			}
		}

		out.println(");");
		}

	@Override
	public void doTruncate(Truncate truncate) throws ProducerException {
		out.print("TRUNCATE TABLE ");
		out.print(getQuotedName(truncate.getTableName(), QuoteType.TRUNCATE));
		out.println(";");
	}

	private String getColumnList(Name[] names, QuoteType type) {
		StringBuffer columnList = new StringBuffer();

		for (Name columnName : names) {
			if (columnList.length() != 0) {
				columnList.append(", ");
			}

			columnList.append(getQuotedName(columnName, type));
		}

		return columnList.toString();
	}

	private String getQuotedName(Name name, QuoteType type) {
		StringBuffer sb = new StringBuffer();

		if (supportsIdentifier(IdentifierType.SCHEMA) && name.getSchemaName() != null) {
			sb.append(getLeftQuote(type));
			sb.append(getValidIdentifier(name.getSchemaName()));
			sb.append(getRightQuote(type));
			sb.append(".");
		}

		if (supportsIdentifier(IdentifierType.DATABASE) && name.getDatabaseName() != null) {
			sb.append(getLeftQuote(type));
			sb.append(getValidIdentifier(name.getDatabaseName()));
			sb.append(getRightQuote(type));
			sb.append(".");
		}

		sb.append(getLeftQuote(type));
		sb.append(getValidIdentifier(name.getObjectName()));
		sb.append(getRightQuote(type));

		return sb.toString();
	}

	private String getColumnList(Column[] columns) {
		StringBuffer columnList = new StringBuffer();

		for (Column column : columns) {
			if (columnList.length() != 0) {
				columnList.append(", ");
			}

			columnList.append(getLeftQuote(QuoteType.TABLE));
			columnList.append(getValidIdentifier(column.getName().getObjectName()));
			columnList.append(getRightQuote(QuoteType.TABLE));
		}

		return columnList.toString();
	}

	private String getForeignKeyConstraintString(ColumnForeignKeyConstraint reference) {
		StringBuffer sb = new StringBuffer();

		sb.append("REFERENCES ");
		sb.append(getQuotedName(reference.getTableName(), QuoteType.TABLE));
		sb.append(" (");
		sb.append(getQuotedName(reference.getColumnName(), QuoteType.TABLE));
		sb.append(")");

		if (reference.getUpdateAction() != null && supportsForeignKeyAction(reference.getUpdateAction())) {
			sb.append(" ON UPDATE ");

			sb.append(getActionValue(reference.getUpdateAction()));
		}

		if (reference.getDeleteAction() != null && supportsForeignKeyAction(reference.getDeleteAction())) {
			sb.append(" ON DELETE ");

			sb.append(getActionValue(reference.getDeleteAction()));
		}

		return sb.toString();
	}

	public String getType(Type type, int size) {
		String dataTypeString = null;

		if (type instanceof ApproximateNumericType) {
			dataTypeString = getType((ApproximateNumericType)type);
		} else if (type instanceof BinaryType) {
			dataTypeString = getType((BinaryType)type);
		} else if (type instanceof BooleanType) {
			dataTypeString = getType((BooleanType)type);
		} else if (type instanceof DateTimeType) {
			dataTypeString = getType((DateTimeType)type);
		} else if (type instanceof DecimalType) {
			dataTypeString = getType((DecimalType)type);
		} else if (type instanceof ExactNumericType) {
			dataTypeString = getType((ExactNumericType)type);
		} else if (type instanceof MonetaryType) {
			dataTypeString = getType((MonetaryType)type);
		} else if (type instanceof StringType) {
			dataTypeString = getType((StringType)type, size);
		}

		if (dataTypeString == null) {
			dataTypeString = type.toString();
		}

		return dataTypeString;
	}

	private String getActionValue(ForeignKeyAction action) {
		switch (action) {
			case CASCADE:
				return "CASCADE";
			case RESTRICT:
				return "RESTRICT";
			case SET_DEFAULT:
				return "SET DEFAULT";
			case SET_NULL:
				return "SET NULL";
			case NO_ACTION:
				return "NO ACTION";
		}

		LOG.warning("Unknown action value: " + action);

		return null;
	}

	// other
	public abstract char getLeftQuote(QuoteType type);
	public abstract char getRightQuote(QuoteType type);

	public abstract String getValidIdentifier(String name);

	public abstract String getEscapedString(String value);

	public abstract String getDefaultConstraintString(DefaultConstraint defaultConstraint);

	public abstract String getType(ApproximateNumericType type);
	public abstract String getType(BinaryType type);
	public abstract String getType(BooleanType type);
	public abstract String getType(DateTimeType type);
	public abstract String getType(DecimalType type);
	public abstract String getType(ExactNumericType type);
	public abstract String getType(MonetaryType type);
	public abstract String getType(StringType type, int size);

	public abstract boolean isValidIdentifier(String name);

	public abstract boolean supportsTypeSize(Type type, String localname);
	public abstract boolean supportsIdentifier(IdentifierType type);
	public abstract boolean supportsTableOption(TableOption option);
	public abstract boolean supportsForeignKeyAction(ForeignKeyAction action);
	public abstract boolean supportsColumnOption(ColumnOption option);

	public enum IdentifierType {
		SCHEMA,
		DATABASE
	}

	public enum QuoteType {
		DATETIME,
		INDEX,
		INSERT,
		TABLE,
		TRUNCATE
	}
}
