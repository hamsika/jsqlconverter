package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.logging.LogLevel;

import java.io.PrintStream;

public class AccessSQLProducer extends SQLProducer {
	public AccessSQLProducer(PrintStream out) {
		super(out);
	}

	public char getLeftQuote(QuoteType type) {
		return '[';
	}

	public char getRightQuote(QuoteType type) {
		return ']';
	}

	public String getValidIdentifier(String name) {
		return name;
	}

	public String getDefaultConstraintString(DefaultConstraint defaultConstraint) {
		return "DEFAULT [" + defaultConstraint.getValue() + "]";
	}

	public String getType(StringType type) {
		switch(type) {
			case CHAR:
			case LONGTEXT:
			case MEDIUMTEXT:
			case NCHAR:
			case NVARCHAR:
				return "memo";
			case NTEXT:
			case TEXT:
			case TINYTEXT:
			case VARCHAR:
				return "text";
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
				return "binary";
			case BLOB:
			case LONGBLOB:
			case MEDIUMBLOB:
			case TINYBLOB:
				return "ole"; // TODO: check this.. this might be LONGBINARY
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
			case DATE:
				return "date";
			case DATETIME:
				return "datetime";
			case TIME:
			case TIMESTAMP:
				return "datetime";
			default:
				return null;
		}
	}

	public String getType(ExactNumericType type) {
		switch(type) {
			case BIGINT:
			case INTEGER:
			case MEDIUMINT:
				return "long";
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

	public String getType(DecimalType type) {
		return "decimal";
	}

	public boolean outputTypeSize(Type type, String localname) {
		return !localname.equals("memo") && !(type instanceof NumericType) && !(type instanceof BooleanType);
	}

	public boolean isValidIdentifier(String name) {
		// TODO: do some regex here
		return false;
	}

	public boolean supportsIdentifier(IdentifierType type) {
		return false;
	}

	public boolean supportsTableOption(TableOption option) {
		switch(option) {
			case TEMPORARY:
				return true;
			case LOCAL:
			case GLOBAL:
				return false;
			default:
				log.log(LogLevel.UNHANDLED, "Unknown table option: " + option);
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
				log.log(LogLevel.UNHANDLED, "Unknown column option: " + option);
				return false;
		}
	}
}
