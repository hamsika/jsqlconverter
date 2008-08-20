package com.googlecode.jsqlconverter.definition.create.table;

import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.definition.Name;

import java.util.Vector;

public class Column {
	private Name columnName;
	private Type dataType;
	private Vector<Constraint> constraints = new Vector<Constraint>();
	private Vector<Reference> references = new Vector<Reference>();
	private int size = -1;

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

	public Vector<Constraint> getConstraints() {
		return constraints;
	}

	public Vector<Reference> getReferences() {
		return references;
	}

	// setters
	public void setSize(int size) {
		this.size = size;
	}

	// adders
	public void addConstraint(Constraint constraint) {
		constraints.add(constraint);
	}

	public void addReference(Reference reference) {
		references.add(reference);
	}
}
