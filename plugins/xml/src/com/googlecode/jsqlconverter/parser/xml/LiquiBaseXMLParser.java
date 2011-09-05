package com.googlecode.jsqlconverter.parser.xml;

import java.io.BufferedInputStream;

import org.w3c.dom.Element;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.type.BooleanType;
import com.googlecode.jsqlconverter.definition.type.DateTimeType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;

// TODO: support inserts

@ServiceName("LiquiBase")
public class LiquiBaseXMLParser extends XMLParser {
	public LiquiBaseXMLParser(@ParameterName("Input") BufferedInputStream in) {
		super(in);
	}

	public String getTableListPath() {
		return "/databaseChangeLog/changeSet/createTable";
	}

	public String getTableNamePath() {
		return "@tableName";
	}

	public String getColumnListPath(Element tableNode) {
		return "column";
	}

	public String getColumnNamePath() {
		// TODO: varchar(50) / date / etc
		return "@name";
	}

	public String getPrimaryKeyPath() {
		return "constraints/@primaryKey='true'";
	}

	public String getAutoIncrementPath() {
		return "@autoIncrement='true'";
	}

	public String getIsRequiredPath() {
		return "constraints/@nullable='false'";
	}

	public String getColumnSizePath() {
		return null;
	}

	public String getDefaultValuePath() {
		return "@defaultValue";
	}

	public String getDataTypePath(Element columnElement) {
		return "@type";
	}

	public Type getType(String type, int size) {
		if (type.equals("int")) {
			return ExactNumericType.INTEGER;
		} else if (type.equals("boolean")) {
			return BooleanType.BOOLEAN;
		} else if (type.equals("date")) {
			return DateTimeType.DATE;
		}

		return StringType.VARCHAR;
	}

	public boolean columnPathContainsSize() {
		return true;
	}
}
