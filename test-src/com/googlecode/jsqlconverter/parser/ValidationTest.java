package com.googlecode.jsqlconverter.parser;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;

import com.googlecode.jsqlconverter.testutils.TestProperties;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;

public class ValidationTest extends TestCase {
	private ArrayList<Parser> parsers = new ArrayList<Parser>();
	// TODO: create a test that will validate the Statement.
	// this is to ensure the parser is creating valid statements

	protected void setUp() throws FileNotFoundException {
		if (TestProperties.getBoolean("validation.access.enabled")) {
			System.out.println("ValidationTest adding Access MDB");

			parsers.add(new AccessMDBParser(new File(TestProperties.getProperty("validation.access.mdb")), true));
		}

		if (TestProperties.getBoolean("validation.delimited.enabled")) {
			System.out.println("ValidationTest adding Delimited");

			parsers.add(
				new DelimitedParser(
					new File(TestProperties.getProperty("validation.delimited.file")),
					TestProperties.getChar("validation.delimited.delimiter"),
					TestProperties.getChar("validation.delimited.quantifer"),
					TestProperties.getBoolean("validation.delimited.has_headers"),
					true
				)
			);
		}

		if (TestProperties.getBoolean("validation.jdbc.enabled")) {
			System.out.println("ValidationTest adding JDBC");

			parsers.add(new JDBCParser(null));
		}

		if (TestProperties.getBoolean("validation.sqlfairy.enabled")) {
			System.out.println("ValidationTest adding SQL Fairy");

			parsers.add(new SQLFairyXMLParser(new FileInputStream(TestProperties.getProperty("validation.sqlfairy.file"))));
		}

		if (TestProperties.getBoolean("validation.turbine.enabled")) {
			System.out.println("ValidationTest adding Turbine");

			parsers.add(new TurbineXMLParser(new FileInputStream(TestProperties.getProperty("validation.turbine.file"))));
		}
	}

	public void testParsers() {
		for (Parser parser : parsers) {
			try {
				parser.parse(new ValidationParserCallback(parser.getClass().getName()));
			} catch (ParserException e) {
				fail(parser.getClass().getName() + " failed: " + e.getMessage());
			}
		}
	}

	private class ValidationParserCallback implements ParserCallback {
		private String parserName;

		public ValidationParserCallback(String parserName) {
			this.parserName = parserName;
		}

		public void produceStatement(Statement statement) {
			if (statement instanceof CreateIndex) {
				validateCreatexIndex((CreateIndex)statement);
			} else if (statement instanceof CreateTable) {
				validateCreateTable((CreateTable)statement);
			} else if (statement instanceof InsertFromValues) {
				validateInsertFromValues((InsertFromValues)statement);
			} else {
				fail("unknown statement type " + statement);
			}
		}

		private void validateCreatexIndex(CreateIndex createIndex) {
			//fail("unimplemented for " + parserName);
			// TODO: do some checks heres
		}

		private void validateCreateTable(CreateTable table) {
			if (table.getColumnCount() == 0) {
				fail(parserName + " no columns defined");
			}

			if (table.getName().getDatabaseName() != null) {
				fail(parserName + " table name should not be database qualified");
			}

			KeyConstraint primaryKey = table.getPrimaryCompoundKeyConstraint();
			KeyConstraint[] uniqueKeys = table.getUniqueCompoundKeyConstraint();

			int primaryKeyCount = 0;

			for (Column column : table.getColumns()) {
				if (column.getName().getDatabaseName() != null || column.getName().getSchemaName() != null) {
					fail(parserName + " column name should not be database or schema qualified");
				}

				if (column.containsOption(ColumnOption.NULL) && column.containsOption(ColumnOption.NOT_NULL)) {
					fail(parserName + " NULL and NOT_NULL column options used together");
				}

				if (column.containsOption(ColumnOption.PRIMARY_KEY)) {
					++primaryKeyCount;

					if (column.containsOption(ColumnOption.UNIQUE)) {
						fail(parserName + " PRIMARY_KEY and UNIQUE column options used together");
					}
				}

				if (primaryKey != null) {
					Name[] pkeyCols = primaryKey.getColumns();

					for (Name pkeycol : pkeyCols) {
						if (pkeycol.getObjectName().equals(column.getName().getObjectName())) {
							if (column.containsOption(ColumnOption.PRIMARY_KEY) || column.containsOption(ColumnOption.UNIQUE)) {
								fail(parserName + " column options conflict with primary compound key constraint");
							}

							return;
						}
					}
				}

				// TODO: check column type and ColumnOptions
			}

			if (primaryKeyCount > 1) {
				fail(parserName + " table has " + primaryKeyCount + " PRIMARY KEY columns defined, only allowed 1");
			}

			for (KeyConstraint ukc : uniqueKeys) {
				if (ukc.getColumns().length <= 1) {
					fail(parserName + " unique compound key must have at least 2 columns defined");
				}
			}
		}

		private void validateInsertFromValues(InsertFromValues insert) {
			//fail("unimplemented for " + parserName);
			// TODO: do some checks heres
			// should have same number of columns as values!
			//assertNotNull("Incorrect column and value count", insert.getObject(insert.getColumnCount() - 1));
		}

		public void log(String message) {
			System.err.println(parserName + " " + message);
		}
	}

	public static Test suite() {
		return new TestSuite(ValidationTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
