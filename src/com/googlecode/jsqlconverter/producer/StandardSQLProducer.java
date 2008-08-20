package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.Reference;

public class StandardSQLProducer implements SQLProducer {
	public void produce(Statement[] statements) {
		for (Statement statement : statements) {
			if (statement instanceof CreateTable) {
				//System.out.println("this is pg sql prod :) create table");
				handleCreateTable((CreateTable)statement);
			} else {
				System.out.print("unhandled statement type");
			}
		}
	}

	private void handleCreateTable(CreateTable table) {
		System.out.print("CREATE ");

		if (table.isTemporary()) {
			System.out.print("TEMPORARY ");
		}

		System.out.print("TABLE ");
		System.out.print(getValidName(table.getName()));
		System.out.println(" (");

		for (Column column : table.getColumns()) {
			System.out.print("\t");
			System.out.print(getValidName(column.getName()));
			System.out.print(" ");
			System.out.print(getType(column.getType()));

			if (column.getSize() != 0) {
				System.out.print("(" + column.getSize() + ")");
			}

			Reference[] references = column.getReferences();

			for (Reference reference : references) {
				System.out.print(" REFERENCES " + getValidName(reference.getTableName()) + " (" + getValidName(reference.getColumnName()) + ")");

				if (reference.getUpdateAction() != Reference.Action.NO_ACTION) {
					System.out.print(" ON UPDATE ");

					switch (reference.getUpdateAction()) {
						case CASCADE:
							System.out.print("CASCADE");
						break;
						case RESTRICT:
							System.out.print("RESTRICT");
						break;
						case SET_DEFAULT:
							System.out.print("SET DEFAULT");
						break;
						case SET_NULL:
							System.out.print("SET NULL");
						break;
					}
				}

				if (reference.getDeleteAction() != Reference.Action.NO_ACTION) {
					System.out.print(" ON DELETE ");

					switch (reference.getDeleteAction()) {
						case CASCADE:
							System.out.print("CASCADE");
						break;
						case RESTRICT:
							System.out.print("RESTRICT");
						break;
						case SET_DEFAULT:
							System.out.print("SET DEFAULT");
						break;
						case SET_NULL:
							System.out.print("SET NULL");
						break;
					}
				}
			}

			System.out.println(",");
		}

		System.out.println(");");
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
