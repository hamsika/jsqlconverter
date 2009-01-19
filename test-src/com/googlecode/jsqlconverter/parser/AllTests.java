package com.googlecode.jsqlconverter.parser;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("all parser tests");

		suite.addTestSuite(DelimitedParserTest.class);

		return suite;
	}
}
