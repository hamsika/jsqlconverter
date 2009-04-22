package com.googlecode.jsqlconverter.parser.xml;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.logging.LogLevel;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;

public class WorkbenchXMLParser extends XMLParser {
	public WorkbenchXMLParser(File zipFile, String xmlFileName) throws IOException {
		super(zipFile, xmlFileName);
	}

	public String getTableListPath() {
		return "//value[@struct-name='workbench.physical.TableFigure']";
	}

	public String getTableNamePath() {
		return "value[@key='name']/text()";
	}

	public String getColumnListPath(Element tableNode) {
		// TODO: don't assume this is always the first <link> element 
		String tableId = tableNode.getElementsByTagName("link").item(0).getTextContent();

		return "//value[@struct-name='db.mysql.Column'][link/text() = '" + tableId + "']";
	}

	public String getColumnNamePath() {
		return getTableNamePath();
	}

	public String getPrimaryKeyPath() {
		return "value[@key='isPrimary']/text()=1";
	}

	public String getAutoIncrementPath() {
		return "value[@key='autoIncrement']/text()=1";
	}

	public String getIsRequiredPath() {
		return "value[@key='isNotNull']/text()=1";
	}

	public String getColumnSizePath() {
		return "value[@key='length']/text()";
	}

	public String getDefaultValuePath() {
		return "value[@key='defaultValue']/text()";
	}

	public String getDataTypePath(Element columnElement) {
		return "link[@key='simpleType']";
	}

	public Type getType(String type, int size) {
		// TODO: check these
		if (type.equals("com.mysql.rdbms.mysql.datatype.bigint")) {
			return ExactNumericType.BIGINT;
		} else if (type.equals("com.mysql.rdbms.mysql.datatype.double")) {
			return ApproximateNumericType.DOUBLE;
		} else if (type.equals("com.mysql.rdbms.mysql.datatype.int")) {
			return ExactNumericType.INTEGER;
		} else if (type.equals("com.mysql.rdbms.mysql.datatype.timestamp")) {
			return DateTimeType.TIMESTAMP;
		} else if (type.equals("com.mysql.rdbms.mysql.datatype.tinyint")) {
			return ExactNumericType.TINYINT;
		} else if (type.equals("com.mysql.rdbms.mysql.datatype.varchar")) {
			return StringType.VARCHAR;
		}

		LOG.log(LogLevel.UNHANDLED, "Unhandled type: " + type);

		// TODO: this should really return null
		return StringType.VARCHAR;
	}
}
