package com.googlecode.jsqlconverter.definition.insert;

import com.googlecode.jsqlconverter.definition.Name;

import java.util.HashMap;

public class InsertFromValues extends Insert {
	private Name[] columns;
	private HashMap<Integer, Object> typeMap = new HashMap<Integer, Object>();
	private Name tableName;

	public InsertFromValues(Name tableName) {
		this(tableName, null);
	}

	public InsertFromValues(Name tableName, Name[] columns) {
		this.tableName = tableName;
		this.columns = columns;
	}

	// setters
	public void setNumeric(int columnIndex, Number value) {
		typeMap.put(columnIndex, value);
	}

	public void setString(int columnIndex, String value) {
		typeMap.put(columnIndex, value);
	}

	// getters
	public Name getTableName() {
		return tableName;
	}

	public Name[] getColumns() {
		return columns;
	}

	public int getColumnCount() {
		return typeMap.size();
	}

	public Number getNumeric(int columnIndex) {
		return (Number)typeMap.get(new Integer(columnIndex));
	}

	public String getString(int columnIndex) {
		return (String)typeMap.get(new Integer(columnIndex));
	}

	public boolean isNumeric(int columnIndex) {
		return typeMap.get(new Integer(columnIndex)) instanceof Number;
	}
}
