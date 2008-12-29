package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DelimitedParser extends Parser {
	private BufferedReader in;
	private Name tableName;
	private boolean hasHeaders;
	private char delimiter;
	private char textQuantifier = '\0';
	private int maxLineReads = 50;
	private Column[] headers = null;

	// hold a copy of the lines read to detect data types
	private ArrayList<String[]> lines = new ArrayList<String[]>();

	// TODO: must be full match, not just partial
	private Pattern floatPattern = Pattern.compile("[-+]?[0-9]*\\.[0-9]{0,9}+");
	private Pattern doublePattern = Pattern.compile("[-+]?[0-9]*\\.[0-9]+");
	// real
	private Pattern integerPattern = Pattern.compile("[-+]?[0-9]+");
	private boolean convertDataToInsert;


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
		this(in, tableName, delimiter, '\0', hasHeaders, convertDataToInsert);
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
				detectDataTypes(callback);
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

				callback.produceStatement(getInsertFromValues(getColumns(callback, line)));
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
			if (columns[i].equals("") || columns[i].equals("null")) {
				insert.setValue(i, null);
			} else {
				insert.setValue(i, columns[i]);
			}
		}

		return insert;
	}

	private void detectDataTypes(ParserCallback callback) throws IOException {
		String line;
		int lineNumber = 0;

		String[] headerNames = new String[] {};
		HashMap<Integer, Type> types = new HashMap<Integer, Type>();
		HashMap<Integer, Integer> sizes = new HashMap<Integer, Integer>();

		while ((line = in.readLine()) != null) {
			if (line.trim().isEmpty()) {
				continue;
			}

			++lineNumber;

			String values[] = getColumns(callback, line);

			// if this is the first line and we are reading headers then set this row as headers
			if (lineNumber == 1 && hasHeaders) {
				headerNames = values;
				continue;
			}

			lines.add(values);

			for (int i=0; i< values.length; i++) {
				// text, number (INT, FLOAT, DOUBLE), date, time, date+time, currency, percentage, fraction
				// float / double:	[-+]?[0-9]*\.?[0-9]+
				// int:				\d+
				// currency:		.
				// time:			.
				// date:			.
				// datetime:		.

				// if column value is null then don't detect data type
				if (values[i].equals("null")) {
					continue;
				} else if (values[i].equals("")) {
					continue;
				}

				Matcher floatMatcher = floatPattern.matcher(values[i]);
				Matcher doubleMatcher = doublePattern.matcher(values[i]);
				Matcher integerMatcher = integerPattern.matcher(values[i]);

				Type type;

				if (floatMatcher.matches()) {
					//System.out.println("is float: " + values[i]);
					type = getColumnType(types.get(i), ApproximateNumericType.FLOAT);
				} else if (doubleMatcher.matches()) {
					//System.out.println("is double: " + values[i]);
					type = getColumnType(types.get(i), ApproximateNumericType.DOUBLE);
				} else if (integerMatcher.matches()) {
					//System.out.println("is int: " + values[i]);
					try {
						long value = Long.parseLong(integerMatcher.group(0));

						if (value >= -128 && value <= 127) {
							type = getColumnType(types.get(i), ExactNumericType.TINYINT);
						} else if (value >= -32768 && value <= 32767) {
							type = getColumnType(types.get(i), ExactNumericType.SMALLINT);
						} else if (value >= -8388608 && value <= 8388607) {
							type = getColumnType(types.get(i), ExactNumericType.MEDIUMINT);
						} else if (value >= -2147483648 && value <= 2147483647) {
							type = getColumnType(types.get(i), ExactNumericType.INTEGER);
						} else {
							type = getColumnType(types.get(i), ExactNumericType.BIGINT);
						}
					} catch (NumberFormatException nfe) {
						type = getColumnType(types.get(i), ExactNumericType.BIGINT);
					}
				} else {
					//System.out.println("unknown column type for value: " + values[i]);
					type = getColumnType(types.get(i), StringType.VARCHAR);
				}

				types.put(i, type);

				// check sizes
				if (sizes.get(i) == null) {
					sizes.put(i, 0);
				}

				if (values[i].length() > sizes.get(i)) {
					sizes.put(i, values[i].length());
				}
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
				callback.log("Datatype for column #" + (i + 1) + " was not detected");

				types.put(i, StringType.VARCHAR);
				sizes.put(i, 255);
			}

			Column column = new Column(new Name(headerNames[i]), types.get(i));

			if (!(column.getType() instanceof NumericType)) {
				column.setSize(sizes.get(i));
			}

			columnList.add(column);
		}

		headers = columnList.toArray(new Column[] {});
	}

	private Type getColumnType(Type currentType, Type type) {
		// this is the first type we've found, so use whatever this is
		if (currentType == null) {
			return type;
		}

		if (getTypeRating(type) > getTypeRating(currentType)) {
			return type;
		} else {
			return currentType;
		}
	}

	private int getTypeRating(Type type) {
		// TODO: complete this. weakest type should be taken
		// e.g.: double trumps float
		// e.g: text trumps all

		/*} else if (type instanceof BinaryType) {
			dataTypeString = getType((BinaryType)type);*/
		/*} else if (type instanceof BooleanType) {
			dataTypeString = getType((BooleanType)type);*/
		/*} else if (type instanceof DateTimeType) {
			dataTypeString = getType((DateTimeType)type);
		} else if (type instanceof DecimalType) {
			dataTypeString = getType((DecimalType)type);*/
		if (type instanceof StringType) {
			switch((StringType)type) {
				case VARCHAR:
					return 9;
			}
		} else if (type instanceof ApproximateNumericType) {
			switch((ApproximateNumericType)type) {
				case DOUBLE:
					return 8;
				case FLOAT:
					return 7;
				case REAL:
					return 6;
			}
		} else if (type instanceof ExactNumericType) {
			switch((ExactNumericType)type) {
				case BIGINT:
					return 5;
				case INTEGER:
					return 4;
				case MEDIUMINT:
					return 3;
				case SMALLINT:
					return 2;
				case TINYINT:
					return 1;
			}
		/*} else if (type instanceof MonetaryType) {
			switch((MonetaryType)type) {
				case MONEY:

				case SMALLMONEY:
			}*/
		}

		return 0;
	}

	// TODO: make sure this returns an array the same size of headers[]
	private String[] getColumns(ParserCallback callback, String line) {
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

					// it's possible this could be the end of the line so no more delims will be present
					if (lineChars[i + 1] != delimiter) {
						callback.log("Expected delimiter on next char.. not found, skipping row");
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
