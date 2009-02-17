package com.googlecode.jsqlconverter.producer;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.*;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.testutils.StatementGenerator;
import com.googlecode.jsqlconverter.testutils.TestProperties;

public class SQLProducerTest extends TestCase {
	private PipedInputStream in;
	private Statement[] statements;
	private ArrayList<ConnectionSetup> connections = new ArrayList<ConnectionSetup>();

	protected void setUp() throws IOException, ClassNotFoundException, SQLException {
		DriverManager.setLoginTimeout(3000);

		PipedInputStream pin = new PipedInputStream();
		PrintStream out = new PrintStream(new PipedOutputStream(pin));

		in = pin;

		statements = new StatementGenerator().generateCreateTableStatements(20);

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
