package com.googlecode.jsqlconverter.definition.create.table;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;

import java.util.ArrayList;

public class CreateTable extends Statement {
	private Name tableName;
	private ArrayList<Column> columns = new ArrayList<Column>();
	private ArrayList<TableOption> options = new ArrayList<TableOption>();
	private ArrayList<KeyConstraint> uniqueKeys = new ArrayList<KeyConstraint>();
	private KeyConstraint primaryKey = null;

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

	public KeyConstraint[] getUniqueCompoundKeyConstraint() {
		return uniqueKeys.toArray(new KeyConstraint[] {});
	}

	public KeyConstraint getPrimaryCompoundKeyConstraint() {
		return primaryKey;
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

	public void setPrimaryCompoundKeyConstraint(KeyConstraint keyConstraint) {
		primaryKey = keyConstraint;
	}

	// adders
	public void addColumn(Column column) {
		columns.add(column);
	}

	public void addOption(TableOption option) {
		options.add(option);
	}

	public void addUniqueCompoundKeyConstraint(KeyConstraint keyConstraint) {
		uniqueKeys.add(keyConstraint);
	}

	// removers
	public void removeColumn(Column column) {
		columns.remove(column);
	}
}
