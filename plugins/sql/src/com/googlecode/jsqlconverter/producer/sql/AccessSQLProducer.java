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
import com.googlecode.jsqlconverter.definition.type.NumericType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;

@ServiceName("Access")
public class AccessSQLProducer extends SQLProducer {
	public AccessSQLProducer(@ParameterName("Output") PrintStream out) {
		super(out);
	}

	@Override
	public char getLeftQuote(QuoteType type) {
		return '[';
	}

	@Override
	public char getRightQuote(QuoteType type) {
		return ']';
	}

	@Override
	public String getValidIdentifier(String name) {
		return name;
	}

	@Override
	public String getEscapedString(String value) {
		return value;
	}

	@Override
	public String getDefaultConstraintString(DefaultConstraint defaultConstraint) {
		return "DEFAULT [" + defaultConstraint.getValue() + "]";
	}

	@Override
	public String getType(StringType type, int size) {
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

	@Override
	public String getType(ApproximateNumericType type) {
		switch (type) {
			case DOUBLE:
				return "double";
			case FLOAT:
				return "float";
			case REAL:
				return "single";
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
				return "binary";
			case BLOB:
			case LONGBLOB:
			case MEDIUMBLOB:
			case TINYBLOB:
				return "oleobject"; // TODO: check this.. this might be LONGBINARY
			case VARBINARY:
				return "longbinary";
			default:
				return null;
		}
	}

	@Override
	public String getType(BooleanType type) {
		switch(type) {
			case BOOLEAN:
				return "yesno";
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
				return "datetime";
			case TIME:
			case TIMESTAMP:
				return "datetime";
			default:
				return null;
		}
	}

	@Override
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

	@Override
	public String getType(MonetaryType type) {
		switch(type) {
			case MONEY:
			case SMALLMONEY:
				return "currency";
			default:
				return null;
		}
	}

	@Override
	public String getType(DecimalType type) {
		return "numeric";
	}

	@Override
	public boolean isValidIdentifier(String name) {
		// TODO: do some regex here
		return false;
	}

	@Override
	public boolean supportsTypeSize(Type type, String localname) {
		return !localname.equals("memo") && !localname.equals("oleobject") && !localname.equals("longbinary") && !(type instanceof DecimalType) && !(type instanceof NumericType) && !(type instanceof BooleanType);
	}

	@Override
	public boolean supportsIdentifier(IdentifierType type) {
		return false;
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

	@Override
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
				LOG.log(Level.WARNING, "Unhandled column option: " + option);
				return false;
		}
	}
}
