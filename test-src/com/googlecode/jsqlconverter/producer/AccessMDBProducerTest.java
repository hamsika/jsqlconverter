package com.googlecode.jsqlconverter.producer;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.parser.GeneratorParser;

public class AccessMDBProducerTest extends TestCase {
	private CreateTable[] createTables;

	@Override
	protected void setUp() {
		GeneratorParser gp = new GeneratorParser(50);

		createTables = gp.generateCreateTableStatements(50);
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
