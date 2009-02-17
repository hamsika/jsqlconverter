package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.type.*;

import java.io.InputStream;

// TODO: support foreign keys
// TODO: support compound foreign keys
public class SQLFairyXMLParser extends XMLParser {
	private String tagPrefix = null;

	public SQLFairyXMLParser(InputStream in) {
		super(in);
	}

	public SQLFairyXMLParser(InputStream in, String tagPrefix) {
		super(in);

		this.tagPrefix = tagPrefix;
	}

	public String getRootTagName() {
		return getPrefixedTagName("schema");
	}

	public String getTableTagName() {
		return getPrefixedTagName("table");
	}

	public String getColumnTagName() {
		return getPrefixedTagName("field");
	}

	public String getDataTypeAttributeName() {
		return "data_type";
	}

	public String getPrimaryKeyAttributeName() {
		return "is_primary_key";
	}

	public String getAutoIncrementAttributeName() {
		return "is_auto_increment";
	}

	public String getIsRequiredAttributeName() {
		return "is_nullable";
	}

	public String getSizeAttributeName() {
		return "size";
	}

	public boolean supportsForeignKeyTag() {
		return false;
	}

	public boolean supportsConstraintTag() {
		return true;
	}

	public boolean supportsUniqueKeyTag() {
		return false;
	}

	public boolean supportsIndexKeyTag() {
		return false;
	}

	public boolean supportsIndicesKeyTag() {
		return true;
	}

	public Type getType(String type, int size) {
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

	private String getPrefixedTagName(String name) {
		if (tagPrefix == null) {
			return name;
		} else {
			return tagPrefix + ":" + name;
		}
	}
}
