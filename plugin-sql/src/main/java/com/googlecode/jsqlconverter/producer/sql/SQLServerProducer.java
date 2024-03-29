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

@ServiceName("SQLServer")
public class SQLServerProducer extends SQLProducer {
	public SQLServerProducer(@ParameterName("Output") PrintStream out) {
		super(out);
	}

	@Override
	public char getLeftQuote(QuoteType type) {
		switch(type) {
			case TABLE:
				return '[';
		}

		return '\'';
	}

	@Override
	public char getRightQuote(QuoteType type) {
		switch(type) {
			case TABLE:
				return ']';
		}

		return '\'';
	}

	@Override
	public String getValidIdentifier(String name) {
		return name;
	}

	@Override
	public String getEscapedString(String value) {
		return value.replace("'", "''");
	}

	@Override
	public String getDefaultConstraintString(DefaultConstraint defaultConstraint) {
		// Remove (, ) and ' as for some reason the default that comes from JDBC Driver is often something like: ('100')
		return "DEFAULT '" + defaultConstraint.getValue().replace("(", "").replace(")", "").replace("'", "") + "'";
	}

	@Override
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

	@Override
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

	@Override
	public String getType(BooleanType type) {
		switch(type) {
			case BOOLEAN:
				return "bit"; // TODO: should be bit(1) ?
			default:
				return null;
		}
	}

	@Override
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
				return "integer";
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
				return "money";
			case SMALLMONEY:
				return "smallmoney";
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

	@Override
	public boolean isValidIdentifier(String name) {
		// TODO: do some regex here
		return false;
	}

	@Override
	public boolean supportsTypeSize(Type type, String localname) {
		return	!(type instanceof NumericType) &&
				!(type instanceof BooleanType) &&
				!(type instanceof DateTimeType) &&
				 (type != BinaryType.BIT);
	}

	@Override
	public boolean supportsIdentifier(IdentifierType type) {
		return true;
	}

	@Override
	public boolean supportsTableOption(TableOption option) {
		switch(option) {
			case TEMPORARY:
				return false;
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
			case SET_DEFAULT:
			case SET_NULL:
				return true;
			case RESTRICT:
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
