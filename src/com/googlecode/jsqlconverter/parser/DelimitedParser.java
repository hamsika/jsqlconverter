package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;
import com.googlecode.jsqlconverter.logging.LogLevel;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DelimitedParser extends Parser {
	private BufferedReader in;
	private Name tableName;
	private boolean hasHeaders;
	private char delimiter;
	private char textQuantifier = '\0';
	private int maxLineReads = 10;

	private Column[] headers = null;

	// hold a copy of the lines read to detect data types
	private ArrayList<String[]> lines = new ArrayList<String[]>();

	// TODO: must be full match, not just partial
	private Pattern floatPattern = Pattern.compile("[-+]?[0-9]*\\.[0-9]{0,9}+");
	private Pattern doublePattern = Pattern.compile("[-+]?[0-9]*\\.[0-9]+");
	// real
	private Pattern integerPattern = Pattern.compile("[-+]?[0-9]*");
	private boolean convertDataToInsert;

	// BIGINT,		/* 8 byte -9223372036854775808 to 9223372036854775807 */
	//INTEGER,	/* 4 byte -2147483648 to 2147483647 */
	//MEDIUMINT,	/* 3 byte -8388608 to 8388607 */
	//NUMERIC,
	//SMALLINT,	/* 2 byte -32768 to 32767 */
	//TINYINT		/* 1 byte -128 to 127 */


	public DelimitedParser(File file, char delimiter, boolean hasHeaders) throws FileNotFoundException {
		this(file, delimiter, '\0', hasHeaders, false);
	}

	public DelimitedParser(File file, char delimiter, char textQuantifier, boolean hasHeaders, boolean convertDataToInsert) throws FileNotFoundException {
		String filename = file.getName();

		int dotIndex = filename.indexOf('.');

		if (dotIndex != -1) {
			filename = filename.substring(0, dotIndex);
		}

		this.in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		this.tableName = new Name(filename);
		this.delimiter = delimiter;
		this.textQuantifier = textQuantifier;
		this.hasHeaders = hasHeaders;
		this.convertDataToInsert = convertDataToInsert;
	}

	public DelimitedParser(BufferedReader in, Name tableName, char delimiter, boolean hasHeaders, boolean convertDataToInsert) {
		this(in, tableName, delimiter, '\0', hasHeaders, false);
	}

	public DelimitedParser(BufferedReader in, Name tableName, char delimiter, char textQuantifier, boolean hasHeaders, boolean convertDataToInsert) {
		this.in = in;
		this.tableName = tableName;
		this.delimiter = delimiter;
		this.textQuantifier = textQuantifier;
		this.hasHeaders = hasHeaders;
		this.convertDataToInsert = convertDataToInsert;
	}

	public void parse(ParserCallback callback) throws ParserException {
		try {
			// only 'detect' data types if headers are available
			if (hasHeaders) {
				detectDataTypes();
			}

			if (headers != null) {
				CreateTable table = new CreateTable(tableName);

				for (Column column : headers) {
					table.addColumn(column);
				}

				callback.produceStatement(table);
			}

			// if user hasn't requested data be converted to inserts then finish here. 
			if (!convertDataToInsert) {
				lines.clear();
				return;
			}

			// loop lines with detected types
			for (String[] columns : lines) {
				InsertFromValues insert = getInsertFromValues(columns);
				callback.produceStatement(insert);
			}

			// clear memory lines is using and loop through rest of the input
			lines.clear();

			String line;

			while ((line = in.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}

				callback.produceStatement(getInsertFromValues(getColumns(line)));
			}
		} catch (IOException ioe) {
			throw new ParserException(ioe.getMessage(), ioe.getCause());
		}
	}

	private InsertFromValues getInsertFromValues(String[] columns) {
		InsertFromValues insert;

		if (headers == null) {
			insert = new InsertFromValues(tableName);
		} else {
			insert = new InsertFromValues(tableName, headers);
		}

		for (int i=0; i<columns.length; i++) {
			insert.setValue(i, columns[i]);
		}

		return insert;
	}

	private void detectDataTypes() throws IOException {
		// TODO: set column size
		String line;
		int lineNumber = 0;

		String[] headerNames = new String[] {};
		HashMap<Integer, Type> types = new HashMap<Integer, Type>();

		while ((line = in.readLine()) != null) {
			if (line.trim().isEmpty()) {
				continue;
			}

			++lineNumber;

			String columns[] = getColumns(line);

			if (lineNumber == 1 && hasHeaders) {
				headerNames = columns;
				continue;
			}

			lines.add(columns);

			for (int i=0; i<columns.length; i++) {
				// text, number (INT, FLOAT, DOUBLE), date, time, date+time, currency, percentage, fraction
				// float / double:	[-+]?[0-9]*\.?[0-9]+
				// int:				\d+
				// currency:		.
				// time:			.
				// date:			.
				// datetime:		.

				// if column value is null then don't detect data type
				if (columns[i].equals("null")) {
					continue;
				}

				Matcher floatMatcher = floatPattern.matcher(columns[i]);
				Matcher doubleMatcher = doublePattern.matcher(columns[i]);
				Matcher integerMatcher = integerPattern.matcher(columns[i]);

				Type type;

				if (floatMatcher.matches()) {
					//System.out.println("is float: " + columns[i]);
					type = getColumnType(types.get(i), ApproximateNumericType.FLOAT);
				} else if (doubleMatcher.matches()) {
					//System.out.println("is double: " + columns[i]);
					type = getColumnType(types.get(i), ApproximateNumericType.DOUBLE);
				} else if (integerMatcher.matches()) {
					//System.out.println("is int: " + columns[i]);
					// TODO: based on length set which int to use (bigint, smallint, etc)
					type = getColumnType(types.get(i), ExactNumericType.INTEGER);
				} else {
					type = getColumnType(types.get(i), StringType.VARCHAR);
					//System.out.println("unknown column type: " + columns[i]);
				}

				types.put(i, type);
			}

			// TODO: if all values are same length and TEXT set to CHAR
			// TODO: find out correct types

			if (lineNumber > maxLineReads) {
				break;
			}
		}

		// create column objects
		ArrayList<Column> columnList = new ArrayList<Column>();

		// TODO: create some fake header names if headers were not provided

		for (int i=0; i<headerNames.length; i++) {
			if (types.get(i) == null) {
				log.logApp(LogLevel.WARNING, "Datatype for column #" + (i + 1) + " was not detected");
				types.put(i, StringType.VARCHAR);
			}

			Column column = new Column(new Name(headerNames[i]), types.get(i));

			columnList.add(column);
		}

		headers = columnList.toArray(new Column[] {});
	}

	private Type getColumnType(Type currentType, Type type) {
		// TODO: take weakest type
		// e.g.: double trumps float
		// e.g: text trumps all

		//types.put(column, type);

		if (currentType == null) {
			// decide what to do
		}

		return type;
	}

	// TODO: make sure this returns an array the same size of headers[]
	private String[] getColumns(String line) {
		// if there's no textQuantifier set it means that all columns are seperated by delimiter
		if (textQuantifier == '\0') {
			// note: if a line has empty columns they will not be included
			return line.split(String.valueOf(delimiter));
		}

		boolean hasStartQuantifier = false;

		char[] lineChars = line.toCharArray();

		StringBuffer columnBuffer = new StringBuffer();

		ArrayList<String> columns = new ArrayList<String>();

		for (int i=0; i<lineChars.length; i++) {
			if (lineChars[i] == textQuantifier) {
				if (hasStartQuantifier) {
					// end (if it's not double quantifier)
					if (lineChars[i + 1] == textQuantifier) {
						++i;
						columnBuffer.append(lineChars[i]);
						continue;
					}

					// TODO: next char must either be nothing, or delimiter
					if (lineChars[i + 1] != delimiter) {
						log.logApp(Level.WARNING, "Expected delimiter on next char.. not found, skipping row");
						continue;
					}

					++i;

					hasStartQuantifier = false;

					columns.add(columnBuffer.toString());

					columnBuffer = new StringBuffer();
				} else {
					hasStartQuantifier = true;
				}
			} else if (lineChars[i] == delimiter) {
				if (hasStartQuantifier) {
					//System.out.println("adding: " + columnBuffer);
					columnBuffer.append(lineChars[i]);
				} else {
					// this must be end of column
					columns.add(columnBuffer.toString());
					columnBuffer = new StringBuffer();
				}
			} else {
				//System.out.println("adding: " + columnBuffer);
				columnBuffer.append(lineChars[i]);
			}
		}

		columns.add(columnBuffer.toString());

		for (int i=0; i<columns.size(); i++) {
			log.fine("col " + (i + 1) + ": " + columns.get(i));
		}

		log.fine("-----------");

		return columns.toArray(new String[] {});
	}
}
