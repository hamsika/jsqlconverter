package com.googlecode.jsqlconverter.producer.sql;

import java.io.PrintStream;
import java.util.logging.Level;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.type.ApproximateNumericType;
import com.googlecode.jsqlconverter.definition.type.BinaryType;
import com.googlecode.jsqlconverter.definition.type.BooleanType;
import com.googlecode.jsqlconverter.definition.type.DateTimeType;
import com.googlecode.jsqlconverter.definition.type.DecimalType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.definition.type.MonetaryType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;

@ServiceName("MySQL")
public class MySQLProducer extends SQLProducer {
	public MySQLProducer(@ParameterName("Output") PrintStream out) {
		super(out);
	}

	@Override
	public char getLeftQuote(QuoteType type) {
		switch(type) {
			case TABLE:
				return '`';
			default:
				return '\'';
		}
	}

	@Override
	public char getRightQuote(QuoteType type) {
		return getLeftQuote(type);
	}

	@Override
	public String getEscapedString(String value) {
		return value.replace("'", "''");
	}

	@Override
	public String getValidIdentifier(String name) {
		return name;
	}

	@Override
	public String getDefaultConstraintString(DefaultConstraint defaultConstraint) {
		return "DEFAULT '" + defaultConstraint.getValue() + "'";
	}

	@Override
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

	@Override
	public String getType(BinaryType type) {
		switch(type) {
			case BINARY:
				return "binary";
			case BIT:
				return "bit";
			case BLOB:
				return "blob";
			case LONGBLOB:
				return "longblob";
			case MEDIUMBLOB:
				return "mediumblob";
			case TINYBLOB:
				return "tinyblob";
			case VARBINARY:
				return "varbinary";
			default:
				return null;
		}
	}

	@Override
	public String getType(BooleanType type) {
		switch(type) {
			case BOOLEAN:
				return "boolean";
			default:
				return null;
		}
	}

	@Override
	public String getType(DateTimeType type) {
		switch(type) {
			case DATE:
				return "date";
			case DATETIME:
				return "datetime"; // according to some docs TIMESTAMP is eqiv to DATETIME aparantly
			case TIME:
				return "time";
			case TIMESTAMP:
				return "timestamp"; // TODO find out if this can have a time zone
			default:
				return null;
		}
	}

	@Override
	public String getType(DecimalType type) {
		return "numeric";
	}

	@Override
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

	@Override
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

	@Override
	public String getType(StringType type, int size) {
		switch(type) {
			case CHAR:
				return "char";
			case NCHAR:
				return "nchar";
			case NTEXT:
				return "text";
			case NVARCHAR:
				return "nvarchar";
			case TEXT:
				return "text";
			case LONGTEXT:
				return (size > 0) ? "text" : "longtext";
			case MEDIUMTEXT:
				return (size > 0) ? "text" : "mediumtext";
			case TINYTEXT:
				return (size > 0) ? "text" : "tinytext";
			case VARCHAR:
				return "varchar";
			default:
				return null;
		}
	}

	@Override
	public boolean isValidIdentifier(String name) {
		// TODO: do some regex here
		return false;
	}

	@Override
	public boolean supportsTypeSize(Type type, String localname) {
		// TODO: support DOUBLE. certain times it should be ok.
		return !(type instanceof DateTimeType) &&
			   !(type instanceof BooleanType) &&
			   !(type instanceof BinaryType && type != BinaryType.VARBINARY) &&
			   type != ApproximateNumericType.DOUBLE;
	}

	@Override
	public boolean supportsIdentifier(IdentifierType type) {
		switch (type) {
			case SCHEMA:
				return false;
			case DATABASE:
				return true;
			default:
				LOG.log(Level.WARNING, "Unhandled identifier type: " + type);
				return false;
		}
	}

	@Override
	public boolean supportsTableOption(TableOption option) {
		switch(option) {
			case TEMPORARY:
				return true;
			default:
				LOG.log(Level.WARNING, "Unhandled table option: " + option);
				return false;
		}
	}

	@Override
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
				LOG.log(Level.WARNING, "Unhandled ForeignKeyAction: " + action);
				return false;
		}
	}

	@Override
	public boolean supportsColumnOption(ColumnOption option) {
		switch(option) {
			case AUTO_INCREMENT:
			case NOT_NULL:
			case NULL:
			case PRIMARY_KEY:
			case UNIQUE:
				return true;
			default:
				LOG.log(Level.WARNING, "Unhandled column option: " + option);
				return false;
		}
	}
}
