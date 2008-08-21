package com.googlecode.jsqlconverter.definition.create.table;

import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;

import java.util.ArrayList;

public class Column {
	// TODO: find out if a single column can have multiple references
	private Name columnName;
	private Type dataType;
	private ArrayList<ColumnOption> options = new ArrayList<ColumnOption>();
	private ForeignKeyConstraint reference = null;
	private int size = 0;

	public Column(Name columnName) {
		this(columnName, null);
	}

	public Column(Name columnName, Type dataType) {
		this.columnName = columnName;
		this.dataType = dataType;
	}

	// getters
	public Name getName() {
		return columnName;
	}

	public Type getType() {
		return dataType;
	}

	public int getSize() {
		return size;
	}

	public ColumnOption[] getOptions() {
		return options.toArray(new ColumnOption[] {});
	}

	public ForeignKeyConstraint getForeignKeyConstraint() {
		return reference;
	}

	// setters
	public void setSize(int size) {
		this.size = size;
	}

	public void setForeignKeyConstraint(ForeignKeyConstraint reference) {
		this.reference = reference;
	}

	// adders
	public void addColumnOption(ColumnOption option) {
		options.add(option);
	}
}
