package com.googlecode.jsqlconverter.definition.create.table;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;

import java.util.ArrayList;

public class CreateTable extends Statement {
	// set primary key
	// add unique key
	private Name tableName;
	ArrayList<Column> columns = new ArrayList<Column>();
	private ArrayList<TableOption> options = new ArrayList<TableOption>();

	public CreateTable(Name tableName) {
		this.tableName = tableName;
	}

	// getters
	public Name getName() {
		return tableName;
	}

	public Column[] getColumns() {
		return columns.toArray(new Column[] {});
	}

	public Column getColumn(int index) {
		return columns.get(index);
	}

	public int getColumnCount() {
		return columns.size();
	}

	public TableOption[] getTableOptions() {
		return options.toArray(new TableOption[] {});
	}

	public boolean containsOption(TableOption option) {
		return options.contains(option);
	}

	// setters
	public void setName(Name tableName) {
		this.tableName = tableName;
	}

	public void setColumn(int index, Column column) {
		columns.set(index, column);
	}

	// adders
	public void addColumn(Column column) {
		columns.add(column);
	}

	public void addOption(TableOption option) {
		options.add(option);
	}

	// removers
	public void removeColumn(Column column) {
		columns.remove(column);
	}
}
