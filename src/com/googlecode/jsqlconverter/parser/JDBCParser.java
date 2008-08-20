package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.Column;

import java.sql.ResultSet;
import java.sql.Types;
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class JDBCParser implements Parser {
	private Connection con;
	private String catalog;
	private String schemaPattern;
	private String tableNamePattern;
	private String columnNamePattern = null;
	private String[] types = { "TABLE" }; // TODO: support VIEW, GLOBAL TEMPORARY, LOCAL TEMPORARY

	public JDBCParser(Connection con) {
		this(con, null, null, null);
	}

	public JDBCParser(Connection con, String catalog, String schemaPattern, String tableNamePattern) {
		this.con = con;
		this.catalog = catalog;
		this.schemaPattern = schemaPattern;
		this.tableNamePattern = tableNamePattern;
	}

	public Statement[] parse() throws ParserException {
		ArrayList<Statement> statements = new ArrayList<Statement>();

		try {
			DatabaseMetaData meta = con.getMetaData();

			ResultSet tablesRs = meta.getTables(catalog, schemaPattern, tableNamePattern, types);

			while (tablesRs.next()) {
				String tableType = tablesRs.getString("TABLE_TYPE");

				if (tableType.equals("TABLE")) {
					CreateTable[] createStatements = getCreateTable(meta, tablesRs.getString("TABLE_CAT"), tablesRs.getString("TABLE_SCHEM"), tablesRs.getString("TABLE_NAME"));

					if (createStatements != null) {
						for (CreateTable createTable : createStatements) {
							statements.add(createTable);
						}
					}
				} else {
					System.out.println("Unhandled Table Type: " + tableType);
				}
			}

			tablesRs.close();
			con.close();
		} catch (SQLException sqle) {
			throw new ParserException(sqle.getMessage(), sqle.getCause());
		}

		if (statements.size() == 0) {
			return null;
		}

		return statements.toArray(new Statement[] {});
	}

	private CreateTable[] getCreateTable(DatabaseMetaData meta, String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
		ArrayList<CreateTable> createStatments = new ArrayList<CreateTable>();

		ResultSet columnsRs = meta.getColumns(
								catalog,
								schemaPattern,
								tableNamePattern,
								columnNamePattern
		);

		String lastTable = "";

		CreateTable ct = null;
		Type dt = null;

		while (columnsRs.next()) {
			String currentTable = columnsRs.getString("TABLE_NAME");

			if (!lastTable.equals(currentTable)) {
				lastTable = currentTable;

				ct = new CreateTable(new Name(columnsRs.getString("TABLE_SCHEM"), currentTable));

				createStatments.add(ct);
			}

			int dataType = columnsRs.getInt("DATA_TYPE");

			switch(dataType) {
				case Types.ARRAY:

				break;
				case Types.BIGINT:
					dt = ExactNumericType.BIGINT;
				break;
				case Types.BINARY:
					dt = BinaryType.BINARY;
				break;
				case Types.BIT:
					dt = BinaryType.BIT;
				break;
				case Types.BLOB:
					dt = BinaryType.BLOB;
				break;
				case Types.BOOLEAN:
					dt = BooleanType.BOOLEAN;
				break;
				case Types.CHAR:
					dt = StringType.CHAR;
				break;
				case Types.CLOB:

				break;
				case Types.DATALINK:

				break;
				case Types.DATE:
					// TODO: make sure this is right
					dt = DateTimeType.DATETIME;
				break;
				case Types.DECIMAL:

				break;
				case Types.DISTINCT:

				break;
				case Types.DOUBLE:
					dt = ApproximateNumericType.DOUBLE;
				break;
				case Types.FLOAT:
					dt = ApproximateNumericType.FLOAT;
				break;
				case Types.INTEGER:
					dt = ExactNumericType.INTEGER;
				break;
				case Types.JAVA_OBJECT:

				break;
				case Types.LONGNVARCHAR:

				break;
				case Types.LONGVARBINARY:

				break;
				case Types.LONGVARCHAR:

				break;
				case Types.NCHAR:
					dt = StringType.NCHAR;
				break;
				case Types.NCLOB:

				break;
				case Types.NULL:

				break;
				case Types.NUMERIC:
					dt = ExactNumericType.NUMERIC;
				break;
				case Types.NVARCHAR:
					dt = StringType.NVARCHAR;
				break;
				case Types.OTHER:

				break;
				case Types.REAL:
					dt = ApproximateNumericType.REAL;
				break;
				case Types.REF:

				break;
				case Types.ROWID:

				break;
				case Types.SMALLINT:
					dt = ExactNumericType.SMALLINT;
				break;
				case Types.SQLXML:

				break;
				case Types.STRUCT:

				break;
				case Types.TIME:

				break;
				case Types.TIMESTAMP:
					dt = DateTimeType.TIMESTAMP;
				break;
				case Types.TINYINT:
					dt = ExactNumericType.TINYINT;
				break;
				case Types.VARBINARY:
					dt = BinaryType.VARBINARY;
				break;
				case Types.VARCHAR:
					dt = StringType.VARCHAR;
				break;
				default:
					dt = new UnhandledType(String.valueOf(dataType));
				break;
			}

			Column col = new Column(new Name(columnsRs.getString("COLUMN_NAME")), dt);

			if (!(dt instanceof NumericType)) {
				col.setSize(columnsRs.getInt("COLUMN_SIZE"));
			}

			ct.addColumn(col);
		}

		columnsRs.close();

		if (createStatments.size() == 0) {
			return null;
		}

		return createStatments.toArray(new CreateTable[] {});
	}
}
