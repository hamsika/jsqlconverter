package com.googlecode.jsqlconverter.definition.type;

public class UnhandledType implements Type {
	private String typeString;

	public UnhandledType(String typeString) {
		this.typeString = typeString;
	}

	public String toString() {
		return typeString;
	}
}
