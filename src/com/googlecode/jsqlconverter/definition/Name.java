package com.googlecode.jsqlconverter.definition;

public class Name {
	private String databaseName;
	private String schemaName;
	private String objectName;
	private String serverName;

	public Name(String objectName) {
		this(null, objectName);
	}

	public Name(String schemaName, String objectName) {
		this(null, schemaName, objectName);
	}

	public Name(String databaseName, String schemaName, String objectName) {
		this(null, databaseName, schemaName, objectName);
	}

	public Name(String serverName, String databaseName, String schemaName, String objectName) {
		this.serverName = serverName;
		this.databaseName = databaseName;
		this.schemaName = schemaName;
		this.objectName = objectName;
	}

	public String getServerName() {
		return serverName;
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
