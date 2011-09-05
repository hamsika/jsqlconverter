package com.googlecode.jsqlconverter.parser.xml;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.w3c.dom.Element;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.type.ApproximateNumericType;
import com.googlecode.jsqlconverter.definition.type.DateTimeType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;

@ServiceName("Workbench")
public class WorkbenchXMLParser extends XMLParser {
	public WorkbenchXMLParser(@ParameterName("File") File zipFile) throws IOException {
		super(zipFile, "document.mwb.xml");
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

	public boolean columnPathContainsSize() {
		return false;
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

		LOG.log(Level.WARNING, "Unhandled type: " + type);

		// TODO: this should really return null
		return StringType.VARCHAR;
	}
}
