package com.googlecode.jsqlconverter.definition.type;

public class UnhandledType implements Type {
	private String name;

	public UnhandledType(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
