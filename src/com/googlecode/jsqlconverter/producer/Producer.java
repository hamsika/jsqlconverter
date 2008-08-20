package com.googlecode.jsqlconverter.producer;

import java.sql.Statement;

public interface Producer {
	public void produce(Statement[] statements);
}
