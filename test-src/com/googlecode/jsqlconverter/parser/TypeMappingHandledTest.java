package com.googlecode.jsqlconverter.parser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.sql.Types;
import java.lang.reflect.Field;

import com.healthmarketscience.jackcess.DataType;

public class TypeMappingHandledTest extends TestCase {
	private AccessMDBParser accessMDBParser;
	private JDBCParser jdbcParser;

	// TODO: add turbine / sql fairy

	protected void setUp() {
		accessMDBParser = new AccessMDBParser(null, false);
		jdbcParser = new JDBCParser(null);
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
