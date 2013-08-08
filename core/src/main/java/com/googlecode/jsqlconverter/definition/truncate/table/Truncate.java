package com.googlecode.jsqlconverter.definition.truncate.table;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;

public class Truncate extends Statement {
	private Name tableName;

	public Truncate(Name tableName) {
		this.tableName = tableName;
	}

	public Name getTableName() {
		return tableName;
	}
}
