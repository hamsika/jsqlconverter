package com.googlecode.jsqlconverter.producer;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ParameterOptional;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.truncate.table.Truncate;
import com.googlecode.jsqlconverter.producer.interfaces.CreateIndexInterface;
import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;
import com.googlecode.jsqlconverter.producer.interfaces.FinalInterface;
import com.googlecode.jsqlconverter.producer.interfaces.InsertFromValuesInterface;
import com.googlecode.jsqlconverter.producer.interfaces.TruncateInterface;
import com.googlecode.jsqlconverter.producer.sql.AccessSQLProducer;
import com.googlecode.jsqlconverter.producer.sql.MySQLProducer;
import com.googlecode.jsqlconverter.producer.sql.OracleSQLProducer;
import com.googlecode.jsqlconverter.producer.sql.PostgreSQLProducer;
import com.googlecode.jsqlconverter.producer.sql.SQLServerProducer;

@ServiceName("JDBC")
public class JDBCProducer extends Producer implements CreateIndexInterface, CreateTableInterface, InsertFromValuesInterface, TruncateInterface, FinalInterface {
	private PipedInputStream in;
	private Connection con;
	private Producer producer;
	private int batchSize = 10000;
	private int statementCount = 0;
	private boolean ignoreCreateTable;
	private boolean ignoreCreateIndex;
	private boolean ignoreInsert;
	private boolean ignoreTruncate;

	public enum SQLDialect {
		Access,
		MySQL,
		Oracle,
		PostgreSQL,
		SQLServer
	}

	public JDBCProducer(
			@ParameterName("Driver") String className,
			@ParameterName("URL") String url,
			@ParameterName("Username") String username,
			@ParameterName("Password") String password,
			@ParameterName("Dialect") SQLDialect sqlProducer,
			@ParameterName("Ignore Table") @ParameterOptional(defaultValue = "false") Boolean ignoreCreateTable,
			@ParameterName("Ignore Index") @ParameterOptional(defaultValue = "false") Boolean ignoreCreateIndex,
			@ParameterName("Ignore Insert") @ParameterOptional(defaultValue = "false") Boolean ignoreInsert,
			@ParameterName("Ignore Truncate") @ParameterOptional(defaultValue = "false") Boolean ignoreTruncate
	) throws IOException, ClassNotFoundException, SQLException {
		PipedInputStream pin = new PipedInputStream();
		PrintStream out = new PrintStream(new PipedOutputStream(pin));

		switch(sqlProducer) {
			case Access:
				producer = new AccessSQLProducer(out);
			break;
			case MySQL:
				producer = new MySQLProducer(out);
			break;
			case Oracle:
				producer = new OracleSQLProducer(out);
			break;
			case PostgreSQL:
				producer = new PostgreSQLProducer(out);
			break;
			case SQLServer:
				producer = new SQLServerProducer(out);
			break;
		}

		this.in = pin;
		this.ignoreCreateTable = ignoreCreateTable;
		this.ignoreCreateIndex = ignoreCreateIndex;
		this.ignoreInsert = ignoreInsert;
		this.ignoreTruncate = ignoreTruncate;

		Class.forName(className);

		con = DriverManager.getConnection(url, username, password);
		con.setAutoCommit(false);
	}

	private void doExecution(Statement statement) throws ProducerException {
		producer.produce(statement);

		int avail;

		try {
			avail = in.available();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ProducerException(e.getMessage(), e);
		}

		byte[] bytes = new byte[avail];

		try {
			if (in.read(bytes) != avail) {
				throw new ProducerException("Number of bytes read doesn't match number of bytes available");
			}
		} catch (IOException e) {
			throw new ProducerException(e.getMessage(), e);
		}

		String sql = new String(bytes);

		LOG.finer(sql);

		try {
			con.prepareStatement(sql).execute();
		} catch (SQLException e) {
			throw new ProducerException(e.getMessage(), e);
		}

		++statementCount;

		if (statementCount > batchSize) {
			statementCount = 0;

			try {
				con.commit();
			} catch (SQLException e) {
				throw new ProducerException(e.getMessage(), e);
			}
		}
	}

	@Override
	public void doCreateTable(CreateTable table) throws ProducerException {
		if (ignoreCreateTable) return;
		doExecution(table);
	}

	@Override
	public void doCreateIndex(CreateIndex index) throws ProducerException {
		if (ignoreCreateIndex) return;
		doExecution(index);
	}

	@Override
	public void doInsertFromValues(InsertFromValues insert) throws ProducerException {
		if (ignoreInsert) return;
		doExecution(insert);
	}

	@Override
	public void doTruncate(Truncate truncate) throws ProducerException {
		if (ignoreTruncate) return;
		doExecution(truncate);
	}

	@Override
	public void doFinal() throws ProducerException {
		try {
			con.commit();
		} catch (SQLException e) {
			throw new ProducerException(e.getMessage(), e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				throw new ProducerException(e.getMessage(), e);
			}
		}
	}
}
