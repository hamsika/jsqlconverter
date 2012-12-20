package com.googlecode.jsqlconverter.producer.sql;

import java.io.PrintStream;

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

@ServiceName("Oracle")
public class OracleSQLProducer extends SQLProducer {
	public OracleSQLProducer(@ParameterName("Output") PrintStream out) {
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
		return "DEFAULT \"" + defaultConstraint.getValue() + "\"";
	}

	@Override
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

	@Override
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

	@Override
	public String getType(BooleanType type) {
		return null;
	}

	@Override
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

	@Override
	public String getType(DecimalType type) {
		return "numeric";
	}

	@Override
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

	@Override
	public String getType(MonetaryType type) {
		return null;
	}

	@Override
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

	@Override
	public boolean isValidIdentifier(String name) {
		return true;
	}

	@Override
	public boolean supportsTypeSize(Type type, String localname) {
		return true;
	}

	@Override
	public boolean supportsIdentifier(IdentifierType type) {
		return true;
	}

	@Override
	public boolean supportsTableOption(TableOption option) {
		return true;
	}

	@Override
	public boolean supportsForeignKeyAction(ForeignKeyAction action) {
		return true;
	}

	@Override
	public boolean supportsColumnOption(ColumnOption option) {
		return true;
	}
}
