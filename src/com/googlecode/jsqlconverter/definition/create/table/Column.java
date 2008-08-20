package com.googlecode.jsqlconverter.definition.create.table;

import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.definition.Name;

import java.util.ArrayList;

public class Column {
	private Name columnName;
	private Type dataType;
	private ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	private ArrayList<Reference> references = new ArrayList<Reference>();
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

	public Constraint[] getConstraints() {
		return constraints.toArray(new Constraint[] {});
	}

	public Reference[] getReferences() {
		return references.toArray(new Reference[] {});
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
