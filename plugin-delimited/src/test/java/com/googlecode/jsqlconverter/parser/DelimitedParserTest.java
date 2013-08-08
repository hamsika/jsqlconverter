package com.googlecode.jsqlconverter.parser;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;

public class DelimitedParserTest {
	@Test
	public void testTextQuantifier() throws FileNotFoundException, ParserException {
		DelimitedParser parser = new DelimitedParser(new File(DelimitedParserTest.class.getResource("/data/delimited/textquantifier.csv").getFile()), ',', '"', true, true);

		CreateTable correctTable = new CreateTable(new Name("textquantifier"));

		correctTable.addColumn(new Column(new Name("column \" a"), StringType.VARCHAR));
		correctTable.addColumn(new Column(new Name("[second col]"), StringType.VARCHAR));
		correctTable.addColumn(new Column(new Name("3rd, title"), StringType.VARCHAR));
		correctTable.addColumn(new Column(new Name("\"this is column, ' four\""), StringType.VARCHAR));
		correctTable.addColumn(new Column(new Name("column,\" five"), StringType.VARCHAR));
		correctTable.addColumn(new Column(new Name("sixth"), StringType.VARCHAR));
		correctTable.addColumn(new Column(new Name(", seventh"), StringType.VARCHAR));
		correctTable.addColumn(new Column(new Name("eigth"), StringType.VARCHAR));

		parser.parse(new TextQuantifierCallback(correctTable));
	}

	@Test
	public void testMultiline() throws FileNotFoundException, ParserException {
		DelimitedParser parser = new DelimitedParser(new File(DelimitedParserTest.class.getResource("/data/delimited/multiline.csv").getFile()), ',', '"', true, true);

		CreateTable correctTable = new CreateTable(new Name("multiline"));
		correctTable.addColumn(new Column(new Name("name"), StringType.VARCHAR));
		correctTable.addColumn(new Column(new Name("date"), StringType.VARCHAR));
		correctTable.addColumn(new Column(new Name("numbers"), StringType.VARCHAR));
		correctTable.addColumn(new Column(new Name("types"), StringType.VARCHAR));

		parser.parse(new TextQuantifierCallback(correctTable));
	}

	private class TextQuantifierCallback implements ParserCallback {
		private CreateTable correctTable;

		public TextQuantifierCallback(CreateTable correctTable) {
			this.correctTable = correctTable;
		}

		@Override
		public void produceStatement(Statement statement) {
			if (!(statement instanceof CreateTable)) {
				return;
			}

			CreateTable ct = (CreateTable) statement;

			assertEquals(ct.getName().getObjectName(), correctTable.getName().getObjectName());

			Column[] columns = ct.getColumns();
			Column[] correctColumns = correctTable.getColumns();

			assertEquals(correctColumns.length, columns.length);

			for (int i=0; i<columns.length; i++) {
				assertEquals("column " + columns[i].getName().getObjectName(), correctColumns[i].getName().getObjectName(), columns[i].getName().getObjectName());
				assertEquals("column " + columns[i].getName().getObjectName(), correctColumns[i].getType(), columns[i].getType());
				//assertEquals(columns[i].getSize(), correctColumns[i].getSize());
			}
		}

		@Override
		public void log(String message) {
			System.err.println(message);
		}
	}
}
