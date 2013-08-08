package com.googlecode.jsqlconverter.definition.type;

public enum ApproximateNumericType implements NumericType {
	DOUBLE, /*
				8 byte
				-1.7976931348623157E+308 - -2.2250738585072014E-308
				2.2250738585072014E-308 - 1.7976931348623157E+308
				15dp accuracy
			*/
	FLOAT, /*
				? byte
				-3.402823466E+38 - -1.175494351E-38
				1.175494351E-38 to 3.402823466E+38
				7dp accuracy
			*/
	REAL	/*
				4 byte
				6dp accuracy			
			*/
}
