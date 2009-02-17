package com.googlecode.jsqlconverter.definition.create.table.constraint;

import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.DefinitionException;

public class KeyConstraint {
	private Name[] columns;

	public KeyConstraint(Name[] columns) {
		this.columns = columns;

		if (columns.length < 2) {
			throw new DefinitionException("must have at least 2 columns");
		}
	}

	public Name[] getColumns() {
		return columns;
	}
}
