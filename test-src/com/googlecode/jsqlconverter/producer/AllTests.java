package com.googlecode.jsqlconverter.producer;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("all producer tests");

		suite.addTestSuite(PostgreSQLProducerTest.class);
		suite.addTestSuite(TypeMappingHandledTest.class);

		return suite;
	}
}
