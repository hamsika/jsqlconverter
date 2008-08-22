package com.googlecode.jsqlconverter.definition.create.table.constraint;

import com.googlecode.jsqlconverter.definition.Name;

public class KeyConstraint {
	private Name[] columns;

	public KeyConstraint(Name[] columns) {
		this.columns = columns;
	}

	public Name[] getColumns() {
		return columns;
	}
}
