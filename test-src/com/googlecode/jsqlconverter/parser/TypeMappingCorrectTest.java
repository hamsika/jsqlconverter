package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.testutils.TestProperties;
import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.definition.type.DecimalType;
import com.healthmarketscience.jackcess.DataType;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.*;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TypeMappingCorrectTest extends TestCase {
	private static final int COL_ACCESS_MDB = 4;
	/*private static final int COL_JDBC = 5;
	private static final int COL_SQLFAIRY = 9;
	private static final int COL_TURBINE = 10;*/

	private HashMap<String, Row> typeMap = new HashMap<String, Row>();
	private HashMap<String, DataType> jackcessTypeMap = new HashMap<String, DataType>();

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

		for (DataType jtype : DataType.values()) {
			jackcessTypeMap.put(jtype.toString(), jtype);
		}
	}

	public void testAccessMDB() throws IOException {
		AccessMDBParser parser = new AccessMDBParser(new File("typemapping.mdb"), false);

		for (Row row : typeMap.values()) {
			Cell parserCell = row.getCell(COL_ACCESS_MDB);
			Cell mainCell = row.getCell(0);

			if (parserCell.getStringCellValue().isEmpty()) {
				continue;
			}

			DataType jackcessType = jackcessTypeMap.get(parserCell.getStringCellValue());

			Type type = parser.getType(jackcessType);

			if (type instanceof DecimalType) {
				continue;
			}

			assertEquals(mainCell.getStringCellValue(), type.toString());
		}
	}

	public static Test suite() {
		return new TestSuite(TypeMappingCorrectTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
