package com.googlecode.jsqlconverter.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ParameterOptional;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.type.ApproximateNumericType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.definition.type.NumericType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;

@ServiceName("Delimited")
public class DelimitedParser extends Parser {
	private BufferedReader in;
	private Name tableName;
	private boolean hasHeaders;
	private char delimiter;
	private char textQuantifier = '\0';
	private int maxLineReads = 20;
	private Column[] headers = null;
	private char nextChar = '\0';

	// hold a copy of the lines read to detect data types
	private ArrayList<String[]> lines = new ArrayList<String[]>();

	// TODO: must be full match, not just partial
	private Pattern floatPattern = Pattern.compile("[-+]?[0-9]*\\.[0-9]{0,9}+");
	private Pattern doublePattern = Pattern.compile("[-+]?[0-9]*\\.[0-9]+");
	// real
	private Pattern integerPattern = Pattern.compile("[-+]?[0-9]+");
	private boolean convertDataToInsert;

	public DelimitedParser(
		@ParameterName("File") File file,
		@ParameterName("Delimiter") @ParameterOptional(defaultValue = ",") Character delimiter,
		@ParameterName("Quantifier") @ParameterOptional(defaultValue = "\"") Character textQuantifier,
		@ParameterName("Has Header") @ParameterOptional(defaultValue = "true") Boolean hasHeaders,
		@ParameterName("Data") @ParameterOptional(defaultValue = "false") Boolean convertDataToInsert
	) throws FileNotFoundException {
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

	@Override
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

			String values[];

			while ((values = getColumns(callback)) != null) {
				callback.produceStatement(getInsertFromValues(values));
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
			if (columns[i].isEmpty() || columns[i].equals("null")) {
				insert.setValue(i, null);
			} else {
				insert.setValue(i, columns[i]);
			}
		}

		return insert;
	}

	private void detectDataTypes(ParserCallback callback) throws IOException {
		int lineNumber = 0;

		String[] headerNames = new String[] {};
		HashMap<Integer, Type> types = new HashMap<Integer, Type>();
		HashMap<Integer, Integer> sizes = new HashMap<Integer, Integer>();
		String[] values;

		while ((values = getColumns(callback)) != null) {
			++lineNumber;

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
				} else if (values[i].isEmpty()) {
					continue;
				}

				Matcher floatMatcher = floatPattern.matcher(values[i]);
				Matcher doubleMatcher = doublePattern.matcher(values[i]);
				Matcher integerMatcher = integerPattern.matcher(values[i]);

				Type type;

				if (floatMatcher.matches()) {
					//LOG.finer("is float: " + values[i]);
					type = getColumnType(types.get(i), ApproximateNumericType.FLOAT);
				} else if (doubleMatcher.matches()) {
					//LOG.finer("is double: " + values[i]);
					type = getColumnType(types.get(i), ApproximateNumericType.DOUBLE);
				} else if (integerMatcher.matches()) {
					//LOG.finer("is int: " + values[i]);
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
					//LOG.finer("unknown column type for value: " + values[i]);
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

		headers = columnList.toArray(new Column[columnList.size()]);
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

	private String[] getColumns(ParserCallback callback) throws IOException {
		if (textQuantifier == '\0') {
			// note: if a line has empty columns they will not be included
			String line = in.readLine();

			if (line == null) {
				return null;
			}

			return line.trim().split(String.valueOf(delimiter));
		}

		int currentInt;
		char currentChar;
		boolean hasStartQuantifier = false;
		StringBuffer columnBuffer = new StringBuffer();
		ArrayList<String> columns = new ArrayList<String>();

		while ((currentInt = readChar()) != -1) {
			currentChar = (char)currentInt;

			if (currentChar == textQuantifier) {
				if (nextChar() == textQuantifier) {
					columnBuffer.append(currentChar);
					nextChar = '\0';
				} else if (hasStartQuantifier) {
					LOG.finer("End Text quantifier");

					hasStartQuantifier = false;
				} else {
					LOG.finer("Start Text quantifier");

					hasStartQuantifier = true;
				}
			} else if (!hasStartQuantifier) {
				if (currentChar == delimiter) {
					LOG.finer("Delimiter: " + columnBuffer);
					columns.add(columnBuffer.toString());
					columnBuffer = new StringBuffer();
				} else if (currentChar == '\r' || currentChar == '\n') {
					if (hasStartQuantifier) {
						// must go onto another line! :)
						LOG.finer("Found quantifier that goes onto another line");
						continue;
					} else {
						LOG.finer("Found end of line: " + columnBuffer);

						if (nextChar() != '\r' && nextChar() != '\n') {
							columns.add(columnBuffer.toString());
							columnBuffer = new StringBuffer();

							break;
						}
					}
				} else {
					columnBuffer.append(currentChar);
				}
			} else {
				columnBuffer.append(currentChar);
			}
		}

		if (currentInt == -1) {
			return null;
		}

		for (int i=0; i<columns.size(); i++) {
			LOG.finer("col " + (i + 1) + ": " + columns.get(i));
		}

		LOG.finer("Returning: " + columns.size());

		return columns.toArray(new String[columns.size()]);
	}

	private int readChar() throws IOException {
		if (nextChar != '\0') {
			char tempChar = nextChar;
			nextChar = '\0';

			return tempChar;
		}

		return in.read();
	}

	private char nextChar() throws IOException {
		if (nextChar != '\0') {
			return nextChar;
		}

		nextChar = (char) in.read();

		return nextChar;
	}
}
