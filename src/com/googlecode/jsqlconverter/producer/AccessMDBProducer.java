package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.logging.LogLevel;
import com.healthmarketscience.jackcess.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class AccessMDBProducer extends Producer {
	private Database db;

	public AccessMDBProducer(File mdbFile) {
		try {
			db = Database.create(mdbFile);
		} catch (IOException e) {
			//throw new ProducerException(e.getMessage(), e.getCause());
			// TODO: complete this
			e.printStackTrace();
		}
	}

	public void produce(Statement statement) {
		if (statement instanceof CreateTable) {
			handleCreateTable((CreateTable)statement);
		} else {
			log.log(LogLevel.UNHANDLED, "statement type");
		}
	}

	private void handleCreateTable(CreateTable table) {
		TableBuilder tb = new TableBuilder(table.getName().getObjectName());

		for (com.googlecode.jsqlconverter.definition.create.table.Column column : table.getColumns()) {
			Column jcol = new Column();
			jcol.setName(column.getName().getObjectName());

			// TODO: setType, setSize

			tb.addColumn(jcol);
		}

		// TODO: INDEXES!

		try {
			tb.toTable(db);
		} catch (IOException e) {
			// TODO: complete this
			//throw new ProducerException(e.getMessage(), e.getCause());
			e.printStackTrace();
		}
	}
}
