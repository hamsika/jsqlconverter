package com.googlecode.jsqlconverter.producer;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.googlecode.jsqlconverter.parser.DelimitedParserTest;

public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("all producer tests");

		suite.addTestSuite(PostgreSQLProducerTest.class);

		return suite;
	}
}
