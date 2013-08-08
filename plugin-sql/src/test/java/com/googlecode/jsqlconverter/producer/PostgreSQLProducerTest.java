package com.googlecode.jsqlconverter.producer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.jsqlconverter.producer.sql.PostgreSQLProducer;
import com.googlecode.jsqlconverter.producer.sql.SQLProducer;

public class PostgreSQLProducerTest {
	private SQLProducer producer;

	@Before
	public void setUp() {
		producer = new PostgreSQLProducer(System.out);
	}

	@Test
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
}
