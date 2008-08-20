package com.googlecode.jsqlconverter.definition.create.table;

public class Constraint {

	// should this class actually be a base class for others to extend
	// ie: classes for each constraint type
	// constraint (has name)
	// unique (has one or more column_names) (and index_param??)
	// foreign key / reference should also probably extend this class

	// TODO: remove value when subclassing

	private ConstraintType constraintType;
	private Object constraintValue;

	public Constraint(ConstraintType constraintType, Object constraintValue) {
		this.constraintType = constraintType;
		this.constraintValue = constraintValue;
	}

	public ConstraintType getType() {
		return constraintType;
	}

	public Object getValue() {
		return constraintValue;
	}
}
