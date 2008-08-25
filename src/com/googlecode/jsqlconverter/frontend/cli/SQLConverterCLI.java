package com.googlecode.jsqlconverter.frontend.cli;

import com.googlecode.jsqlconverter.parser.Parser;
import com.googlecode.jsqlconverter.parser.DelimitedParser;
import com.googlecode.jsqlconverter.parser.JDBCParser;
import com.googlecode.jsqlconverter.parser.ParserException;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.producer.PostgreSQLProducer;
import com.googlecode.jsqlconverter.producer.Producer;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConverterCLI {
	private Operation op = null;
	private ArrayList<String> argList;

	// jdbc params
	private String driver;
	private String url;
	private String user;
	private String pass;

	// delim / sql input
	private String file;

	private enum Operation {
		JDBC,
		DELIMITED
	}

	public SQLConverterCLI(String[] args) throws ParserException, SQLException, FileNotFoundException, ClassNotFoundException {
		if (args.length == 0) {
			printUsage();
		}

		argList = new ArrayList<String>(Arrays.asList(args));

		// detect operation
		if (argList.contains("-jdbc")) {
			setOperation(Operation.JDBC);
		}

		if (argList.contains("-delim")) {
			setOperation(Operation.DELIMITED);
		}

		// detect parser options
		switch(op) {
			case JDBC:
				driver = getRequiredParameter("-driver");
				url = getRequiredParameter("-url");
				user = getOptionalParameter("-user");
				pass = getOptionalParameter("-pass");
			break;
			case DELIMITED:
				setStdInOrFile();
			break;
		}

		// detect producer options


		// run it! :)
		run();
	}

	private void setStdInOrFile() {
		file = getOptionalParameter("-file");

		if (file != null && argList.contains("-stdin")) {
			throw new RuntimeException("Both stdin and file are defined");
		}
	}

	public String getRequiredParameter(String param) {		
		String value = getOptionalParameter(param);

		if (value == null) {
			throw new RuntimeException("Missing required parameter");
		}

		return value;
	}

	public String getOptionalParameter(String param) {
		if (!argList.contains(param)) {
			return null;
		}

		int paramIndex = argList.indexOf(param);

		if (paramIndex + 1 >= argList.size()) {
			throw new RuntimeException("Parameter value missing for " + param);
		}

		return argList.get(paramIndex + 1);
	}

	private void setOperation(Operation newOp) {
		if (op != null) {
			throw new RuntimeException("Multiple operations found");
		}

		this.op = newOp;
	}

	private void run() throws FileNotFoundException, SQLException, ClassNotFoundException, ParserException {
		// setup parser
		Parser parser = null;

		switch(op) {
			case DELIMITED:
				if (file != null) {
					parser = new DelimitedParser(new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)))), ',', true);
				} else {
					parser = new DelimitedParser(new BufferedReader(new InputStreamReader(System.in)), ',', true);
				}
			break;
			case JDBC:
				Class.forName(driver);
				Connection con = DriverManager.getConnection(url, user, pass);
				parser = new JDBCParser(con);
			break;
			default:
				System.out.println("This option hasn't been defined!");
				System.exit(1);
			break;
		}

		Statement[] statements = parser.parse();

		if (statements == null) {
			System.out.println("Parser returned no results");
			System.exit(1);
		}

		// setup producer
		Producer producer = new PostgreSQLProducer();
		//Producer producer = new AccessSQLProducer();

		producer.produce(statements);
	}

	private void printUsage() {
		//System.out.println("jsqlparser < -mysql | -postgres | -sqlserver > < -stdin | -file <filename> >");
		System.out.println("jsqlparser -jdbc -driver <driver> -url <url> [-user <username>] [-pass <password>]");
		System.out.println("jsqlparser -delim < -stdin | -file <filename> >"); // [-seperator <char>] [-quantifier <char>]

		System.exit(0);
	}

	public static void main (String[] args) throws ParserException, SQLException, FileNotFoundException, ClassNotFoundException {
		new SQLConverterCLI(args);
	}
}
