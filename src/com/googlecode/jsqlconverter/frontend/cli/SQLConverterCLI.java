package com.googlecode.jsqlconverter.frontend.cli;

import com.googlecode.jsqlconverter.parser.JDBCParser;
import com.googlecode.jsqlconverter.parser.ParserException;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.producer.StandardSQLProducer;
import com.googlecode.jsqlconverter.producer.Producer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConverterCLI {
	public static void main (String[] args) throws ClassNotFoundException, SQLException, ParserException {
		System.out.println("CLI Frontend :)");

		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		Class.forName("org.postgresql.Driver");

		//Connection con = DriverManager.getConnection("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=default.mdb;DriverID=22;READONLY=false}");
		Connection con = DriverManager.getConnection("jdbc:postgresql://10.156.103.29/lircdb", "jamcarte", "jamcarte");

		JDBCParser parser = new JDBCParser(con);

		Statement[] statements = parser.parse();

		System.out.println("i created " + statements.length + " statements from that database homes");

		Producer producer = new StandardSQLProducer();

		producer.produce(statements);
	}
}
