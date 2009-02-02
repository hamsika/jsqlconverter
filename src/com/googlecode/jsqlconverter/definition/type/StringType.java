package com.googlecode.jsqlconverter.definition.type;

public enum StringType implements Type {
	CHAR,
	LONGTEXT,	/* 1 - 4294967295 bytes */
	MEDIUMTEXT,	/* 1 - 16777215 bytes */
	NCHAR,
	NTEXT,
	NVARCHAR,
	TINYTEXT,	/* 1 - 255 bytes */
	TEXT,		/* 1 - 65535 bytes */
	VARCHAR
}
