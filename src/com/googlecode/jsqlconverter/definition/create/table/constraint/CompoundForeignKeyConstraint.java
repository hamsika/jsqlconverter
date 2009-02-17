package com.googlecode.jsqlconverter.definition.create.table.constraint;

import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.DefinitionException;

public class CompoundForeignKeyConstraint extends ForeignKeyConstraint {
	private Name[] columnNames;
	private Name[] referenceColumns;

	public CompoundForeignKeyConstraint(Name tableName, Name[] columnNames, Name[] referenceColumns) {
		super(tableName);

		this.columnNames = columnNames;
		this.referenceColumns = referenceColumns;

		if (columnNames.length != referenceColumns.length) {
			throw new DefinitionException("column names of local and reference table must be the same length");
		}

		if (columnNames.length < 2) {
			throw new DefinitionException("compound foreign keys must have at least 2 columns");
		}
	}

	public Name[] getColumnNames() {
		return columnNames;
	}

	public Name[] getReferenceColumns() {
		return referenceColumns;
	}
}
