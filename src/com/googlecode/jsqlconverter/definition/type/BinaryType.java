package com.googlecode.jsqlconverter.definition.type;

public enum BinaryType implements Type {
	BIT,
	BINARY,
	BLOB,		/* 1 - 65535 bytes */
	LONGBLOB,	/* 1 - 4294967295 bytes */
	MEDIUMBLOB,	/* 1 - 16777215 bytes */
	TINYBLOB,	/* 1 - 255 bytes */
	VARBINARY
}
