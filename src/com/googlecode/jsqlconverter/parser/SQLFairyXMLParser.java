package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.definition.type.StringType;

import java.io.InputStream;

// TODO: support foreign keys

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
		// TODO: complete this
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
