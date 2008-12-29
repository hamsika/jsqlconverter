package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.truncate.table.Truncate;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.logging.LogLevel;

import java.io.PrintStream;
import java.util.logging.Logger;

public abstract class Producer {
	protected static Logger log = Logger.getLogger(Producer.class.getName());
	protected PrintStream out = System.out;

	// don't force the use of a specific constructor
	public Producer() {

	}

	public Producer(PrintStream out) {
		this.out = out;
	}

	public final void produce(Statement statement) throws ProducerException {
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

	public abstract void doCreateIndex(CreateIndex index) throws ProducerException;
	public abstract void doCreateTable(CreateTable table) throws ProducerException;
	public abstract void doInsertFromValues(InsertFromValues insert) throws ProducerException;
	public abstract void doTruncate(Truncate truncate) throws ProducerException;

	public abstract void end() throws ProducerException;
}
