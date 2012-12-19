package com.googlecode.jsqlconverter.parser;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Types;

import org.junit.Before;
import org.junit.Test;

import com.healthmarketscience.jackcess.DataType;

public class TestParserTypeMappingHandled {
	private AccessMDBParser accessMDBParser;

	// TODO: add turbine / sql fairy

	@Before
	public void setUp() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		accessMDBParser = new AccessMDBParser(null, false);
	}

	@Test
	public void testAccessMDBParser() {
		for (DataType type : DataType.values()) {
			assertNotNull(type + " type isn't handled by AccessMDBParser", accessMDBParser.getType(type));
		}
	}

	@Test
	public void testJDBCParser() throws IllegalAccessException {
		for (Field field  : Types.class.getDeclaredFields()) {
			assertNotNull(field.getName() + " type isn't handled by JDBCParser", JDBCParser.getType(field.getInt(field), 0, 0));
		}
	}
}
