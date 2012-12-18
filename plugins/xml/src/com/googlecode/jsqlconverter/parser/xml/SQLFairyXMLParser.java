package com.googlecode.jsqlconverter.parser.xml;

import java.io.BufferedInputStream;

import org.w3c.dom.Element;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.type.ApproximateNumericType;
import com.googlecode.jsqlconverter.definition.type.BinaryType;
import com.googlecode.jsqlconverter.definition.type.DateTimeType;
import com.googlecode.jsqlconverter.definition.type.DecimalType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;

@ServiceName("SQLFairy")
public class SQLFairyXMLParser extends XMLParser {
	public SQLFairyXMLParser(@ParameterName("Input") BufferedInputStream in) {
		super(in);
	}

	@Override
	public String getTableListPath() {
		return "//table";
	}

	@Override
	public String getTableNamePath() {
		return "@name";
	}

	@Override
	public String getColumnListPath(Element tableNode) {
		return "fields/field";
	}

	@Override
	public String getColumnNamePath() {
		return getTableNamePath();
	}

	@Override
	public String getPrimaryKeyPath() {
		return "@is_primary_key=1";
	}

	@Override
	public String getAutoIncrementPath() {
		return "@is_auto_increment=1";
	}

	@Override
	public String getIsRequiredPath() {
		return "@is_nullable=0";
	}

	@Override
	public String getColumnSizePath() {
		return "@size";
	}

	@Override
	public String getDefaultValuePath() {
		return "defaultValue";
	}

	@Override
	public String getDataTypePath(Element columnElement) {
		return "@data_type";
	}

	@Override
	public boolean columnPathContainsSize() {
		return false;
	}

	@Override
	public Type getType(String type, int size) {
		type = type.toLowerCase();

		if (type.equals("bit")) {
			return BinaryType.BIT;
		} else if (type.equals("tinyint")) {
			return ExactNumericType.TINYINT;
		} else if (type.equals("smallint")) {
			return ExactNumericType.SMALLINT;
		} else if (type.equals("mediumint")) {
			return ExactNumericType.MEDIUMINT;
		} else if (type.equals("int")) {
			return ExactNumericType.INTEGER;
		} else if (type.equals("bigint")) {
			return ExactNumericType.BIGINT;
		} else if (type.equals("float")) {
			return ApproximateNumericType.FLOAT;
		} else if (type.equals("double")) {
			return ApproximateNumericType.DOUBLE;
		} else if (type.equals("decimal") || type.equals("dec") || type.equals("numeric") || type.equals("fixed")) {
			return new DecimalType(size);
		} else if (type.equals("real")) {
			return ApproximateNumericType.REAL;
		} else if (type.equals("tinytext")) {
			return StringType.TINYTEXT;
		} else if (type.equals("tinyblob")) {
			return BinaryType.TINYBLOB;
		} else if (type.equals("blob")) {
			return BinaryType.BLOB;
		} else if (type.equals("text")) {
			return StringType.TEXT;
		} else if (type.equals("mediumblob")) {
			return BinaryType.MEDIUMBLOB;
		} else if (type.equals("mediumtext")) {
			return StringType.MEDIUMTEXT;
		} else if (type.equals("longblob")) {
			return BinaryType.LONGBLOB;
		} else if (type.equals("longtext")) {
			return StringType.LONGTEXT;
		} else if (type.equals("date")) {
			return DateTimeType.DATE;
		} else if (type.equals("datetime")) {
			return DateTimeType.DATETIME;
		} else if (type.equals("timestamp")) {
			return DateTimeType.TIMESTAMP;
		} else if (type.equals("time")) {
			return DateTimeType.TIME;
		} else if (type.equals("char")) {
			return StringType.CHAR;
		} else if (type.equals("varchar")) {
			return StringType.VARCHAR;
		} else if (type.equals("binary")) {
			return BinaryType.BINARY;
		} else if (type.equals("varbinary")) {
			return BinaryType.VARBINARY;
		}

		return StringType.VARCHAR;
	}
}
