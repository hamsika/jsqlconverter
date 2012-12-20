package com.googlecode.jsqlconverter.producer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.producer.JDBCProducer.SQLDialect;
import com.googlecode.jsqlconverter.testutils.CommonTasks;
import com.googlecode.jsqlconverter.testutils.TestProperties;

public class TestJDBCProducer {
	private static final TestProperties prop = new TestProperties();

	@Before
	public void setUp() throws FileNotFoundException, IOException {
		prop.load(new FileInputStream("test/resources/conf/jdbc.properties"));
	}

	@Test
	public void testAllActive() throws IOException, ClassNotFoundException, SQLException {
		for (String dbKey : prop.getProperties("active")) {
			testDatabase(dbKey);
		}
	}

	private void testDatabase(String dbKey) throws IOException, ClassNotFoundException, SQLException {
		JDBCProducer producer = getJDBCProducer(dbKey);

		for (CreateTable ct : CommonTasks.getCreateTables()) {
			producer.doCreateTable(ct);
		}

		producer.doFinal();
	}

	private JDBCProducer getJDBCProducer(String dbKey) throws IOException, ClassNotFoundException, SQLException {
		return new JDBCProducer(
			prop.getProperty(dbKey + ".driver"),
			prop.getProperty(dbKey + ".url"),
			prop.getProperty(dbKey + ".user"),
			prop.getProperty(dbKey + ".pass"),
			SQLDialect.valueOf(dbKey),
			false,
			false,
			false,
			false
		);
	}
}
