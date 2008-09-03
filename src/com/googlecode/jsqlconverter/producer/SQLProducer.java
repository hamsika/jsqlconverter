package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.truncate.table.Truncate;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.logging.LogLevel;

import java.io.PrintStream;

public abstract class SQLProducer extends Producer {
	// TODO: support quoting ([ ], ', ", `, etc). column quoting may be different to value quoting for inserts
	// e.g:  insert into `bob` values('a', 'b');
	private PrintStream out = System.out;

	public void produce(Statement[] statements) {
		for (Statement statement : statements) {
			if (statement instanceof CreateIndex) {
				handleCreateIndex((CreateIndex)statement);
			} else if (statement instanceof CreateTable) {
				handleCreateTable((CreateTable)statement);
			} else if (statement instanceof InsertFromValues) {
				handleInsertFromValues((InsertFromValues)statement);
			} else if (statement instanceof Truncate) {
				handleTruncate((Truncate)statement);
			} else {
				System.out.print("unhandled statement type");
			}
		}
	}

	private void handleCreateIndex(CreateIndex createIndex) {
		out.print("CREATE ");

		if (createIndex.isUnique()) {
			out.print("UNIQUE ");
		}

		out.print("INDEX ");
		out.print(getValidName(createIndex.getIndexName()));
		out.print(" ON ");
		out.print(getValidName(createIndex.getTableName()));
		out.print(" (");
		out.print(getColumnList(createIndex.getColumns()));
		out.println(");");
	}

	private void handleCreateTable(CreateTable table) {
		out.print("CREATE ");

		if (table.containsOption(TableOption.GLOBAL) && supportsTableOption(TableOption.GLOBAL)) {
			out.print("GLOBAL ");
		}

		if (table.containsOption(TableOption.LOCAL) && supportsTableOption(TableOption.LOCAL)) {
			out.print("LOCAL ");
		}

		if (table.containsOption(TableOption.TEMPORARY) && supportsTableOption(TableOption.TEMPORARY)) {
			out.print("TEMPORARY ");
		}

		out.print("TABLE ");
		out.print(getValidName(table.getName()));
		out.println(" (");

		Column[] columns = table.getColumns();

		for (int i=0; i<columns.length; i++) {
			Column column = columns[i];

			out.print("\t");
			out.print(getValidName(column.getName()));
			out.print(" ");

			// constraints
			String typeName = getType(column.getType());

			out.print(typeName);

			if (column.getSize() != 0 && outputTypeSize(column.getType(), typeName)) {
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
						log.log(LogLevel.UNHANDLED, "Unknown constraint: " + option);
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
			ForeignKeyConstraint reference = column.getForeignKeyConstraint();

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
			out.print(getColumnList(primaryKey.getColumns()));
			out.println(")");
		}

		// indexes
		KeyConstraint[] keys = table.getUniqueCompoundKeyConstraint();

		for (int i=0; i<keys.length; i++) {
			KeyConstraint key = keys[i];

			out.print("\tUNIQUE (");
			out.print(getColumnList(key.getColumns()));
			out.println(")");
		}

		out.println(");");
	}

	private void handleInsertFromValues(InsertFromValues insert) {
		out.print("INSERT INTO ");
		out.print(getValidName(insert.getTableName()));
		out.print(" ");

		Name[] columns = insert.getColumns();

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

			if (insert.isNumeric(i)) {
				out.print(insert.getNumeric(i));
			} else {
				out.print(insert.getString(i));
			}
		}

		out.println(");");
	}

	private void handleTruncate(Truncate truncate) {
		out.print("TRUNCATE TABLE ");
		out.println(getValidName(truncate.getTableName()));
		out.println(";");
	}

	private String getColumnList(Name[] names) {
		StringBuffer columnList = new StringBuffer();

		for (Name columnName : names) {
			if (columnList.length() != 0) {
				columnList.append(", ");
			}

			columnList.append(getValidName(columnName));
		}

		return columnList.toString();
	}

	private String getForeignKeyConstraintString(ForeignKeyConstraint reference) {
		StringBuffer sb = new StringBuffer();

		sb.append("REFERENCES ");
		sb.append(getValidName(reference.getTableName()));
		sb.append(" (");
		sb.append(getValidName(reference.getColumnName()));
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

	private String getType(Type type) {
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
			dataTypeString = getType((StringType)type);
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
		}

		return null;
	}

	// other
	public abstract String getValidName(Name name);

	public abstract String getDefaultConstraintString(DefaultConstraint defaultConstraint);

	public abstract String getType(ApproximateNumericType type);
	public abstract String getType(BinaryType type);
	public abstract String getType(BooleanType type);
	public abstract String getType(DateTimeType type);
	public abstract String getType(DecimalType type);
	public abstract String getType(ExactNumericType type);
	public abstract String getType(MonetaryType type);
	public abstract String getType(StringType type);

	public abstract boolean outputTypeSize(Type type, String localname);

	public abstract boolean supportsTableOption(TableOption option);
	public abstract boolean supportsForeignKeyAction(ForeignKeyAction action);
	public abstract boolean supportsColumnOption(ColumnOption option);
}
