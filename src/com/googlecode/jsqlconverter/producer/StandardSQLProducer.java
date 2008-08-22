package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;

import java.io.PrintStream;

public class StandardSQLProducer implements SQLProducer {
	private PrintStream out = System.out;

	public void produce(Statement[] statements) {
		for (Statement statement : statements) {
			if (statement instanceof CreateTable) {
				handleCreateTable((CreateTable)statement);
			} else if (statement instanceof CreateIndex) {
				handleCreateIndex((CreateIndex)statement);
			} else if (statement instanceof InsertFromValues) {
				handleInsertFromValues((InsertFromValues)statement);
			} else {
				System.out.print("unhandled statement type");
			}
		}
	}

	private void handleInsertFromValues(InsertFromValues insert) {
		out.print("INSERT INTO ");
		out.print(getValidName(insert.getTableName()));
		out.print(" ");
		//out.print(" "); // columns
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

		StringBuffer columnList = new StringBuffer();

		for (Name columnName : createIndex.getColumns()) {
			if (columnList.length() != 0) {
				columnList.append(", ");
			}

			columnList.append(getValidName(columnName));
		}

		out.print(columnList);

		out.println(");");
	}

	// TODO: make sure correct type is being detected (use size / precision to calculate)
	// TODO: support DEFAULT, PRIMARY KEY and UNIQUE
	private void handleCreateTable(CreateTable table) {
		out.print("CREATE ");

		if (table.containsOption(TableOption.TEMPORARY)) {
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
			// TODO: may want to check current data type here too (must be int / etc?)
			if (column.containsOption(ColumnOption.AUTO_INCREMENT) && column.containsOption(ColumnOption.UNIQUE)) {
				out.print(getPrimaryKeyValue());
			} else {
				out.print(getType(column.getType()));

				if (column.getSize() != 0) {
					out.print("(" + column.getSize() + ")");
				}

				for (ColumnOption option : column.getOptions()) {
					out.print(" ");
					out.print(getOptionValue(option));
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

			if (i + 1 != columns.length || table.getUniqueCompoundKeyConstraint().length != 0) {
				out.print(",");
			}

			out.println();
		}

		// indexes
		KeyConstraint[] keys = table.getUniqueCompoundKeyConstraint();

		for (int i=0; i<keys.length; i++) {
			KeyConstraint key = keys[i];

			StringBuffer columnList = new StringBuffer();

			for (Name columnName : key.getColumns()) {
				if (columnList.length() != 0) {
					columnList.append(", ");
				}

				columnList.append(getValidName(columnName));
			}

			out.print("\tUNIQUE (");
			out.print(columnList);
			out.println(")");
		}

		out.println(");");
	}

	private String getPrimaryKeyValue() {
		return "serial NOT NULL";
	}

	private String getDefaultConstraintString(DefaultConstraint defaultConstraint) {
		return "DEFAULT " + defaultConstraint.getValue();
	}

	private String getForeignKeyConstraintString(ForeignKeyConstraint reference) {
		StringBuffer sb = new StringBuffer();

		sb.append("REFERENCES ");
		sb.append(getValidName(reference.getTableName()));
		sb.append(" (");
		sb.append(getValidName(reference.getColumnName()));
		sb.append(")");

		if (reference.getUpdateAction() != ForeignKeyAction.NO_ACTION) {
			sb.append(" ON UPDATE ");

			sb.append(getActionValue(reference.getUpdateAction()));
		}

		if (reference.getDeleteAction() != ForeignKeyAction.NO_ACTION) {
			sb.append(" ON DELETE ");

			sb.append(getActionValue(reference.getDeleteAction()));
		}

		return sb.toString();
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

	private String getOptionValue(ColumnOption option) {
		switch(option) {
			case AUTO_INCREMENT:
				return "SERIAL";
			//case NULL:
			case NOT_NULL:
				return "NOT NULL";
			case UNIQUE:
				return "UNIQUE";
			default:
				System.out.println("Unknown constraint: " + option);
			break;
		}

		return null;
	}

	public String getType(Type type) {
		String dataTypeString = null;

		if (type instanceof ApproximateNumericType) {
			dataTypeString = getType((ApproximateNumericType)type);
		} else if (type instanceof BinaryType) {
			dataTypeString = getType((BinaryType)type);
		} else if (type instanceof BooleanType) {
			dataTypeString = getType((BooleanType)type);
		} else if (type instanceof DateTimeType) {
			dataTypeString = getType((DateTimeType)type);
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

	public String getType(StringType type) {
		switch(type) {
			case CHAR:
				return "CHAR";
			case LONGTEXT:
			case MEDIUMTEXT:
			case NCHAR:
			case NTEXT:
			case NVARCHAR:
				return null;
			case TEXT:
				return "TEXT";
			case TINYTEXT:
				return null;
			case VARCHAR:
				return "VARCHAR";
			default:
				return null;
		}
	}

	public String getType(ApproximateNumericType type) {
		switch (type) {
			case DOUBLE:
				return "FLOAT8";
			case FLOAT:
				return null;
			case REAL:
				return "REAL";
			default:
				return null;
		}
	}

	public String getType(BinaryType type) {
		switch(type) {
			case BINARY:
				return "BYTEA";
			case BIT:
				return "BIT";
			// TODO: confirm 'bytea' is correct to return for blob / binary types
			case BLOB:
			case LONGBLOB:
			case MEDIUMBLOB:
			case TINYBLOB:
			case VARBINARY:
				return "BYTEA";
			default:
				return null;
		}
	}

	public String getType(BooleanType type) {
		switch(type) {
			case BOOLEAN:
				return "boolean";
			default:
				return null;
		}
	}

	public String getType(DateTimeType type) {
		switch(type) {
			case DATETIME:
				return "TIMESTAMP"; // according to some docs TIMESTAMP is eqiv to DATETIME aparantly
			case TIMESTAMP:
				return "TIMESTAMP"; // TODO find out if this can have a time zone
			default:
				return null;
		}
	}

	public String getType(ExactNumericType type) {
		switch(type) {
			case BIGINT:
				return "BIGINT";
			case INTEGER:
				return "INTEGER";
			case MEDIUMINT:
				return null;
			case NUMERIC:
				return "NUMERIC";
			case SMALLINT:
				return "SMALLINT";
			case TINYINT:
				return null;
			default:
				return null;
		}
	}

	public String getType(MonetaryType type) {
		switch(type) {
			case MONEY:
				return null;
			case SMALLMONEY:
				return "MONEY";
			default:
				return null;
		}
	}

	public String getValidName(Name name) {
		if (name.getDatabaseName() != null) {

		}

		if (name.getSchemaName() != null) {

		}

		return name.getObjectName();
	}
}
