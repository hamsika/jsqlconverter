package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.parser.GeneratorParser;
import com.googlecode.jsqlconverter.producer.sql.*;
import com.googlecode.jsqlconverter.testutils.TestProperties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class SQLProducerTest extends TestCase {
	private PipedInputStream in;
	private Statement[] statements;
	private ArrayList<ConnectionSetup> connections = new ArrayList<ConnectionSetup>();

	protected void setUp() throws IOException, ClassNotFoundException, SQLException {
		DriverManager.setLoginTimeout(3000);

		PipedInputStream pin = new PipedInputStream();
		PrintStream out = new PrintStream(new PipedOutputStream(pin));

		in = pin;

		statements = new GeneratorParser(20).generateCreateTableStatements(20);

		if (TestProperties.getBoolean("access.enabled")) {
			System.out.println("SQLProducerTest adding Access");
			connections.add(new ConnectionSetup("access", new AccessSQLProducer(out)));
		}

		if (TestProperties.getBoolean("mysql.enabled")) {
			System.out.println("SQLProducerTest adding MySQL");
			connections.add(new ConnectionSetup("mysql", new MySQLProducer(out)));
		}

		if (TestProperties.getBoolean("postgresql.enabled")) {
			System.out.println("SQLProducerTest adding PostgreSQL");
			connections.add(new ConnectionSetup("postgresql", new PostgreSQLProducer(out)));
		}

		if (TestProperties.getBoolean("sqlserver.enabled")) {
			System.out.println("SQLProducerTest adding SQLServer");
			connections.add(new ConnectionSetup("sqlserver", new SQLServerProducer(out)));
		}
	}

	public void testProduce() {
		for (ConnectionSetup cs : connections) {
			try {
				doExecution(cs.getConnection(), cs.getProducer());
			} catch (IOException e) {
				fail(e.getMessage());
			}
		}
	}

	private void doExecution(Connection con, Producer producer) throws IOException {
		for (Statement statement : statements) {
			producer.produce(statement);

			int avail = in.available();

			byte[] bytes = new byte[avail];

			if (in.read(bytes) != avail) {
				fail("Number of bytes read doesn't match number of bytes available");
			}

			String sql = new String(bytes);

			try {
				con.prepareStatement(sql).executeUpdate();
			} catch (SQLException e) {
				fail(sql + "\n" + e.getMessage());
			}
		}
	}

	private class ConnectionSetup {
		private SQLProducer producer;
		private Connection con;

		public ConnectionSetup(String dbname, SQLProducer producer) throws ClassNotFoundException, SQLException {
			this.producer = producer;

			con = getConnection(dbname);
		}

		public SQLProducer getProducer() {
			return producer;
		}

		public Connection getConnection() {
			return con;
		}

		private Connection getConnection(String db) throws ClassNotFoundException, SQLException {
			Class.forName(TestProperties.getProperty(db + ".driver"));

			return DriverManager.getConnection(
				TestProperties.getProperty(db + ".url"),
				TestProperties.getProperty(db + ".user"),
				TestProperties.getProperty(db + ".pass")
			);
		}
	}

	public static Test suite() {
		return new TestSuite(SQLProducerTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
