package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.type.*;

public class AccessSQLProducer extends SQLProducer {
	public String getDefaultConstraintString(DefaultConstraint defaultConstraint) {
		return "DEFAULT " + defaultConstraint.getValue();
	}

	public String getValidName(Name name) {
		return name.getObjectName();
	}

	public String getType(StringType type) {
		switch(type) {
			case CHAR:
			case LONGTEXT:
			case MEDIUMTEXT:
			case NCHAR:
			case NTEXT:
				return null;
			case NVARCHAR:
				return "memo";
			case TEXT:
			case TINYTEXT:
				return null;
			case VARCHAR:
				return "memo";
			default:
				return null;
		}
	}

	public String getType(ApproximateNumericType type) {
		switch (type) {
			case DOUBLE:
				return "decimal";
			case FLOAT:
				return "float";
			case REAL:
				return "single";
			default:
				return null;
		}
	}

	public String getType(BinaryType type) {
		switch(type) {
			case BINARY:
				return "binary";
			case BIT:
			case BLOB:
			case LONGBLOB:
			case MEDIUMBLOB:
			case TINYBLOB:
				return null;
			case VARBINARY:
				return "longbinary";
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
				return "date";
			case TIMESTAMP:
			default:
				return null;
		}
	}

	public String getType(ExactNumericType type) {
		switch(type) {
			case BIGINT:
			case INTEGER:
				return "long";
			case MEDIUMINT:
			case NUMERIC:
				return null;
			case SMALLINT:
				return "integer";
			case TINYINT:
				return "byte";
			default:
				return null;
		}
	}

	public String getType(MonetaryType type) {
		switch(type) {
			case MONEY:
			case SMALLMONEY:
				return "currency";
			default:
				return null;
		}
	}

	public boolean outputTypeSize(Type type) {
		return false;
	}

	public boolean supportsTableOption(TableOption option) {
		switch(option) {
			case TEMPORARY:
				return true;
			case IF_NOT_EXISTS:
			case LOCAL:
			case GLOBAL:
				return false;
			default:
				System.out.println("Unknown table option: " + option);
				return false;
		}
	}

	public boolean supportsForeignKeyAction(ForeignKeyAction action) {
		switch(action) {
			case CASCADE:

			break;
			case RESTRICT:

			break;
			case SET_DEFAULT:

			break;
			case SET_NULL:

			break;
		}

		return false;
	}

	public boolean supportsColumnOption(ColumnOption option) {
		switch(option) {
			case AUTO_INCREMENT:
				return false;
			case NOT_NULL:
			case NULL:
			case PRIMARY_KEY:
			case UNIQUE:
				return true;
			default:
				System.out.println("Unknown column option: " + option);
				return false;
		}
	}
}
