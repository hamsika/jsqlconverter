package com.googlecode.jsqlconverter.definition.insert;

import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.definition.type.StringType;

import java.util.HashMap;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class InsertFromValues extends Insert {
	private HashMap<Integer, Object> values = new HashMap<Integer, Object>();
	private Column[] columns;
	private Name tableName;

	public InsertFromValues(Name tableName) {
		this(tableName, null);
	}

	public InsertFromValues(Name tableName, Column[] columns) {
		this.tableName = tableName;
		this.columns = columns;
	}

	// setters
	/*public void setString(int columnIndex, String value) {
		values.set(columnIndex, value);
	}

	public void setBoolean(int columnIndex, boolean value) {
		values.set(columnIndex, value);
	}

	public void setInt(int columnIndex, int value) {
		values.set(columnIndex, value);
	}

	public void setLong(int columnIndex, long value) {
		values.set(columnIndex, value);
	}

	public void setFloat(int columnIndex, float value) {
		values.set(columnIndex, value);
	}

	public void setDouble(int columnIndex, double value) {
		values.set(columnIndex, value);
	}

	public void setDate(int columnIndex, Date value) {
		values.set(columnIndex, value);
	}

	public void setTime(int columnIndex, Time value) {
		values.set(columnIndex, value);
	}

	public void setTimestamp(int columnIndex, Timestamp value) {
		values.set(columnIndex, value);
	}
	*/

	public void setValue(int columnIndex, Object value) {
		if (columns != null) {
			if (columnIndex > values.size()) {
				// TODO: handle this error. Cannot set value if the column does not exist
			}
		}

		//values.set(columnIndex, value);
		values.put(columnIndex, value);
	}

	// getters
	public Name getTableName() {
		return tableName;
	}

	public Column[] getColumns() {
		return columns;
	}

	public Type getType(int columnIndex) {
		if (columns == null) {
			return StringType.VARCHAR;
		}

		return columns[columnIndex].getType();
	}

	public int getColumnCount() {
		if (columns != null) {
			return columns.length;
		}

		return values.size();
	}

	// type getters
	public String getString(int columnIndex) {
		return (String) values.get(columnIndex);
	}

	public boolean getBoolean(int columnIndex) {
		return (Boolean) values.get(columnIndex);
	}

	public int getInt(int columnIndex) {
		return (Integer) values.get(columnIndex);
	}

	public long getLong(int columnIndex) {
		return (Long) values.get(columnIndex);
	}

	public float getFloat(int columnIndex) {
		return (Float) values.get(columnIndex);
	}

	public double getDouble(int columnIndex) {
		return (Double) values.get(columnIndex);
	}

	public Date getDate(int columnIndex) {
		return (Date) values.get(columnIndex);
	}

	public Time getTime(int columnIndex) {
		return (Time) values.get(columnIndex);
	}

	public Timestamp getTimestamp(int columnIndex) {
		return (Timestamp) values.get(columnIndex);
	}

	public Object getObject(int columnIndex) {
		return values.get(columnIndex);
	}
}
