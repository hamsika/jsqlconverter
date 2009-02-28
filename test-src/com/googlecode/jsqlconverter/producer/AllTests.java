package com.googlecode.jsqlconverter.producer;

import junit.framework.Test;
import junit.framework.TestSuite;

public final class AllTests {
	private AllTests() { }

	public static Test suite() {
		TestSuite suite = new TestSuite("all producer tests");

		suite.addTestSuite(AccessMDBProducerTest.class);
		suite.addTestSuite(PostgreSQLProducerTest.class);
		suite.addTestSuite(SQLProducerTest.class);
		suite.addTestSuite(TypeMappingHandledTest.class);
		suite.addTestSuite(TypeMappingCorrectTest.class);

		return suite;
	}
}
