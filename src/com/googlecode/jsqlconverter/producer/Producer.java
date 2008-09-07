package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.logging.MyLogger;

import java.io.PrintStream;

public abstract class Producer {
	protected static MyLogger log = MyLogger.getLogger(Producer.class.getName());
	protected PrintStream out = System.out;

	public Producer(PrintStream out) {
		this.out = out;
	}

	public abstract void produce(Statement statement);
}
