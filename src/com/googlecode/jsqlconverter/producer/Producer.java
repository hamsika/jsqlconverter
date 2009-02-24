package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.truncate.table.Truncate;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.logging.LogLevel;
import com.googlecode.jsqlconverter.producer.interfaces.CreateIndexInterface;
import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;
import com.googlecode.jsqlconverter.producer.interfaces.InsertFromValuesInterface;
import com.googlecode.jsqlconverter.producer.interfaces.TruncateInterface;

import java.io.PrintStream;
import java.util.logging.Logger;

public abstract class Producer {
	protected static final Logger LOG = Logger.getLogger(Producer.class.getName());
	protected PrintStream out = System.out;

	// don't force the use of a specific constructor
	public Producer() {

	}

	public Producer(PrintStream out) {
		this.out = out;
	}

	public final void produce(Statement statement) throws ProducerException {
		if (statement instanceof CreateIndex) {
			if (this instanceof CreateIndexInterface) {
				CreateIndexInterface cii = (CreateIndexInterface)this;
				cii.doCreateIndex((CreateIndex)statement);
			} else {
				LOG.warning("This Producer does not support CreateIndexInterface");
			}
		} else if (statement instanceof CreateTable) {
			if (this instanceof CreateTableInterface) {
				CreateTableInterface cti = (CreateTableInterface)this;
				cti.doCreateTable((CreateTable)statement);
			} else {
				LOG.warning("This Producer does not support CreateTableInterface");
			}
		} else if (statement instanceof InsertFromValues) {
			if (this instanceof InsertFromValuesInterface) {
				InsertFromValuesInterface ifvi = (InsertFromValuesInterface)this;
				ifvi.doInsertFromValues((InsertFromValues)statement);
			} else {
				LOG.warning("This Producer does not support InsertFromValuesInterface");
			}
		} else if (statement instanceof Truncate) {
			if (this instanceof TruncateInterface) {
				TruncateInterface ti = (TruncateInterface)this;
				ti.doTruncate((Truncate)statement);
			} else {
				LOG.warning("This Producer does not support TruncateInterface");
			}
		} else {
			LOG.log(LogLevel.UNHANDLED, "statement type: " + statement.getClass().getName());
		}
	}
}
