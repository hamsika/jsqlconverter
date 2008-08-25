package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.definition.type.StringType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class DelimitedParser implements Parser {
	private BufferedReader in;
	private boolean hasHeaders;
	private char delimiter;
	private char textQuantifier = '\0'; // \0

	private String[] headers = null;
	private HashMap<Integer, Type> types = new HashMap<Integer, Type>();

	// hold a copy of the lines read to detect data types
	private ArrayList<String[]> lines = new ArrayList<String[]>();

	// TODO: must be full match, not just partial
	private Pattern floatPattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]{0,9}+");
	private Pattern doublePattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
	// real
	private Pattern intPattern = Pattern.compile("[-+]?[0-9]*");
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
		this(new BufferedReader(new InputStreamReader(new FileInputStream(file))), delimiter, textQuantifier, hasHeaders);
	}

	public DelimitedParser(BufferedReader in, char delimiter, boolean hasHeaders) {
		this(in, delimiter, '\0', hasHeaders);
	}

	public DelimitedParser(BufferedReader in, char delimiter, char textQuantifier, boolean hasHeaders) {
		this.in = in;
		this.delimiter = delimiter;
		this.textQuantifier = textQuantifier;
		this.hasHeaders = hasHeaders;
	}

	public Statement[] parse() throws ParserException {
		try {
			String line;
			int lineNumber = 1;

			detectDataTypes();

			// TODO: make CreateTable statement
			if (headers == null) {
				// TODO: generate fake header names
				// and if this is the case, then when creating the insert from values, don't include headers
			}

			// loop lines with detected types

			// loop rest of data in file

			// TODO: create InsertFromValues statements

			if (hasHeaders) {
				// create some default headers here
				// make create table statement
			}
		} catch (IOException ioe) {
			throw new ParserException(ioe.getMessage(), ioe.getCause());
		}

		return null;
	}

	private void detectDataTypes() throws IOException {
		String line;
		int lineNumber = 1;

		while ((line = in.readLine()) != null) {
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
				types.put(i, StringType.TEXT);
			}

			// TODO: find out correct types

			++lineNumber;
		}
	}

	private String[] getColumns(String line) {
		System.out.println(line);

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
						System.out.println("Expected delimiter on next char.. not found");
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
			System.out.println("col " + (i + 1) + ": " + columns.get(i));
		}

		System.out.println("-----------");

		return columns.toArray(new String[] {});
	}
}
