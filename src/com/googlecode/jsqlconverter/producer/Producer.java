package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.Statement;

public interface Producer {
	public void produce(Statement[] statements);
}
