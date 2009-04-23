package com.googlecode.jsqlconverter.producer.sql;

import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.type.*;

import java.io.PrintStream;

public class OracleSQLProducer extends SQLProducer {
	public OracleSQLProducer(PrintStream out) {
		super(out);
	}

	public char getLeftQuote(QuoteType type) {
		return '"';
	}

	public char getRightQuote(QuoteType type) {
		return '"';
	}

	public String getValidIdentifier(String name) {
		return name;
	}

	public String getDefaultConstraintString(DefaultConstraint defaultConstraint) {
		return "DEFAULT \"" + defaultConstraint.getValue() + "\"";
	}

	public String getType(ApproximateNumericType type) {
		switch (type) {
			case DOUBLE:
				return "double precision";
			case FLOAT:
				return "float";
			case REAL:
				return "real";
			default:
				return null;
		}
	}

	public String getType(BinaryType type) {
		switch(type) {
			case BINARY:
			case VARBINARY:
				return "raw";
			case BLOB:
			case LONGBLOB:
			case MEDIUMBLOB:
			case TINYBLOB:
				return "blob";
			default:
				return null;
		}
	}

	public String getType(BooleanType type) {
		return null;
	}

	public String getType(DateTimeType type) {
		switch(type) {
			case DATE:
			case TIME:
				return "date";
			case DATETIME:
			case TIMESTAMP:
				return "timestamp";
			default:
				return null;
		}
	}

	public String getType(DecimalType type) {
		return null;
	}

	public String getType(ExactNumericType type) {
		switch(type) {
			case BIGINT:
				return "number";
			case INTEGER:
				return "integer";
			case MEDIUMINT:
			case SMALLINT:
			case TINYINT:
			default:
				return null;
		}
	}

	public String getType(MonetaryType type) {
		return null;
	}

	public String getType(StringType type, int size) {
		switch(type) {
			case CHAR:
				return "char";
			case NCHAR:
				return "nchar";
			case LONGTEXT:
			case MEDIUMTEXT:
			case TEXT:
			case NTEXT:
			case TINYTEXT:
				return null;
			case VARCHAR:
				return "varchar2";
			case NVARCHAR:
				return "nvarchar2";
			default:
				return null;
		}
	}

	public boolean outputTypeSize(Type type, String localname) {
		return true;
	}

	public boolean isValidIdentifier(String name) {
		return true;
	}

	public boolean supportsIdentifier(IdentifierType type) {
		return true;
	}

	public boolean supportsTableOption(TableOption option) {
		return true;
	}

	public boolean supportsForeignKeyAction(ForeignKeyAction action) {
		return true;
	}

	public boolean supportsColumnOption(ColumnOption option) {
		return true;
	}
}
