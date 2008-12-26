package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.truncate.table.Truncate;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.logging.MyLogger;
import com.googlecode.jsqlconverter.logging.LogLevel;

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

	public final void produce(Statement statement) {
		if (statement instanceof CreateIndex) {
			doCreateIndex((CreateIndex)statement);
		} else if (statement instanceof CreateTable) {
			doCreateTable((CreateTable)statement);
		} else if (statement instanceof InsertFromValues) {
			doInsertFromValues((InsertFromValues)statement);
		} else if (statement instanceof Truncate) {
			doTruncate((Truncate)statement);
		} else {
			log.log(LogLevel.UNHANDLED, "statement type");
		}
	}

	public abstract void doCreateIndex(CreateIndex index);
	public abstract void doCreateTable(CreateTable table);
	public abstract void doInsertFromValues(InsertFromValues insert);
	public abstract void doTruncate(Truncate truncate);
}
