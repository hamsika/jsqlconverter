package com.googlecode.jsqlconverter.producer.sql;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

@ServiceName("PostgreSQL")
public class PostgreSQLProducer extends SQLProducer {
	public PostgreSQLProducer(@ParameterName("Output") PrintStream out) {
		super(out);
	}

	@Override
	public char getLeftQuote(QuoteType type) {
		return '"';
	}

	@Override
	public char getRightQuote(QuoteType type) {
		return '"';
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
		return "DEFAULT '" + defaultConstraint.getValue() + "'";
	}

	@Override
	public String getType(ApproximateNumericType type) {
		switch (type) {
			case DOUBLE:
				return "double precision";
			case FLOAT:
			case REAL:
				return "real";
			default:
				return null;
		}
	}

	@Override
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
			case TIME:
				return "time";
			case DATETIME:
			case TIMESTAMP:
				return "timestamp"; // TODO find out if this can have a time zone
			default:
				return null;
		}
	}

	@Override
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

	@Override
	public String getType(MonetaryType type) {
		switch(type) {
			case MONEY:
				return "money"; // this might not be the best choice.
			case SMALLMONEY:
				return "money";
			default:
				return null;
		}
	}

	@Override
	public String getType(StringType type, int size) {
		switch(type) {
			case CHAR:
			case NCHAR:
				return "char";
			case LONGTEXT:
			case MEDIUMTEXT:
			case TEXT:
			case NTEXT:
				return "text";
			case TINYTEXT:
			case VARCHAR:
			case NVARCHAR:
				return "varchar";
			default:
				return null;
		}
	}

	@Override
	public String getType(DecimalType type) {
		// TODO: precision / scale
		return "numeric";
	}

	@Override
	public boolean outputTypeSize(Type type, String localname) {
		return !(type instanceof NumericType) &&
			!(type instanceof BooleanType) &&
			!(type instanceof DateTimeType) &&
			!(type instanceof MonetaryType) &&
			!localname.equals("bytea") &&
			!localname.equals("text");
	}

	@Override
	public boolean isValidIdentifier(String name) {
		Pattern pattern = Pattern.compile("^([:alpha:_](\\w|_|\\$){0,62})$");
		Matcher matcher = pattern.matcher(name);

		return matcher.find();
	}

	@Override
	public boolean supportsIdentifier(IdentifierType type) {
		return true;
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
			case SET_DEFAULT:
			case SET_NULL:
				return true;
			default:
				LOG.log(Level.WARNING, "Unhandled ForeignKeyAction: " + action);
				return false;
		}
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
