package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.producer.sql.*;
import com.googlecode.jsqlconverter.testutils.CommonTasks;
import com.googlecode.jsqlconverter.testutils.TestProperties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.poi.ss.usermodel.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TypeMappingCorrectTest extends TestCase {
	private static final int COL_ACCESS_MDB = 4;
	private static final int COL_ACCESS_SQL = 3;
	private static final int COL_MYSQL = 6;
	private static final int COL_ORACLE = 12;
	private static final int COL_POSTGRESQL = 7;
	private static final int COL_SQLSERVER = 8;
	private static final int COL_SQLFAIRY = 9;
	private static final int COL_TURBINE = 10;

	private HashMap<String, Row> typeMap = new HashMap<String, Row>();
	private Type[] types = CommonTasks.getTypes();
	private PrintStream out;

	protected void setUp() throws Exception {
		FileInputStream in = new FileInputStream(TestProperties.getProperty("typemapping.file"));
		Workbook wb = WorkbookFactory.create(in);

		ArrayList<String> sheets = new ArrayList<String>();

		sheets.add("Binary");
		sheets.add("Boolean");
		sheets.add("Date Time");
		sheets.add("Monetary");
		sheets.add("Approximate Numeric");
		sheets.add("Exact Numeric");
		sheets.add("String");

		for (String sheetName : sheets) {
			Sheet sheet = wb.getSheet(sheetName);

			for (Iterator it = sheet.rowIterator(); it.hasNext();) {
				Row row = (Row)it.next();

				if (row.getRowNum() == 0) {
					continue;
				}

				Cell mainType = row.getCell(0);

				typeMap.put(mainType.getStringCellValue(), row);
			}
		}

		out = System.out;
	}

	public void testAccessMDB() throws IOException {
		AccessMDBProducer producer = new AccessMDBProducer(new File("typemapping.mdb"));

		for (Type type : types) {
			checkType(type, COL_ACCESS_MDB, producer.getClass().getName(), producer.getType(type).toString());
		}
	}

	public void testAccessSQL() {
		testTypeMapping(new AccessSQLProducer(out), COL_ACCESS_SQL);
	}

	public void testMySQL() {
		testTypeMapping(new MySQLProducer(out), COL_MYSQL);
	}

	public void testOracle() {
		testTypeMapping(new OracleSQLProducer(out), COL_ORACLE);
	}

	public void testPostgreSQL() {
		testTypeMapping(new PostgreSQLProducer(out), COL_POSTGRESQL);
	}

	public void testSQLServer() {
		testTypeMapping(new SQLServerProducer(out), COL_SQLSERVER);
	}

	public void testSQLFairy() throws TransformerException, ParserConfigurationException {
		SQLFairyXMLProducer producer = new SQLFairyXMLProducer(out);

		for (Type type : types) {
			checkType(type, COL_SQLFAIRY, producer.getClass().getName(), producer.getType(type));
		}
	}

	public void testTurbine() throws TransformerException, ParserConfigurationException {
		TurbineXMLProducer producer = new TurbineXMLProducer(out);

		for (Type type : types) {
			checkType(type, COL_TURBINE, producer.getClass().getName(), producer.getType(type));
		}
	}

	private void testTypeMapping(SQLProducer producer, int column) {
		for (Type type : types) {
			checkType(type, column, producer.getClass().getName(), producer.getType(type, 0));
		}
	}

	private void checkType(Type type, int column, String producerName, String producerType) {
		String types = type.toString();

		Row row = typeMap.get(types);

		assertNotNull(types + " is not listed", row);

		Cell cell = row.getCell(column);

		if (cell == null || cell.getStringCellValue().isEmpty()) {
			return;
		}

		assertEquals(producerName + " says: \"" + producerType + "\" file says: \"" + cell.getStringCellValue() + "\" for main type: \"" + type + "\"", cell.getStringCellValue(), producerType);
	}

	public static Test suite() {
		return new TestSuite(TypeMappingCorrectTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
