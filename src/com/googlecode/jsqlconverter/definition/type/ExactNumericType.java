package com.googlecode.jsqlconverter.definition.type;

public enum ExactNumericType implements NumericType {
	BIGINT,		/* 8 byte -9223372036854775808 to 9223372036854775807 */
	INTEGER,	/* 4 byte -2147483648 to 2147483647 */
	MEDIUMINT,	/* 3 byte -8388608 to 8388607 */
	NUMERIC,
	SMALLINT,	/* 2 byte -32768 to 32767 */
	TINYINT		/* 1 byte -128 to 127 */
}
