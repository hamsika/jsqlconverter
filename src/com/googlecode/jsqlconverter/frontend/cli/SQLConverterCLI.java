package com.googlecode.jsqlconverter.frontend.cli;

import com.googlecode.jsqlconverter.parser.Parser;
import com.googlecode.jsqlconverter.parser.DelimitedParser;
import com.googlecode.jsqlconverter.parser.JDBCParser;
import com.googlecode.jsqlconverter.parser.ParserException;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.producer.PostgreSQLProducer;
import com.googlecode.jsqlconverter.producer.Producer;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConverterCLI {
	private InputOperation inputOp = null;
	private OutputOperation outputOp = null;
	private ArrayList<String> argList;

	// jdbc params
	private String driver;
	private String url;
	private String user;
	private String pass;

	// delim / sql input
	private String file;
	private boolean hasHeaderRow = true;
	private char delimiter = ',';
	private char textQuantifier = '\0';

	private enum InputOperation {
		JDBC,
		DELIMITED
	}

	private enum OutputOperation {
		POSTGRESQL
	}

	public SQLConverterCLI(String[] args) throws ParserException, SQLException, FileNotFoundException, ClassNotFoundException {
		if (args.length == 0) {
			printUsage();
			System.exit(0);
		}

		argList = new ArrayList<String>(Arrays.asList(args));

		if (argList.contains("-h") || argList.contains("-help") || argList.contains("--help")) {
			printUsage();
			System.exit(0);
		}

		// detect operation
		if (argList.contains("-jdbc")) {
			setInputOperation(InputOperation.JDBC);
		}

		if (argList.contains("-delim")) {
			setInputOperation(InputOperation.DELIMITED);
		}


		if (inputOp == null) {
			printMessage("No input operation specified");
		}

		// detect parser options
		switch(inputOp) {
			case JDBC:
				driver = getRequiredParameter("-driver");
				url = getRequiredParameter("-url");
				user = getOptionalParameter("-user");
				pass = getOptionalParameter("-pass");
			break;
			case DELIMITED:
				setStdInOrFile();

				hasHeaderRow = !argList.contains("-noheader");

				String seperator = getOptionalParameter("-seperator");

				if (seperator != null) {
					if (seperator.length() == 1) {
						delimiter = seperator.charAt(0);
					} else {
						printMessage("Seperator must be a single character");
					}
				}


				String quantifier = getOptionalParameter("-quantifier");

				if (quantifier != null) {
					if (quantifier.length() == 1) {
						textQuantifier = quantifier.charAt(0);
					} else {
						printMessage("Text quantifier must be a single character");
					}
				}
			break;
		}

		// detect producer options
		if (argList.contains("-out-postgre")) {
			setOutputOperation(OutputOperation.POSTGRESQL);
		}

		if (outputOp == null) {
			printMessage("No output operation specified");
		}

		// run it! :)
		run();
	}

	private void setStdInOrFile() {
		file = getOptionalParameter("-file");
	}

	public String getRequiredParameter(String param) {		
		String value = getOptionalParameter(param);

		if (value == null) {
			printMessage("Missing required parameter");
		}

		return value;
	}

	public String getOptionalParameter(String param) {
		if (!argList.contains(param)) {
			return null;
		}

		int paramIndex = argList.indexOf(param);

		if (paramIndex + 1 >= argList.size()) {
			printMessage("Parameter value missing for " + param);
		}

		return argList.get(paramIndex + 1);
	}

	private void setInputOperation(InputOperation newOp) {
		if (inputOp != null) {
			printMessage("Multiple input operations found");
		}

		this.inputOp = newOp;
	}

	private void setOutputOperation(OutputOperation newOp) {
		if (outputOp != null) {
			printMessage("Multiple output operations found");
		}

		this.outputOp = newOp;
	}

	private void run() throws FileNotFoundException, SQLException, ClassNotFoundException, ParserException {
		// setup parser
		Parser parser = null;

		switch(inputOp) {
			case DELIMITED:
				if (file != null) {
					parser = new DelimitedParser(new File(file), delimiter, textQuantifier, hasHeaderRow);
				} else {
					parser = new DelimitedParser(new BufferedReader(new InputStreamReader(System.in)), new Name("unknown"), delimiter, textQuantifier, hasHeaderRow);
				}
			break;
			case JDBC:
				Class.forName(driver);
				Connection con = DriverManager.getConnection(url, user, pass);
				parser = new JDBCParser(con);
			break;
			default:
				printMessage("This input option hasn't been defined!");
				System.exit(0);
			break;
		}


		Statement[] statements = parser.parse();

		if (statements == null) {
			printMessage("Parser returned no results");
		}


		// setup producer
		Producer producer = null;

		switch(outputOp) {
			case POSTGRESQL:
				 producer = new PostgreSQLProducer();
			break;
			default:
				printMessage("This output option hasn't been defined!");
				System.exit(0);
			break;
		}

		producer.produce(statements);
	}

	private void printMessage(String message) {
		System.out.println(message);

		System.exit(0);
	}

	private void printUsage() {
		System.out.println(
			"jsqlparser <input options> <output options>\n" +

			"input options:\n" +
			"\t-jdbc -driver <driver> -url <url> [-user <username>] [-pass <password>]\n" +
			"\t-delim [ -file <filename> ] [-noheader] [-seperator <char>] [-quantifier <char>]\n" +
			"\n" +
			"output options:\n" +
			"\t-out-postgre"
		);
	}

	public static void main (String[] args) throws ParserException, SQLException, FileNotFoundException, ClassNotFoundException {
		new SQLConverterCLI(args);
	}
}
