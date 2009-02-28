package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.testutils.StatementGenerator;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class AccessMDBProducerTest extends TestCase {
	private CreateTable[] createTables;

	protected void setUp() {
		StatementGenerator sg = new StatementGenerator();

		createTables = sg.generateCreateTableStatements(50);
	}

	public void testAccessProducer() throws IOException {
		AccessMDBProducer producer = new AccessMDBProducer(new File("typemapping.mdb"));

		for (CreateTable ct : createTables) {
			producer.produce(ct);
		}
	}

	public static Test suite() {
		return new TestSuite(AccessMDBProducerTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
