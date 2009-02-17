package com.googlecode.jsqlconverter.definition.create.table.constraint;

import com.googlecode.jsqlconverter.definition.Name;

public class ColumnForeignKeyConstraint extends ForeignKeyConstraint {
	private Name columnName;

	public ColumnForeignKeyConstraint(Name tableName, Name columnName) {
		super(tableName);

		this.columnName = columnName;
	}

	public Name getColumnName() {
		return columnName;
	}
}
