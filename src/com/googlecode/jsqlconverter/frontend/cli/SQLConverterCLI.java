package com.googlecode.jsqlconverter.frontend.cli;

import com.googlecode.jsqlconverter.producer.*;
import com.googlecode.jsqlconverter.parser.*;
import com.googlecode.jsqlconverter.definition.Statement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConverterCLI {
	public static void main (String[] args) throws ClassNotFoundException, SQLException, ParserException {
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		//Class.forName("org.postgresql.Driver");
		//Class.forName("org.h2.Driver");

		Connection con = DriverManager.getConnection("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=default.mdb;DriverID=22;READONLY=true}");
		//Connection con = DriverManager.getConnection("jdbc:postgresql://10.156.103.29/lircdb", "jamcarte", "jamcarte");
		//Connection con = DriverManager.getConnection("jdbc:h2:C:\\Users\\Projects\\LircDb\\db\\lirc_db", "sa", "");

		JDBCParser parser = new JDBCParser(con);

		Statement[] statements = parser.parse();

		if (statements == null) {
			System.out.println("Nothing returned from database.");
			System.exit(0);
		}

		System.out.println(statements.length + " statements were created from selected database");

		Producer producer = new PostgreSQLProducer();
		//Producer producer = new AccessSQLProducer();

		producer.produce(statements);
	}
}
