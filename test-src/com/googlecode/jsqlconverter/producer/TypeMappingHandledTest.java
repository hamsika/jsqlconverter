package com.googlecode.jsqlconverter.producer;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.io.PrintStream;
import java.io.File;
import java.io.IOException;

import com.googlecode.jsqlconverter.definition.type.*;

import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;

public class TypeMappingHandledTest extends TestCase {
	private PrintStream out = System.out;
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
		sqlproducers.add(new PostgreSQLProducer(out));
		sqlproducers.add(new SQLServerProducer(out));

		sqlfairyProducer = new SQLFairyXMLProducer(out);

		turbineProducer = new TurbineXMLProducer(out);
	}

	public void testAcesssMDBProducer() {
		for (ApproximateNumericType type : ApproximateNumericType.values()) {
			assertNotNull(accessMDBProducer.getClass().getName() + " does not handle " + type, accessMDBProducer.getType(type));
		}

		for (BinaryType type : BinaryType.values()) {
			assertNotNull(accessMDBProducer.getClass().getName() + " does not handle " + type, accessMDBProducer.getType(type));
		}

		for (BooleanType type : BooleanType.values()) {
			assertNotNull(accessMDBProducer.getClass().getName() + " does not handle " + type, accessMDBProducer.getType(type));
		}

		for (DateTimeType type : DateTimeType.values()) {
			assertNotNull(accessMDBProducer.getClass().getName() + " does not handle " + type, accessMDBProducer.getType(type));
		}

		for (ExactNumericType type : ExactNumericType.values()) {
			assertNotNull(accessMDBProducer.getClass().getName() + " does not handle " + type, accessMDBProducer.getType(type));
		}

		for (MonetaryType type : MonetaryType.values()) {
			assertNotNull(accessMDBProducer.getClass().getName() + " does not handle " + type, accessMDBProducer.getType(type));
		}

		for (StringType type : StringType.values()) {
			assertNotNull(accessMDBProducer.getClass().getName() + " does not handle " + type, accessMDBProducer.getType(type));
		}

		assertNotNull(accessMDBProducer.getClass().getName() + " does not handle DecimalType", accessMDBProducer.getType(decimalType));
	}

	public void testSQLFairy() {
		for (ApproximateNumericType type : ApproximateNumericType.values()) {
			assertNotNull(sqlfairyProducer.getClass().getName() + " does not handle " + type, sqlfairyProducer.getType(type));
		}

		for (BinaryType type : BinaryType.values()) {
			assertNotNull(sqlfairyProducer.getClass().getName() + " does not handle " + type, sqlfairyProducer.getType(type));
		}

		for (BooleanType type : BooleanType.values()) {
			assertNotNull(sqlfairyProducer.getClass().getName() + " does not handle " + type, sqlfairyProducer.getType(type));
		}

		for (DateTimeType type : DateTimeType.values()) {
			assertNotNull(sqlfairyProducer.getClass().getName() + " does not handle " + type, sqlfairyProducer.getType(type));
		}

		for (ExactNumericType type : ExactNumericType.values()) {
			assertNotNull(sqlfairyProducer.getClass().getName() + " does not handle " + type, sqlfairyProducer.getType(type));
		}

		for (MonetaryType type : MonetaryType.values()) {
			assertNotNull(sqlfairyProducer.getClass().getName() + " does not handle " + type, sqlfairyProducer.getType(type));
		}

		for (StringType type : StringType.values()) {
			assertNotNull(sqlfairyProducer.getClass().getName() + " does not handle " + type, sqlfairyProducer.getType(type));
		}

		assertNotNull(sqlfairyProducer.getClass().getName() + " does not handle DecimalType", sqlfairyProducer.getType(decimalType));
	}

	public void testSQLProducers() {
		for (SQLProducer sqlproducer : sqlproducers) {
			for (ApproximateNumericType type : ApproximateNumericType.values()) {
				assertNotNull(sqlproducer.getClass().getName() + " does not handle " + type, sqlproducer.getType(type));
			}

			for (BinaryType type : BinaryType.values()) {
				assertNotNull(sqlproducer.getClass().getName() + " does not handle " + type, sqlproducer.getType(type));
			}

			for (BooleanType type : BooleanType.values()) {
				assertNotNull(sqlproducer.getClass().getName() + " does not handle " + type, sqlproducer.getType(type));
			}

			for (DateTimeType type : DateTimeType.values()) {
				assertNotNull(sqlproducer.getClass().getName() + " does not handle " + type, sqlproducer.getType(type));
			}

			for (ExactNumericType type : ExactNumericType.values()) {
				assertNotNull(sqlproducer.getClass().getName() + " does not handle " + type, sqlproducer.getType(type));
			}

			for (MonetaryType type : MonetaryType.values()) {
				assertNotNull(sqlproducer.getClass().getName() + " does not handle " + type, sqlproducer.getType(type));
			}

			for (StringType type : StringType.values()) {
				assertNotNull(sqlproducer.getClass().getName() + " does not handle " + type, sqlproducer.getType(type));
			}

			assertNotNull(sqlproducer.getClass().getName() + " does not handle DecimalType", sqlproducer.getType(decimalType));
		}
	}

	public void testTurbineProducer() {
		for (ApproximateNumericType type : ApproximateNumericType.values()) {
			assertNotSame(turbineProducer.getClass().getName() + " does not handle " + type, turbineProducer.getType(type), "OTHER");
		}

		for (BinaryType type : BinaryType.values()) {
			assertNotSame(turbineProducer.getClass().getName() + " does not handle " + type, turbineProducer.getType(type), "OTHER");
		}

		for (BooleanType type : BooleanType.values()) {
			assertNotSame(turbineProducer.getClass().getName() + " does not handle " + type, turbineProducer.getType(type), "OTHER");
		}

		for (DateTimeType type : DateTimeType.values()) {
			assertNotSame(turbineProducer.getClass().getName() + " does not handle " + type, turbineProducer.getType(type), "OTHER");
		}

		for (ExactNumericType type : ExactNumericType.values()) {
			assertNotSame(turbineProducer.getClass().getName() + " does not handle " + type, turbineProducer.getType(type), "OTHER");
		}

		for (MonetaryType type : MonetaryType.values()) {
			assertNotSame(turbineProducer.getClass().getName() + " does not handle " + type, turbineProducer.getType(type), "OTHER");
		}

		for (StringType type : StringType.values()) {
			assertNotSame(turbineProducer.getClass().getName() + " does not handle " + type, turbineProducer.getType(type), "OTHER");
		}

		assertNotSame(turbineProducer.getClass().getName() + " does not handle DecimalType", turbineProducer.getType(decimalType), "OTHER");
	}

	public static Test suite() {
		return new TestSuite(TypeMappingHandledTest.class);
	}

	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
