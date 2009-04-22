package com.googlecode.jsqlconverter.parser.xml;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.logging.LogLevel;
import org.w3c.dom.Element;

import java.io.InputStream;

public class DBModelXMLParser extends XMLParser {
	public DBModelXMLParser(InputStream in) {
		super(in);
	}

	public String getTableListPath() {
		return "/DBMODEL/METADATA/TABLES/TABLE";
	}

	public String getTableNamePath() {
		return "@Tablename";
	}

	public String getColumnListPath(Element tableNode) {
		return "COLUMNS/COLUMN";
	}

	public String getColumnNamePath() {
		return "@ColName";
	}

	public String getPrimaryKeyPath() {
		return "@PrimaryKey=1";
	}

	public String getAutoIncrementPath() {
		return "@AutoInc=1";
	}

	public String getIsRequiredPath() {
		return "@NotNull=1";
	}

	public String getColumnSizePath() {
		// TODO: @DatatypeParams  (80) and (8,2)
		return "1";
	}

	public String getDefaultValuePath() {
		return "@DefaultValue";
	}

	public String getDataTypePath(Element columnElement) {
		return "//DATATYPE[@ID=" + columnElement.getAttribute("idDatatype") + "]/@TypeName";
	}

	public Type getType(String type, int size) {
		if (type.equals("BIT")) {
			return BinaryType.BIT;
		} else if (type.equals("BOOL")) {
			return BooleanType.BOOLEAN;
		} else if (type.equals("BLOB")) {
			return BinaryType.BLOB;
		} else if (type.equals("CHAR")) {
			return StringType.CHAR;
		} else if (type.equals("DATE")) {
			return DateTimeType.DATE;
		} else if (type.equals("DATETIME")) {
			return DateTimeType.DATETIME;
		} else if (type.equals("DECIMAL")) {
			return new DecimalType(size);
		} else if (type.equals("DOUBLE")) {
			return ApproximateNumericType.DOUBLE;
		} else if (type.equals("ENUM")) {
			return StringType.VARCHAR;
		} else if (type.equals("FLOAT")) {
			return ApproximateNumericType.FLOAT;
		} else if (type.equals("INTEGER")) {
			return ExactNumericType.INTEGER;
		} else if (type.equals("LONGBLOB")) {
			return BinaryType.LONGBLOB;
		} else if (type.equals("LONGTEXT")) {
			return StringType.LONGTEXT;
		} else if (type.equals("MEDIUMBLOB")) {
			return BinaryType.MEDIUMBLOB;
		} else if (type.equals("MEDIUMTEXT")) {
			return StringType.MEDIUMTEXT;
		} else if (type.equals("TEXT")) {
			return StringType.TEXT;
		} else if (type.equals("TIME")) {
			return DateTimeType.TIME;
		} else if (type.equals("TIMESTAMP")) {
			return DateTimeType.TIMESTAMP;
		} else if (type.equals("TINYBLOB")) {
			return BinaryType.TINYBLOB;
		} else if (type.equals("TINYTEXT")) {
			return StringType.TINYTEXT;
		} else if (type.equals("VARCHAR")) {
			return StringType.VARCHAR;
		} else if (type.equals("YEAR")) {
			return DateTimeType.DATE;
		}

		LOG.log(LogLevel.UNHANDLED, "Unhandled type: " + type);

		return StringType.VARCHAR;
	}
}
