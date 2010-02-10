package com.googlecode.jsqlconverter.producer.sql;

import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.logging.LogLevel;

import java.io.PrintStream;

public class SQLServerProducer extends SQLProducer {
	public SQLServerProducer(PrintStream out) {
		super(out);
	}

	public char getLeftQuote(QuoteType type) {
		switch(type) {
			case TABLE:
				return '"';
		}

		return '\'';
	}

	public char getRightQuote(QuoteType type) {
		return getLeftQuote(type);
	}

	public String getValidIdentifier(String name) {
		return name;
	}

	public String getEscapedString(String value) {
		return value.replace("'", "''");
	}

	public String getDefaultConstraintString(DefaultConstraint defaultConstraint) {
		// Remove (, ) and ' as for some reason the default that comes from JDBC Driver is often something like: ('100') 
		return "DEFAULT '" + defaultConstraint.getValue().replace("(", "").replace(")", "").replace("'", "") + "'";
	}

	public String getType(ApproximateNumericType type) {
		switch (type) {
			case DOUBLE:
				return "float";
			case FLOAT:
				return "real";
			case REAL:
				return "float";
			default:
				return null;
		}
	}

	public String getType(BinaryType type) {
		switch(type) {
			case BINARY:
				return "binary";
			case BIT:
				return "bit";
			case BLOB:
			case LONGBLOB:
			case MEDIUMBLOB:
			case TINYBLOB:
				return "image";
			case VARBINARY:
				return "varbinary";
			default:
				return null;
		}
	}

	public String getType(BooleanType type) {
		switch(type) {
			case BOOLEAN:
				return "bit"; // TODO: should be bit(1) ?
			default:
				return null;
		}
	}

	public String getType(DateTimeType type) {
		switch(type) {
			case DATE:
			case DATETIME:
				return "datetime"; // according to some docs TIMESTAMP is eqiv to DATETIME
			case TIME:
			case TIMESTAMP:
				return "smalldatetime";
			default:
				return null;
		}
	}

	public String getType(DecimalType type) {
		return "numeric";
	}

	public String getType(ExactNumericType type) {
		switch(type) {
			case BIGINT:
				return "bigint";
			case INTEGER:
				return "integer";
			case MEDIUMINT:
				return "integer";
			case SMALLINT:
				return "smallint";
			case TINYINT:
				return "tinyint";
			default:
				return null;
		}
	}

	public String getType(MonetaryType type) {
		// TODO: check these. best for money = numeric(19, 4)

		switch(type) {
			case MONEY:
				return "money";
			case SMALLMONEY:
				return "smallmoney";
			default:
				return null;
		}
	}

	public String getType(StringType type, int size) {
		switch(type) {
			case CHAR:
				return "char";
			case NCHAR:
				return "nchar";
			case NTEXT:
				return "ntext";
			case NVARCHAR:
				return "nvarchar";
			case TEXT:
			case LONGTEXT:
			case MEDIUMTEXT:
			case TINYTEXT:
				return "text";
			case VARCHAR:
				return "varchar";
			default:
				return null;
		}
	}

	public boolean outputTypeSize(Type type, String localname) {
		return	!(type instanceof NumericType) &&
				!(type instanceof BooleanType) &&
				!(type instanceof DateTimeType) &&
				 (type != BinaryType.BIT);
	}

	public boolean isValidIdentifier(String name) {
		// TODO: do some regex here
		return false;
	}

	public boolean supportsIdentifier(IdentifierType type) {
		return true;
	}

	public boolean supportsTableOption(TableOption option) {
		switch(option) {
			case TEMPORARY:
				return false;
			default:
				LOG.log(LogLevel.UNHANDLED, "Unknown table option: " + option);
				return false;
		}
	}

	public boolean supportsForeignKeyAction(ForeignKeyAction action) {
		switch(action) {
			case CASCADE:
			case NO_ACTION:
			case SET_DEFAULT:
			case SET_NULL:
				return true;
			case RESTRICT:			
				return false;
			default:
				LOG.log(LogLevel.UNHANDLED, "Unknown ForeignKeyAction: " + action);
				return false;
		}
	}

	public boolean supportsColumnOption(ColumnOption option) {
		switch(option) {
			case AUTO_INCREMENT:
			case NOT_NULL:
			case NULL:
			case PRIMARY_KEY:
			case UNIQUE:
				return true;
			default:
				LOG.log(LogLevel.UNHANDLED, "Unknown column option: " + option);
				return false;
		}
	}
}
