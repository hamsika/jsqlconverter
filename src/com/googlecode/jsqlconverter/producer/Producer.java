package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.logging.MyLogger;

public abstract class Producer {
	protected static MyLogger log = MyLogger.getLogger(Producer.class.getName());

	public abstract void produce(Statement[] statements);
}
