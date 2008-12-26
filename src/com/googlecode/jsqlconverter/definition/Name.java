package com.googlecode.jsqlconverter.definition;

public class Name {
	private String databaseName;
	private String schemaName;
	private String objectName;

	public Name(String objectName) {
		this(null, objectName);
	}

	public Name(String schemaName, String objectName) {
		this(null, schemaName, objectName);
	}

	public Name(String databaseName, String schemaName, String objectName) {
		this.databaseName = databaseName;
		this.schemaName = schemaName;
		this.objectName = objectName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getObjectName() {
		return objectName;
	}
}
