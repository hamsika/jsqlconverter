package com.googlecode.jsqlconverter;

import junit.framework.TestSuite;
import junit.framework.Test;

public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("All jSQLConverter Tests");

		suite.addTest(com.googlecode.jsqlconverter.parser.AllTests.suite());
		suite.addTest(com.googlecode.jsqlconverter.producer.AllTests.suite());

		return suite;
	}

	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
