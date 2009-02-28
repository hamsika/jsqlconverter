package com.googlecode.jsqlconverter.parser;

import junit.framework.Test;
import junit.framework.TestSuite;

public final class AllTests {
	private AllTests() { }

	public static Test suite() {
		TestSuite suite = new TestSuite("all parser tests");

		suite.addTestSuite(DelimitedParserTest.class);
		suite.addTestSuite(TypeMappingCorrectTest.class);
		suite.addTestSuite(TypeMappingHandledTest.class);
		suite.addTestSuite(ValidationTest.class);

		return suite;
	}
}
