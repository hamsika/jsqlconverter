package com.googlecode.jsqlconverter.definition.create.table;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;

import java.util.ArrayList;

public class CreateTable extends Statement {
	private Name tableName;
	private boolean isTemporary = false;
	ArrayList<Column> columns = new ArrayList<Column>();
	private ArrayList<CreateIndex> indexes = new ArrayList<CreateIndex>();

	public CreateTable(Name tableName) {
		this(tableName, false);
	}

	public CreateTable(Name tableName, boolean isTemporary) {
		this.tableName = tableName;
		this.isTemporary = isTemporary;
	}

	public Name getName() {
		return tableName;
	}

	public Column[] getColumns() {
		return columns.toArray(new Column[] {});
	}

	public CreateIndex[] getIndexes() {
		return indexes.toArray(new CreateIndex[] {});
	}

	public Column getColumn(int index) {
		return columns.get(index);
	}

	public int getColumnCount() {
		return columns.size();
	}

	public boolean isTemporary() {
		return isTemporary;
	}

	public void setName(Name tableName) {
		this.tableName = tableName;
	}

	public void setColumn(int index, Column column) {
		columns.set(index, column);
	}

	public void setTemporary(boolean isTemporary) {
		this.isTemporary = isTemporary;
	}

	// add column, delete column
	public void addColumn(Column column) {
		columns.add(column);
	}

	public void removeColumn(Column column) {
		columns.remove(column);
	}

	public void addIndex(CreateIndex index) {
		indexes.add(index);
	}
}
