package com.googlecode.jsqlconverter.definition.create.index;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;

import java.util.ArrayList;

public class CreateIndex extends Statement {
	private Name indexName;
	private boolean isUnique;
	private ArrayList<Name> columns = new ArrayList<Name>();

	public CreateIndex(Name indexName) {
		this.indexName = indexName;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public void addColumn(Name columnName) {
		columns.add(columnName);
	}

	public boolean isUnique() {
		return isUnique;
	}

	public Name getName() {
		return indexName;
	}

	public Name[] getColumns() {
		return columns.toArray(new Name[] {});
	}
}
