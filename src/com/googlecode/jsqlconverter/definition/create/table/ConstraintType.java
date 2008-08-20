package com.googlecode.jsqlconverter.definition.create.table;

public enum ConstraintType {
	AUTO_INCREMENT, // is this needed if PRIMARY_KEY is here?
	CHECK,
	COMMENT,
	CONSTRAINT,
	DEFAULT,
	NOT_NULL,
	NULL,
	PRIMARY_KEY,
	UNIQUE
}
