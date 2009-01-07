package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.logging.LogLevel;

import java.io.PrintStream;

public class MySQLProducer extends SQLProducer {
	public MySQLProducer(PrintStream out) {
		super(out);
	}

	public char getLeftQuote() {
		return '`';
	}

	public char getRightQuote() {
		return '`';
	}

	public String getValidName(Name name) {
		StringBuffer nameBuffer = new StringBuffer();

		if (name.getSchemaName() != null) {
			nameBuffer.append(name.getSchemaName());
			nameBuffer.append(".");
		}

		nameBuffer.append(name.getObjectName());

		return nameBuffer.toString();
	}

	public String getDefaultConstraintString(DefaultConstraint defaultConstraint) {
		return "DEFAULT `" + defaultConstraint.getValue() + "`";
	}

	public String getType(ApproximateNumericType type) {
		switch (type) {
			case DOUBLE:
				return "double";
			case FLOAT:
				return "float";
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
				return "blob";
			case VARBINARY:
				return "varbinary";
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
				return "datetime"; // according to some docs TIMESTAMP is eqiv to DATETIME aparantly
			case TIMESTAMP:
				return "timestamp"; // TODO find out if this can have a time zone
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
				return "mediumint";
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
				return "numeric";
			case SMALLMONEY:
				return "numeric";
			default:
				return null;
		}
	}

	public String getType(StringType type) {
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
		return true;
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
			case NO_ACTION:
			case RESTRICT:
			case SET_NULL:
				return true;
			case SET_DEFAULT:
				return false;
			default:
				log.log(LogLevel.UNHANDLED, "Unknown ForeignKeyAction: " + action);
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
				log.log(LogLevel.UNHANDLED, "Unknown column option: " + option);
				return false;
		}
	}
}
