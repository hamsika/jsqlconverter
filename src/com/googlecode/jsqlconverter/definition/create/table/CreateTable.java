package com.googlecode.jsqlconverter.definition.create.table;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.CompoundForeignKeyConstraint;

import java.util.ArrayList;

// TODO: support non-unique keys, compound or otherwise
public class CreateTable extends Statement implements Comparable<CreateTable> {
	private Name tableName;
	private ArrayList<Column> columns = new ArrayList<Column>();
	private ArrayList<TableOption> options = new ArrayList<TableOption>();
	private ArrayList<KeyConstraint> compoundUniqueKeys = new ArrayList<KeyConstraint>();
	private ArrayList<CompoundForeignKeyConstraint> compoundForeignKeys = new ArrayList<CompoundForeignKeyConstraint>();
	private KeyConstraint primaryKey = null;

	public CreateTable(Name tableName) {
		this.tableName = tableName;
	}

	// getters
	public Name getName() {
		return tableName;
	}

	public Column[] getColumns() {
		return columns.toArray(new Column[columns.size()]);
	}

	public Column getColumn(int index) {
		return columns.get(index);
	}

	public Column getColumn(String name) {
		for (Column column : columns) {
			if (column.getName().getObjectName().equals(name)) {
				return column;
			}
		}

		return null;
	}

	public int getColumnCount() {
		return columns.size();
	}

	public TableOption[] getTableOptions() {
		return options.toArray(new TableOption[options.size()]);
	}

	public KeyConstraint getPrimaryCompoundKeyConstraint() {
		return primaryKey;
	}

	public KeyConstraint[] getUniqueCompoundKeyConstraint() {
		return compoundUniqueKeys.toArray(new KeyConstraint[compoundUniqueKeys.size()]);
	}

	public CompoundForeignKeyConstraint[] getCompoundForeignKeyConstraints() {
		return compoundForeignKeys.toArray(new CompoundForeignKeyConstraint[compoundForeignKeys.size()]);
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
		compoundUniqueKeys.add(keyConstraint);
	}

	public void addCompoundForeignKeyConstraint(CompoundForeignKeyConstraint keyConstraint) {
		compoundForeignKeys.add(keyConstraint);
	}

	// removers
	public void removeColumn(Column column) {
		columns.remove(column);
	}

	public int compareTo(CreateTable that) {
		// compare this to that
		int thisFKeyCount = 0;
		int thatFKeyCount = 0;

		for (Column column : getColumns()) {
			if (column.getForeignKeyConstraint() != null) {
				++thisFKeyCount;
			}
		}

		for (Column column : that.getColumns()) {
			if (column.getForeignKeyConstraint() != null) {
				++thatFKeyCount;
			}
		}

		if (thisFKeyCount == 0 && thatFKeyCount == 0) {
			// nothing to do
			return 0;
		} else if (thisFKeyCount == 0) {
			// this table doesn't have any foreign keys, move this up the stack
			return -1;
		} else if (thatFKeyCount == 0) {
			// that table doesn't have any foreign keys, so move that up the stack
			return 1;
		} else {
			// check if that table is using this table name, if it is, move this up, otherwise move this down
			for (Column column : that.getColumns()) {
				if (column.getForeignKeyConstraint() != null) {
					if (column.getForeignKeyConstraint().getTableName().getObjectName().equals(getName().getObjectName())) {
						return -1;
					}
				}
			}

			return 1;
		}
	}
}
