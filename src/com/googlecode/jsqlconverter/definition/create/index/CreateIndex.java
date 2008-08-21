package com.googlecode.jsqlconverter.definition.create.index;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;

import java.util.ArrayList;

public class CreateIndex extends Statement {
	// TODO: may need to support SortSequence for each column
	private Name indexName;
	private Name tableName;
	private boolean isUnique;
	private ArrayList<Name> columns = new ArrayList<Name>();
	private SortSequence sortSequence = SortSequence.UNKNOWN;
	private IndexType indexType = IndexType.UNKNOWN;

	public enum SortSequence { ASC, DESC, UNKNOWN }
	public enum IndexType { CLUSTERED, HASHED, UNKNOWN }

	public CreateIndex(Name indexName, Name tableName) {
		this.indexName = indexName;
		this.tableName = tableName;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public void setSortSequence(SortSequence sortSequence) {
		this.sortSequence = sortSequence;
	}

	public void setType(IndexType indexType) {
		this.indexType = indexType;
	}

	public void addColumn(Name columnName) {
		columns.add(columnName);
	}

	public boolean isUnique() {
		return isUnique;
	}

	public Name getIndexName() {
		return indexName;
	}

	public Name getTableName() {
		return tableName;
	}

	public IndexType getIndexType() {
		return indexType;
	}

	public Name[] getColumns() {
		return columns.toArray(new Name[] {});
	}

	public SortSequence getSortSequence() {
		return sortSequence;
	}
}
