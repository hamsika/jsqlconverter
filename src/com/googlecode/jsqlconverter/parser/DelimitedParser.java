package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.type.*;

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

	private String[] headers = null;
	private HashMap<Integer, Type> types = new HashMap<Integer, Type>();

	// hold a copy of the lines read to detect data types
	private ArrayList<String[]> lines = new ArrayList<String[]>();

	// TODO: must be full match, not just partial
	private Pattern floatPattern = Pattern.compile("[-+]?[0-9]*\\.[0-9]{0,9}+");
	private Pattern doublePattern = Pattern.compile("[-+]?[0-9]*\\.[0-9]+");
	// real
	private Pattern integerPattern = Pattern.compile("[-+]?[0-9]*");

	// BIGINT,		/* 8 byte -9223372036854775808 to 9223372036854775807 */
	//INTEGER,	/* 4 byte -2147483648 to 2147483647 */
	//MEDIUMINT,	/* 3 byte -8388608 to 8388607 */
	//NUMERIC,
	//SMALLINT,	/* 2 byte -32768 to 32767 */
	//TINYINT		/* 1 byte -128 to 127 */


	public DelimitedParser(File file, char delimiter, boolean hasHeaders) throws FileNotFoundException {
		this(file, delimiter, '\0', hasHeaders);
	}

	public DelimitedParser(File file, char delimiter, char textQuantifier, boolean hasHeaders) throws FileNotFoundException {
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
	}

	public DelimitedParser(BufferedReader in, Name tableName, char delimiter, boolean hasHeaders) {
		this(in, tableName, delimiter, '\0', hasHeaders);
	}

	public DelimitedParser(BufferedReader in, Name tableName, char delimiter, char textQuantifier, boolean hasHeaders) {
		this.in = in;
		this.tableName = tableName;
		this.delimiter = delimiter;
		this.textQuantifier = textQuantifier;
		this.hasHeaders = hasHeaders;
	}

	public Statement[] parse() throws ParserException {
		try {
			String line;
			int lineNumber = 1;

			detectDataTypes();

			if (headers != null) {
				// TODO: make CreateTable statement

				// lol
			}

			ArrayList<InsertFromValues> inserts = new ArrayList<InsertFromValues>();

			// loop lines with detected types
			for (String[] columns : lines) {
				InsertFromValues insert;

				if (headers == null) {
					insert = new InsertFromValues(tableName);
				} else {
					insert = new InsertFromValues(tableName, null);
				}

				for (int i=0; i<columns.length; i++) {
					if (types.get(i) instanceof NumericType) {
						//insert.setNumeric(i, columns[i]);
					} else {
						insert.setString(i, columns[i]);
					}
				}

				inserts.add(insert);
			}

			// loop rest of data in file

			// TODO: create InsertFromValues statements

			if (hasHeaders) {
				// create some default headers here
				// make create table statement
			}

			return inserts.toArray(new InsertFromValues[] {});
		} catch (IOException ioe) {
			throw new ParserException(ioe.getMessage(), ioe.getCause());
		}
	}

	private void detectDataTypes() throws IOException {
		String line;
		int lineNumber = 0;

		while ((line = in.readLine()) != null) {
			++lineNumber;

			String columns[] = getColumns(line);

			if (lineNumber == 1 && hasHeaders) {
				headers = columns;
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

				Matcher floatMatcher = floatPattern.matcher(columns[i]);
				Matcher doubleMatcher = doublePattern.matcher(columns[i]);
				Matcher integerMatcher = integerPattern.matcher(columns[i]);

				if (floatMatcher.matches()) {
					//System.out.println("is float: " + columns[i]);
					setColumnType(i, ApproximateNumericType.FLOAT);
				} else if (doubleMatcher.matches()) {
					//System.out.println("is double: " + columns[i]);
					setColumnType(i, ApproximateNumericType.DOUBLE);
				} else if (integerMatcher.matches()) {
					//System.out.println("is int: " + columns[i]);
					// TODO: based on length set which int to use (bigint, smallint, etc)
					setColumnType(i, ExactNumericType.INTEGER);
				} else {
					setColumnType(i, StringType.VARCHAR);
					//System.out.println("unknown column type: " + columns[i]);
				}

				// TODO: if all values are same length and TEXT set to CHAR

				setColumnType(i, StringType.TEXT);
			}

			// TODO: find out correct types
		}
	}

	private void setColumnType(int column, Type type) {
		// TODO: take weakest type
		// e.g.: double trumps float
		// e.g: text trumps all
		types.put(column, type);
	}

	private String[] getColumns(String line) {
		//System.out.println(line);

		if (textQuantifier == '\0') {
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
						log.log(Level.WARNING, "Expected delimiter on next char.. not found, skipping row");
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
