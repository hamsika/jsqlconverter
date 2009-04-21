package com.googlecode.jsqlconverter.parser.xml;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.logging.LogLevel;

import org.w3c.dom.Element;

import java.io.InputStream;

public class TurbineXMLParser extends XMLParser {
	public TurbineXMLParser(InputStream in) {
		super(in);
	}

	public String getTableListPath() {
		return "/database/table";
	}

	public String getTableNamePath() {
		return "@name";
	}

	public String getColumnListPath(Element tableNode) {
		return "column";
	}

	public String getColumnNamePath() {
		return getTableNamePath();
	}

	public String getPrimaryKeyPath() {
		return "@primaryKey='true'";
	}

	public String getAutoIncrementPath() {
		return "@autoIncrement='true'";
	}

	public String getIsRequiredPath() {
		return "@required='true'";
	}

	public String getColumnSizePath() {
		return "@size";
	}

	public String getDefaultValuePath() {
		return "defaultValue";
	}

	public String getDataTypePath() {
		return "@type";
	}

	public Type getType(String type, int size) {
		if (type.equals("ARRAY")) {
		} else if (type.equals("BIGINT")) {
			return ExactNumericType.BIGINT;
		} else if (type.equals("BINARY")) {
			return BinaryType.BINARY;
		} else if (type.equals("BLOB")) {
			return BinaryType.BLOB;
		} else if (type.equals("BOOLEANCHAR")) {
			return BooleanType.BOOLEAN; // TODO: check this
		} else if (type.equals("BOOLEANINT")) {
			return BooleanType.BOOLEAN; // TODO: check this
		} else if (type.equals("BIT")) {
			return BinaryType.BIT;
		} else if (type.equals("CHAR")) {
			return StringType.CHAR;
		} else if (type.equals("CLOB")) {
			return BinaryType.BLOB; // TODO: check this
		} else if (type.equals("DATE")) {
			return DateTimeType.DATE;
		} else if (type.equals("DECIMAL") || type.equals("NUMERIC")) {
			return new DecimalType(size);
		} else if (type.equals("DISTINCT")) {
		} else if (type.equals("DOUBLE")) {
			return ApproximateNumericType.DOUBLE;
		} else if (type.equals("FLOAT")) {
			return ApproximateNumericType.FLOAT;
		} else if (type.equals("INTEGER")) {
			return ExactNumericType.INTEGER;
		} else if (type.equals("JAVA_OBJECT")) {
		} else if (type.equals("LONGVARBINARY")) {
			return BinaryType.VARBINARY;
		} else if (type.equals("LONGVARCHAR")) {
			return StringType.VARCHAR;
		} else if (type.equals("NULL")) {
		} else if (type.equals("OTHER")) {
		} else if (type.equals("REAL")) {
			return ApproximateNumericType.REAL;
		} else if (type.equals("REF")) {
		} else if (type.equals("SMALLINT")) {
			return ExactNumericType.SMALLINT;
		} else if (type.equals("STRUCT")) {
		} else if (type.equals("TIME")) {
			return DateTimeType.TIME;
		} else if (type.equals("TIMESTAMP")) {
			return DateTimeType.TIMESTAMP; // TODO: check this - should it be DATETIME instead?
		} else if (type.equals("TINYINT")) {
			return ExactNumericType.TINYINT;
		} else if (type.equals("VARBINARY")) {
			return BinaryType.VARBINARY;
		} else if (type.equals("VARCHAR")) {
			return StringType.VARCHAR;
		}

		LOG.log(LogLevel.UNHANDLED, "Unhandled type: " + type);

		return StringType.VARCHAR;
	}
}
