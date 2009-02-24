package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.logging.LogLevel;

import java.io.InputStream;

public class TurbineXMLParser extends XMLParser {
	// TODO: support compound foreign keys
	public TurbineXMLParser(InputStream in) {
		super(in);
	}

	public String getRootTagName() {
		return "database";
	}

	public String getTableTagName() {
		return "table";
	}

	public String getColumnTagName() {
		return "column";
	}

	public String getDataTypeAttributeName() {
		return "type";
	}

	public String getPrimaryKeyAttributeName() {
		return "primaryKey";
	}

	public String getAutoIncrementAttributeName() {
		return "autoIncrement";
	}

	public String getIsRequiredAttributeName() {
		return "required";
	}

	public String getSizeAttributeName() {
		return "size";
	}

	public boolean supportsForeignKeyTag() {
		return true;
	}

	public boolean supportsConstraintTag() {
		return false;
	}

	public boolean supportsUniqueKeyTag() {
		return true;
	}

	public boolean supportsIndexKeyTag() {
		return true;
	}

	public boolean supportsIndicesKeyTag() {
		return false;
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
