package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.producer.sql.PostgreSQLProducer;
import com.googlecode.jsqlconverter.producer.sql.SQLProducer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PostgreSQLProducerTest extends TestCase {
	private SQLProducer producer;

	protected void setUp() {
		producer = new PostgreSQLProducer(System.out);
	}

	public void testValidName() {
		assertTrue(producer.isValidIdentifier("abc12f"));
		assertTrue(producer.isValidIdentifier("_abc"));
		assertTrue(producer.isValidIdentifier("a$bc"));
		assertTrue(producer.isValidIdentifier("a_bc"));
		assertTrue(producer.isValidIdentifier("_a_b_c"));
		assertTrue(producer.isValidIdentifier("a"));
		// 63 chars
		assertTrue(producer.isValidIdentifier("abccccccccccccccccccccccccccccccccccccccccccccccccccccccccddddd"));

		assertFalse(producer.isValidIdentifier("$abc"));
		assertFalse(producer.isValidIdentifier("33333333344444444444444"));

		// 64 chars
		assertFalse(producer.isValidIdentifier("abccccccccccccccccccccccccccccccccccccccccccccccccccccccccdddddd"));
	}

	public static Test suite() {
		return new TestSuite(PostgreSQLProducerTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
