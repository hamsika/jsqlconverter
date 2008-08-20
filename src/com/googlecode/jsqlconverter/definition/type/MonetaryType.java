package com.googlecode.jsqlconverter.definition.type;

public enum MonetaryType implements Type {
	MONEY,		/* 8 byte -922337203685477.5808 to 922337203685477.5807 */
	SMALLMONEY	/* 4 byte -214748.3648 to 214748.3647 */
}
