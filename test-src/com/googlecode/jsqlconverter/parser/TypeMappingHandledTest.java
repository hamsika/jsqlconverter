package com.googlecode.jsqlconverter.parser;

import java.lang.reflect.Field;
import java.sql.Types;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.healthmarketscience.jackcess.DataType;

public class TypeMappingHandledTest extends TestCase {
	private AccessMDBParser accessMDBParser;
	private JDBCParser jdbcParser;

	// TODO: add turbine / sql fairy

	protected void setUp() {
		accessMDBParser = new AccessMDBParser(null, false);

		try {
			jdbcParser = new JDBCParser(null, null, null, null, null, null, null, null, true);
		} catch (Exception e) {
			fail();
		}
	}

	public void testAccessMDBParser() {
		for (DataType type : DataType.values()) {
			assertNotNull(type + " type isn't handled by AccessMDBParser", accessMDBParser.getType(type));
		}
	}

	public void testJDBCParser() throws IllegalAccessException {
		for (Field field  : Types.class.getDeclaredFields()) {
			assertNotNull(field.getName() + " type isn't handled by JDBCParser", jdbcParser.getType(field.getInt(field), 0, 0));
		}
	}

	public static Test suite() {
		return new TestSuite(TypeMappingHandledTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
