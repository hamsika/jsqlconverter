package com.googlecode.jsqlconverter.definition.create.table.constraint;

import com.googlecode.jsqlconverter.definition.Name;

public class ForeignKeyConstraint extends ColumnConstraint {
	private Name tableName;
	private Name columnName;

	private ForeignKeyMatch match;
	private ForeignKeyAction updateAction = null;
	private ForeignKeyAction deleteAction = null;

	public ForeignKeyConstraint(Name tableName, Name columnName) {
		this.tableName = tableName;
		this.columnName = columnName;
	}

	// getters
	public Name getTableName() {
		return tableName;
	}

	public Name getColumnName() {
		return columnName;
	}

	public ForeignKeyAction getUpdateAction() {
		return updateAction;
	}

	public ForeignKeyAction getDeleteAction() {
		return deleteAction;
	}

	public ForeignKeyMatch getMatch() {
		return match;
	}

	// setters
	public void setMatch(ForeignKeyMatch match) {
		this.match = match;
	}

	public void setUpdate(ForeignKeyAction action) {
		updateAction = action;
	}

	public void setDelete(ForeignKeyAction delete) {
		deleteAction = delete;
	}
}
