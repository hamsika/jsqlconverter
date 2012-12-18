package com.googlecode.jsqlconverter.parser.xml;

import java.io.BufferedInputStream;
import java.util.logging.Level;

import org.w3c.dom.Element;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.type.ApproximateNumericType;
import com.googlecode.jsqlconverter.definition.type.BinaryType;
import com.googlecode.jsqlconverter.definition.type.BooleanType;
import com.googlecode.jsqlconverter.definition.type.DateTimeType;
import com.googlecode.jsqlconverter.definition.type.DecimalType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;

@ServiceName("DBDesigner")
public class DBDesignerXMLParser extends XMLParser {
	public DBDesignerXMLParser(@ParameterName("Input") BufferedInputStream in) {
		super(in);
	}

	@Override
	public String getTableListPath() {
		return "/DBMODEL/METADATA/TABLES/TABLE";
	}

	@Override
	public String getTableNamePath() {
		return "@Tablename";
	}

	@Override
	public String getColumnListPath(Element tableNode) {
		return "COLUMNS/COLUMN";
	}

	@Override
	public String getColumnNamePath() {
		return "@ColName";
	}

	@Override
	public String getPrimaryKeyPath() {
		return "@PrimaryKey=1";
	}

	@Override
	public String getAutoIncrementPath() {
		return "@AutoInc=1";
	}

	@Override
	public String getIsRequiredPath() {
		return "@NotNull=1";
	}

	@Override
	public String getColumnSizePath() {
		// TODO: @DatatypeParams  (80) and (8,2)
		return "1";
	}

	@Override
	public String getDefaultValuePath() {
		return "@DefaultValue";
	}

	@Override
	public String getDataTypePath(Element columnElement) {
		return "//DATATYPE[@ID=" + columnElement.getAttribute("idDatatype") + "]/@TypeName";
	}

	@Override
	public boolean columnPathContainsSize() {
		return false;
	}

	@Override
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

		LOG.log(Level.WARNING, "Unhandled type: " + type);

		return StringType.VARCHAR;
	}
}
