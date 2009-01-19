package com.googlecode.jsqlconverter.producer;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.googlecode.jsqlconverter.definition.Name;

public class PostgreSQLProducerTest extends TestCase {
	private PostgreSQLProducer producer;

	protected void setUp() {
		producer = new PostgreSQLProducer(System.out);
	}

	public void testValidName() {
		assertTrue(producer.isValidIdentifier(new Name("abc12f")));
		assertTrue(producer.isValidIdentifier(new Name("_abc")));
		assertTrue(producer.isValidIdentifier(new Name("a$bc")));
		assertTrue(producer.isValidIdentifier(new Name("a_bc")));
		assertTrue(producer.isValidIdentifier(new Name("_a_b_c")));
		assertTrue(producer.isValidIdentifier(new Name("a")));
		// 63 chars
		assertTrue(producer.isValidIdentifier(new Name("abccccccccccccccccccccccccccccccccccccccccccccccccccccccccddddd")));

		assertFalse(producer.isValidIdentifier(new Name("$abc")));
		assertFalse(producer.isValidIdentifier(new Name("33333333344444444444444")));

		// 64 chars
		assertFalse(producer.isValidIdentifier(new Name("abccccccccccccccccccccccccccccccccccccccccccccccccccccccccdddddd")));
	}

	public static Test suite() {
		return new TestSuite(PostgreSQLProducerTest.class);
	}

	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
