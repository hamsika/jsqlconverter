package com.googlecode.jsqlconverter.frontend.cli;

import com.googlecode.jsqlconverter.parser.*;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.producer.*;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConverterCLI implements ParserCallback {
	private InputOperation inputOp = null;
	private OutputOperation outputOp = null;
	private ArrayList<String> argList;

	// input params
	// delim
	private String file; // this is also used by AccessMDBParser
	private boolean hasHeaderRow = true;
	private char delimiter = ',';
	private char textQuantifier = '\0';
	private Producer producer;

	// jdbc params
	private String driver;
	private String url;
	private String user;
	private String pass;
	private boolean doData = false;

	// output params
	// access mdb
	private String outputFile;
	private PrintStream out = System.out;

	private enum InputOperation {
		MSACCESS_MDB,
		DELIMITED,
		JDBC,
		TURBINE_XML
	}

	private enum OutputOperation {
		MSACCESS_MDB,
		MSACCESS_SQL,
		POSTGRESQL
	}

	// TODO: check if unknown / unneeded params were used
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
		if (argList.contains("-access-mdb")) {
			setInputOperation(InputOperation.MSACCESS_MDB);
		}

		if (argList.contains("-delim")) {
			setInputOperation(InputOperation.DELIMITED);
		}

		if (argList.contains("-jdbc")) {
			setInputOperation(InputOperation.JDBC);
		}

		if (argList.contains("-turbine")) {
			setInputOperation(InputOperation.TURBINE_XML);
		}

		if (inputOp == null) {
			exitMessage("No input operation specified");
		}

		// detect parser options
		switch(inputOp) {
			case MSACCESS_MDB:
				file = getRequiredParameter("-file");
			break;
			case DELIMITED:
				setStdInOrFile();

				hasHeaderRow = !argList.contains("-noheader");

				String seperator = getOptionalParameter("-seperator");

				if (seperator != null) {
					if (seperator.length() == 1) {
						delimiter = seperator.charAt(0);
					} else {
						exitMessage("Seperator must be a single character");
					}
				}


				String quantifier = getOptionalParameter("-quantifier");

				if (quantifier != null) {
					if (quantifier.length() == 1) {
						textQuantifier = quantifier.charAt(0);
					} else {
						exitMessage("Text quantifier must be a single character");
					}
				}
			break;
			case JDBC:
				driver = getRequiredParameter("-driver");
				url = getRequiredParameter("-url");
				user = getOptionalParameter("-user");
				pass = getOptionalParameter("-pass");
			break;
			case TURBINE_XML:
				file = getRequiredParameter("-file");
			break;
		}

		// detect global optional params
		doData = argList.contains("-data");

		// detect producer options
		if (argList.contains("-out-access-mdb")) {
			setOutputOperation(OutputOperation.MSACCESS_MDB);
		}

		if (argList.contains("-out-access")) {
			setOutputOperation(OutputOperation.MSACCESS_SQL);
		}

		if (argList.contains("-out-postgre")) {
			setOutputOperation(OutputOperation.POSTGRESQL);
		}

		if (outputOp == null) {
			exitMessage("No output operation specified");
		}

		// detect parser options
		switch(outputOp) {
			case MSACCESS_MDB:
				outputFile = getRequiredParameter("-ofile");
			break;
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
			exitMessage("Missing required parameter: " + param);
		}

		return value;
	}

	public String getOptionalParameter(String param) {
		if (!argList.contains(param)) {
			return null;
		}

		int paramIndex = argList.indexOf(param);

		if (paramIndex + 1 >= argList.size()) {
			exitMessage("Parameter value missing for " + param);
		}

		return argList.get(paramIndex + 1);
	}

	private void setInputOperation(InputOperation newOp) {
		if (inputOp != null) {
			exitMessage("Multiple input operations found");
		}

		this.inputOp = newOp;
	}

	private void setOutputOperation(OutputOperation newOp) {
		if (outputOp != null) {
			exitMessage("Multiple output operations found");
		}

		this.outputOp = newOp;
	}

	private void run() throws FileNotFoundException, SQLException, ClassNotFoundException, ParserException {
		// setup parser
		Parser parser = null;

		switch(inputOp) {
			case MSACCESS_MDB:
				parser = new AccessMDBParser(new File(file), doData);
			break;
			case DELIMITED:
				if (file != null) {
					parser = new DelimitedParser(new File(file), delimiter, textQuantifier, hasHeaderRow, doData);
				} else {
					parser = new DelimitedParser(new BufferedReader(new InputStreamReader(System.in)), new Name("unknown"), delimiter, textQuantifier, hasHeaderRow, doData);
				}
			break;
			case JDBC:
				Class.forName(driver);
				Connection con = DriverManager.getConnection(url, user, pass);
				parser = new JDBCParser(con, null, null, null, doData);
			break;
			case TURBINE_XML:
				parser = new TurbineXMLParser(new FileInputStream(file));
			break;
			default:
				exitMessage("This input option hasn't been defined!");
				System.exit(0);
			break;
		}

		// setup producer
		switch(outputOp) {
			case MSACCESS_MDB:
				producer = new AccessMDBProducer(new File(outputFile));
			break;
			case MSACCESS_SQL:
				producer = new AccessSQLProducer(out);
			break;
			case POSTGRESQL:
				producer = new PostgreSQLProducer(out);
			break;
			default:
				exitMessage("This output option hasn't been defined!");
				System.exit(0);
			break;
		}

		long beforeMili = System.currentTimeMillis();

		parser.parse(this);

		long runTimeMili = System.currentTimeMillis() - beforeMili;

		System.err.println("Runtime: " + runTimeMili + "ms");
	}

	public void produceStatement(Statement statement) {
		producer.produce(statement);
	}

	private void exitMessage(String message) {
		System.out.println(message);

		System.exit(0);
	}

	private void printUsage() {
		System.out.println(
			"jsqlparser <input options> <output options> [<additional options>]\n" +

			"input options:\n" +
			"\t-access-mdb -file <filename>\n" +
			"\t-delim [ -file <filename> ] [-noheader] [-seperator <char>] [-quantifier <char>]\n" +
			"\t-jdbc -driver <driver> -url <url> [-user <username>] [-pass <password>]\n" +
			"\t-turbine -file <filename>\n" +
			"\n" +
			"output options:\n" +
			"\t-out-access\n" +
			"\t-out-access-mdb -ofile <filename>\n" +
			"\t-out-postgre\n" +
			"\n" +
			"additional options:\n" +
			"\t-data"
		);
	}

	public static void main (String[] args) throws ParserException, SQLException, FileNotFoundException, ClassNotFoundException {
		new SQLConverterCLI(args);
	}
}
