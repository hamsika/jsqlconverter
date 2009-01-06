package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.logging.LogLevel;

import java.io.PrintStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PostgreSQLProducer extends SQLProducer {
	public PostgreSQLProducer(PrintStream out) {
		super(out);
	}

	public char getLeftQuote() {
		return '"';
	}

	public char getRightQuote() {
		return '"';
	}

	public String getValidName(Name name) {
		StringBuffer nameBuffer = new StringBuffer();

		if (name.getDatabaseName() != null) {
			nameBuffer.append(name.getDatabaseName());
			nameBuffer.append(".");
		}

		if (name.getSchemaName() != null) {
			nameBuffer.append(name.getSchemaName());
			nameBuffer.append(".");
		}

		nameBuffer.append(name.getObjectName());

		return nameBuffer.toString();
	}

	public boolean isValidIdentifier(Name name) {
		Pattern pattern = Pattern.compile("^([:alpha:_](\\w|_|\\$){0,62})$");
		Matcher matcher = pattern.matcher(name.getObjectName());

		return matcher.find();
	}

	public String getDefaultConstraintString(DefaultConstraint defaultConstraint) {
		return "DEFAULT '" + defaultConstraint.getValue() + "'";
	}

	public String getType(StringType type) {
		switch(type) {
			case CHAR:
				return "char";
			case LONGTEXT:
			case MEDIUMTEXT:
			case NCHAR:
			case NTEXT:
			case NVARCHAR:
				return null;
			case TEXT:
				return "text";
			case TINYTEXT:
				return null;
			case VARCHAR:
				return "varchar";
			default:
				return null;
		}
	}

	public String getType(ApproximateNumericType type) {
		switch (type) {
			case DOUBLE:
				return "float8";
			case FLOAT:
				return "float4";
			case REAL:
				return "real";
			default:
				return null;
		}
	}

	public String getType(BinaryType type) {
		switch(type) {
			case BINARY:
				return "bytea";
			case BIT:
				return "bit";
			// TODO: confirm 'bytea' is correct to return for blob / binary types
			case BLOB:
			case LONGBLOB:
			case MEDIUMBLOB:
			case TINYBLOB:
			case VARBINARY:
				return "bytea";
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
				return "timestamp"; // according to some docs TIMESTAMP is eqiv to DATETIME aparantly
			case TIMESTAMP:
				return "timestamp"; // TODO find out if this can have a time zone
			default:
				return null;
		}
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
				// double check this is best
				return "smallint";
			default:
				return null;
		}
	}

	public String getType(MonetaryType type) {
		switch(type) {
			case MONEY:
				return null;
			case SMALLMONEY:
				return "money";
			default:
				return null;
		}
	}

	public String getType(DecimalType type) {
		// TODO: precision / scale
		return "numeric";
	}

	public boolean outputTypeSize(Type type, String localname) {
		return true;
	}

	public boolean supportsTableOption(TableOption option) {
		switch(option) {
			case TEMPORARY:
			case LOCAL:
			case GLOBAL:
				return true;
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
			case SET_DEFAULT:
			case SET_NULL:
				return true;
			default:
				log.log(LogLevel.UNHANDLED, "Unknown ForeignKeyAction: " + action);
				return false;
		}
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
