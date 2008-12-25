package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.logging.MyLogger;

import java.io.PrintStream;

public abstract class Producer {
	protected static MyLogger log = MyLogger.getLogger(Producer.class.getName());
	protected PrintStream out = System.out;

	// don't force the use of a specific constructor
	public Producer() {

	}

	public Producer(PrintStream out) {
		this.out = out;
	}

	public abstract void produce(Statement statement);
}
