package com.googlecode.jsqlconverter.frontend.cli;

import com.googlecode.jsqlconverter.parser.JDBCParser;
import com.googlecode.jsqlconverter.parser.ParserException;
import com.googlecode.jsqlconverter.definition.Statement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConverterCLI {
	public static void main (String[] args) throws ClassNotFoundException, SQLException, ParserException {
		System.out.println("CLI Frontend :)");

		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

		Connection con = DriverManager.getConnection("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=default.mdb;DriverID=22;READONLY=false}");

		JDBCParser parser = new JDBCParser(con);

		Statement[] statements = parser.parse();

		System.out.println("i created " + statements.length + " statements from that database homes");
	}
}
