package com.googlecode.jsqlconverter.producer;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.testutils.CommonTasks;

public class TestAccessMDBProducer {
	private CreateTable[] createTables;

	@Before
	public void setUp() {
		createTables = CommonTasks.getCreateTables();
	}

	@Test
	public void testAccessProducer() throws IOException {
		AccessMDBProducer producer = new AccessMDBProducer(new File("typemapping.mdb"));

		for (CreateTable ct : createTables) {
			producer.produce(ct);
		}
	}
}
