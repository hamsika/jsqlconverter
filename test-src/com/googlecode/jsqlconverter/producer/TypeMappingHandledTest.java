package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.type.DecimalType;
import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.producer.sql.*;
import com.googlecode.jsqlconverter.testutils.CommonTasks;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class TypeMappingHandledTest extends TestCase {
	private PrintStream out = System.out;
	private Type[] types = CommonTasks.getTypes();
	private DecimalType decimalType;
	private AccessMDBProducer accessMDBProducer;
	private SQLFairyXMLProducer sqlfairyProducer;
	private ArrayList<SQLProducer> sqlproducers = new ArrayList<SQLProducer>();
	private TurbineXMLProducer turbineProducer;

	protected void setUp() throws TransformerException, ParserConfigurationException, IOException {
		decimalType = new DecimalType(4, 5);

		accessMDBProducer = new AccessMDBProducer(new File("typemapping.mdb"));

		sqlproducers.add(new AccessSQLProducer(out));
		sqlproducers.add(new MySQLProducer(out));
		sqlproducers.add(new OracleSQLProducer(out));
		sqlproducers.add(new PostgreSQLProducer(out));
		sqlproducers.add(new SQLServerProducer(out));

		sqlfairyProducer = new SQLFairyXMLProducer(out);

		turbineProducer = new TurbineXMLProducer(out);
	}

	public void testAcesssMDBProducer() {
		for (Type type : types) {
			assertNotNull(accessMDBProducer.getClass().getName() + " does not handle " + type, accessMDBProducer.getType(type));
		}

		assertNotNull(accessMDBProducer.getClass().getName() + " does not handle DecimalType", accessMDBProducer.getType(decimalType));
	}

	public void testSQLFairy() {
		for (Type type : types) {
			assertNotNull(sqlfairyProducer.getClass().getName() + " does not handle " + type, sqlfairyProducer.getType(type));
		}

		assertNotNull(sqlfairyProducer.getClass().getName() + " does not handle DecimalType", sqlfairyProducer.getType(decimalType));
	}

	public void testSQLProducers() {
		for (SQLProducer sqlproducer : sqlproducers) {
			for (Type type : types) {
				assertNotNull(sqlproducer.getClass().getName() + " does not handle " + type, sqlproducer.getType(type, 0));
			}

			assertNotNull(sqlproducer.getClass().getName() + " does not handle DecimalType", sqlproducer.getType(decimalType));
		}
	}

	public void testTurbineProducer() {
		for (Type type : types) {
			assertNotSame(turbineProducer.getClass().getName() + " does not handle " + type, turbineProducer.getType(type), "OTHER");
		}

		assertNotSame(turbineProducer.getClass().getName() + " does not handle DecimalType", turbineProducer.getType(decimalType), "OTHER");
	}

	public static Test suite() {
		return new TestSuite(TypeMappingHandledTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
