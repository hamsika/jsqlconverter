package com.googlecode.jsqlconverter.definition.type;

public class DecimalType implements NumericType {
	private int precision;	// max number of digits
	private int scale;		// decimal places

	// e.g: the number 23.5141 has a precision of 6 and a scale of 4

	public DecimalType(int precision) {
		this (precision, 0);
	}

	public DecimalType(int precision, int scale) {
		this.precision = precision;
		this.scale = scale;
	}

	public int getPrecision() {
		return precision;
	}

	public int getScale() {
		return scale;
	}
}
