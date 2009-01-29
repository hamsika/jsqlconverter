package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.*;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.logging.LogLevel;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;

public class JDBCParser extends Parser {
	// TODO: support create schema
	// TODO: find out if all RDBMS always create indexes on foreign key columns
	private Connection con;
	private String catalog;
	private String schemaPattern;
	private String tableNamePattern;
	private static final String columnNamePattern = null; // currently unused
	private boolean convertDataToInsert;
	private ParserCallback callback;
	private String[] types = { "TABLE" };	// TODO: support VIEW, GLOBAL TEMPORARY, LOCAL TEMPORARY
									// h2: TABLE LINK
									// postgres: TEMPORARY TABLE

	public JDBCParser(Connection con) {
		this(con, null, null, null);
	}

	public JDBCParser(Connection con, String catalog, String schemaPattern, String tableNamePattern) {
		this(con, catalog, schemaPattern, tableNamePattern, false);
	}

	public JDBCParser(Connection con, String catalog, String schemaPattern, String tableNamePattern, boolean convertDataToInsert) {
		this.con = con;
		this.catalog = catalog;
		this.schemaPattern = schemaPattern;
		this.tableNamePattern = tableNamePattern;
		this.convertDataToInsert = convertDataToInsert;
	}

	public void parse(ParserCallback callback) throws ParserException {
		this.callback = callback;

		try {
			DatabaseMetaData meta = con.getMetaData();

			ResultSet tablesRs = meta.getTables(catalog, schemaPattern, tableNamePattern, types);

			while (tablesRs.next()) {
				// find out what type the object returned is (it may be a table / view / other)
				String tableType = tablesRs.getString("TABLE_TYPE");

				if (tableType.equals("TABLE")) {
					getTableStatements(meta, tablesRs.getString("TABLE_CAT"), tablesRs.getString("TABLE_SCHEM"), tablesRs.getString("TABLE_NAME"), convertDataToInsert);
				} else {
					log.log(Level.WARNING, "Unhandled Table Type: " + tableType);
				}
			}

			tablesRs.close();

			con.close();
		} catch (SQLException sqle) {
			throw new ParserException(sqle.getMessage(), sqle.getCause());
		}
	}

	private void getTableStatements(DatabaseMetaData meta, String catalog, String schema, String tableName, boolean convertDataToInsert) throws SQLException {
		ResultSet columnsRs = meta.getColumns(
								catalog,
								schema,
								tableName,
								columnNamePattern
		);

		CreateTable createTable = null;
		Type dataType;

		while (columnsRs.next()) {
			String currentTable = columnsRs.getString("TABLE_NAME");
			String tableSchema = columnsRs.getString("TABLE_SCHEM");

			if (createTable == null) {
				createTable = new CreateTable(new Name(tableSchema, currentTable));
			}

			int dbType = columnsRs.getInt("DATA_TYPE");
			int colSize = columnsRs.getInt("COLUMN_SIZE");

			dataType = getType(dbType, colSize, columnsRs.getInt("DECIMAL_DIGITS"));

			Column col = new Column(new Name(columnsRs.getString("COLUMN_NAME")), dataType);

			if (!(dataType instanceof NumericType) && !(dataType instanceof BooleanType)) {
				col.setSize(colSize);
			}

			if (columnsRs.getString("IS_NULLABLE").equals("NO")) {
				col.addColumnOption(ColumnOption.NOT_NULL);
			}

			// default value
			String defaultValue = columnsRs.getString("COLUMN_DEF");

			// work out if the default value is auto increment
			if (defaultValue != null) {
				if (defaultValue.startsWith("nextval(")) {
					col.addColumnOption(ColumnOption.AUTO_INCREMENT);
				} else if (defaultValue.startsWith("(NEXT VALUE FOR")) {
					col.addColumnOption(ColumnOption.AUTO_INCREMENT);
				} else {
					col.setDefaultConstraint(new DefaultConstraint(defaultValue));
				}
			}

			createTable.addColumn(col);


			// references
			ResultSet referencesRs = null;

			try {
				referencesRs = meta.getImportedKeys(catalog, tableSchema, currentTable);
			} catch (SQLException sqle) {
				callback.log("Failed to get foreign key list");
				log.log(LogLevel.WARNING, "", sqle);
			}

			if (referencesRs != null) {
				while (referencesRs.next()) {
					if (referencesRs.getString("PKCOLUMN_NAME").equals(columnsRs.getString("COLUMN_NAME"))) {
						ForeignKeyConstraint ref = new ForeignKeyConstraint(new Name(referencesRs.getString("PKTABLE_NAME")), new Name(referencesRs.getString("PKCOLUMN_NAME")));

						switch(referencesRs.getShort("UPDATE_RULE")) {
							case DatabaseMetaData.importedKeyNoAction:
								ref.setUpdate(ForeignKeyAction.NO_ACTION);
							break;
							case DatabaseMetaData.importedKeyCascade:
								ref.setUpdate(ForeignKeyAction.CASCADE);
							break;
							case DatabaseMetaData.importedKeySetNull:
								ref.setUpdate(ForeignKeyAction.SET_NULL);
							break;
							case DatabaseMetaData.importedKeySetDefault:
								ref.setUpdate(ForeignKeyAction.SET_DEFAULT);
							break;
							case DatabaseMetaData.importedKeyRestrict:
								ref.setUpdate(ForeignKeyAction.RESTRICT);
							break;
							default:
								log.log(LogLevel.UNHANDLED, "Unknown update reference");
							break;
						}

						switch(referencesRs.getShort("DELETE_RULE")) {
							case DatabaseMetaData.importedKeyNoAction:
								ref.setDelete(ForeignKeyAction.NO_ACTION);
							break;
							case DatabaseMetaData.importedKeyCascade:
								ref.setDelete(ForeignKeyAction.CASCADE);
							break;
							case DatabaseMetaData.importedKeySetNull:
								ref.setDelete(ForeignKeyAction.SET_NULL);
							break;
							case DatabaseMetaData.importedKeySetDefault:
								ref.setDelete(ForeignKeyAction.SET_DEFAULT);
							break;
							case DatabaseMetaData.importedKeyRestrict:
								ref.setDelete(ForeignKeyAction.RESTRICT);
							break;
							default:
								log.log(LogLevel.UNHANDLED, "Unknown delete reference");
							break;
						}

						// TODO: support setting match
						//ref.setMatch();

						col.setForeignKeyConstraint(ref);
					}
				}
			}
		}

		columnsRs.close();

		if (createTable == null) {
			return;
		}

		callback.produceStatement(createTable);

		// query indexes and associate with table
		CreateIndex[] indexes = getTableIndexes(meta, createTable.getName());

		CreateIndex primaryKey = null;

		try {
			primaryKey = getTablePrimaryKey(meta, createTable.getName());
		} catch (SQLException sqle) {
			callback.log("Failed to get primary key list");
			log.log(LogLevel.WARNING, "", sqle);
		}

		if (primaryKey != null) {
			if (primaryKey.getColumns().length == 1) {
				Column column = getTableColumn(createTable, primaryKey.getColumns()[0]);

				if (column != null) {
					column.addColumnOption(ColumnOption.PRIMARY_KEY);
				} else {
					callback.log("couldn't find primary key column");
				}
			} else {
				createTable.setPrimaryCompoundKeyConstraint(new KeyConstraint(primaryKey.getColumns()));
			}
		}

		if (indexes != null) {
			for (CreateIndex index : indexes) {
				// if this index is actually a primary key, then ignore it
				if (primaryKey != null) {
					if (index.getIndexName().getObjectName().equals(primaryKey.getIndexName().getObjectName())) {
						continue;
					}
				}

				// any unique indexes are considers to be part of the table and not user generated
				if (index.isUnique()) {
					if (index.getColumns().length == 1) {
						Column column = getTableColumn(createTable, index.getColumns()[0]);

						if (column != null) {
							column.addColumnOption(ColumnOption.UNIQUE);
						}

						continue;
					} else {
						createTable.addUniqueCompoundKeyConstraint(new KeyConstraint(index.getColumns()));
						continue;
					}
				}


				// user created index
				callback.produceStatement(index);

				if (index.isUnique()) {
					log.log(LogLevel.UNHANDLED, "possible implicit database index: " + index.getTableName().getObjectName() + " " + index.getIndexName().getObjectName() + " " + index.getColumns().length + " " + index.isUnique());
				}
			}
		}

		if (convertDataToInsert) {
			InsertFromValues[] inserts = getTableData(createTable);

			for (InsertFromValues insert : inserts) {
				callback.produceStatement(insert);
			}
		}
	}

	private Column getTableColumn(CreateTable createTable, Name columnName) {
		for (int i=0; i<createTable.getColumnCount(); i++) {
			Column column = createTable.getColumn(i);

			if (column.getName().getObjectName().equals(columnName.getObjectName())) {
				return column;
			}
		}

		return null;
	}

	private InsertFromValues[] getTableData(CreateTable tableName) throws SQLException {
		// TODO: find better way of doing this
		// some DBMS do not allow setting tablename as "?" for PreparedStatements
		PreparedStatement dataPs = con.prepareStatement("SELECT * FROM \"" + tableName.getName().getObjectName() + "\"");

		ResultSet dataRs = dataPs.executeQuery();

		ResultSetMetaData meta = dataRs.getMetaData();

		ArrayList<InsertFromValues> inserts = new ArrayList<InsertFromValues>();

		while (dataRs.next()) {
			InsertFromValues insert = new InsertFromValues(tableName.getName(), tableName.getColumns());

			for (int i=0; i<meta.getColumnCount(); i++) {
				String columnValue = dataRs.getString(i + 1);

				if (columnValue == null) {
					continue;
				}

				insert.setValue(i, columnValue);
			}

			inserts.add(insert);
		}

		dataRs.close();
		dataPs.close();

		return inserts.toArray(new InsertFromValues[inserts.size()]);
	}

	private CreateIndex[] getTableIndexes(DatabaseMetaData meta, Name tableName) throws SQLException {
		ResultSet indexesRs = meta.getIndexInfo(catalog, tableName.getSchemaName(), tableName.getObjectName(), false, false);

		ArrayList<CreateIndex> indexes = new ArrayList<CreateIndex>();

		while (indexesRs.next()) {
			String indexName = indexesRs.getString("INDEX_NAME");
			String columnName = indexesRs.getString("COLUMN_NAME");

			if (indexName == null) {
				if (columnName == null) {
					// TODO: find out if this is unique to access (and what it means)
					// TODO: find out if statistic index has anything to do with this 

					log.log(LogLevel.ERROR, "both column name and index name are null - skipped");
					continue;
				}

				log.log(LogLevel.ERROR, "index name is null, but a column name is available - skipped");

				continue;
			}

			CreateIndex createIndex = null;

			// find out if the index already exists (multiple column index)
			for (CreateIndex previousIndex : indexes) {
				if (previousIndex.getIndexName().getObjectName().equals(indexName)) {
					createIndex = previousIndex;

					break;
				}
			}

			if (createIndex == null) {
				createIndex = new CreateIndex(new Name(indexName), tableName);

				indexes.add(createIndex);

				if (!indexesRs.getBoolean("NON_UNIQUE")) {
					createIndex.setUnique(true);
				}

				String sortSeq = indexesRs.getString("ASC_OR_DESC");

				if (sortSeq != null) {
					if (sortSeq.equals("A")) {
						createIndex.setSortSequence(CreateIndex.SortSequence.ASC);
					} else if (sortSeq.equals("D")) {
						createIndex.setSortSequence(CreateIndex.SortSequence.DESC);
					} else {
						log.log(LogLevel.UNHANDLED, "Unknown sort sequence");
					}
				}

				switch(indexesRs.getShort("TYPE")) {
					case DatabaseMetaData.tableIndexStatistic:
						
					break;
					case DatabaseMetaData.tableIndexClustered:
						createIndex.setType(CreateIndex.IndexType.CLUSTERED);
					break;
					case DatabaseMetaData.tableIndexHashed:
						createIndex.setType(CreateIndex.IndexType.HASHED);
					break;
					case DatabaseMetaData.tableIndexOther:

					break;
				}
			}

			createIndex.addColumn(new Name(columnName));
		}

		if (indexes.size() == 0) {
			return null;
		}

		return indexes.toArray(new CreateIndex[indexes.size()]);
	}

	private CreateIndex getTablePrimaryKey(DatabaseMetaData meta, Name tableName) throws SQLException {
		ResultSet primarykeyRs = meta.getPrimaryKeys(catalog, tableName.getSchemaName(), tableName.getObjectName());

		CreateIndex primaryKey = null;

		while(primarykeyRs.next()) {
			if (primaryKey == null) {
				primaryKey = new CreateIndex(
					new Name(primarykeyRs.getString("PK_NAME")),
					new Name(primarykeyRs.getString("TABLE_CAT"), primarykeyRs.getString("TABLE_SCHEM"), primarykeyRs.getString("TABLE_NAME"))
				);
			}

			primaryKey.addColumn(new Name(primarykeyRs.getString("COLUMN_NAME")));
		}

		return primaryKey;
	}

	private Type getType(int dbType, int columnSize, int decimalDigits) {
		Type dataType = null;

		switch(dbType) {
			case Types.ARRAY:
				dataType = BinaryType.BLOB; // guess
			break;
			case Types.BIGINT:
				dataType = ExactNumericType.BIGINT;
			break;
			case Types.BINARY:
				dataType = BinaryType.BINARY;
			break;
			case Types.BIT:
				dataType = BinaryType.BIT;
			break;
			case Types.BLOB:
				dataType = BinaryType.BLOB;
			break;
			case Types.BOOLEAN:
				dataType = BooleanType.BOOLEAN;
			break;
			case Types.CHAR:
				dataType = StringType.CHAR;
			break;
			case Types.CLOB:

			break;
			/*case Types.DATALINK:

			break;*/
			case Types.DATE:
				// TODO: make sure JDBC DATE is same as our DATETIME
				dataType = DateTimeType.DATETIME;
			break;
			case Types.DECIMAL:
			case Types.NUMERIC:
				// TODO: detect if this should be money / small money
				dataType = new DecimalType(columnSize, decimalDigits);
			break;
			/*case Types.DISTINCT:

			break;*/
			case Types.DOUBLE:
				dataType = ApproximateNumericType.DOUBLE;
			break;
			case Types.FLOAT:
				dataType = ApproximateNumericType.FLOAT;
			break;
			case Types.INTEGER:
				dataType = ExactNumericType.INTEGER;
			break;
			/*case Types.JAVA_OBJECT:

			break;*/
			case Types.LONGNVARCHAR:
				dataType = StringType.NVARCHAR; // guess
			break;
			case Types.LONGVARBINARY:
				dataType = BinaryType.VARBINARY; // guess
			break;
			case Types.LONGVARCHAR:
				dataType = StringType.VARCHAR; // guess
			break;
			case Types.NCHAR:
				dataType = StringType.NCHAR;
			break;
			case Types.NCLOB:

			break;
			/*case Types.NULL:

			break;*/
			case Types.NVARCHAR:
				dataType = StringType.NVARCHAR;
			break;
			/*case Types.OTHER:

			break;*/
			case Types.REAL:
				dataType = ApproximateNumericType.REAL;
			break;
			case Types.REF:

			break;
			case Types.ROWID:

			break;
			case Types.SMALLINT:
				dataType = ExactNumericType.SMALLINT;
			break;
			/*case Types.SQLXML:

			break;
			case Types.STRUCT:

			break;*/
			case Types.TIME:

			break;
			case Types.TIMESTAMP:
				// TODO: is JDBC timestamp the same as our timestamp?
				dataType = DateTimeType.TIMESTAMP;
			break;
			case Types.TINYINT:
				dataType = ExactNumericType.TINYINT;
			break;
			case Types.VARBINARY:
				dataType = BinaryType.VARBINARY;
			break;
			case Types.VARCHAR:
				dataType = StringType.VARCHAR;
			break;
			default:
				// This should only happen if new types are added in newer JDBC versions
				log.log(LogLevel.UNHANDLED, "unknown data type - this needs to be fixed: " + dbType);
				dataType = new UnhandledType(String.valueOf(dbType));
			break;
		}

		if (dataType == null) {
			log.log(LogLevel.UNHANDLED, "Unhandled type from database: " + dbType);
			dataType = StringType.VARCHAR;
		}

		return dataType;
	}
}
