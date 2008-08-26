package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.*;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;

import java.sql.*;
import java.util.ArrayList;

public class JDBCParser implements Parser {
	// TODO: find out if all RDBMS always create indexes on foreign key columns
	// TODO: support temp tables (does that have anything to do with GLOBAL / LOCAL TEMPORARY?)
	private Connection con;
	private String catalog;
	private String schemaPattern;
	private String tableNamePattern;
	private String columnNamePattern = null;
	private String[] types = { "TABLE" }; // TODO: support VIEW, GLOBAL TEMPORARY, LOCAL TEMPORARY
	private boolean convertDataToInsert;

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

	public Statement[] parse() throws ParserException {
		ArrayList<Statement> statements = new ArrayList<Statement>();

		try {
			DatabaseMetaData meta = con.getMetaData();

			ResultSet tablesRs = meta.getTables(catalog, schemaPattern, tableNamePattern, types);

			while (tablesRs.next()) {
				// find out what type the object returned is (it may be a table / view / other)
				Statement[] generatedStatements = null;
				String tableType = tablesRs.getString("TABLE_TYPE");

				if (tableType.equals("TABLE")) {
					generatedStatements = getTableStatements(meta, tablesRs.getString("TABLE_CAT"), tablesRs.getString("TABLE_SCHEM"), tablesRs.getString("TABLE_NAME"), convertDataToInsert);
				} else {
					System.out.println("Unhandled Table Type: " + tableType);
				}

				// add the statements to main list
				if (generatedStatements != null) {
					for (Statement statement : generatedStatements) {
						statements.add(statement);
					}
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

		// TODO: support ordering of tables, views, etc so they're in correct 'create' order. select should be ignored.

		return statements.toArray(new Statement[] {});
	}

	private Statement[] getTableStatements(DatabaseMetaData meta, String catalog, String schema, String tableName, boolean convertDataToInsert) throws SQLException {
		ResultSet columnsRs = meta.getColumns(
								catalog,
								schema,
								tableName,
								columnNamePattern
		);

		ArrayList<Statement> statements = new ArrayList<Statement>();
		CreateTable createTable = null;
		Type dataType;

		while (columnsRs.next()) {
			String currentTable = columnsRs.getString("TABLE_NAME");
			String tableSchema = columnsRs.getString("TABLE_SCHEM");

			if (createTable == null) {
				createTable = new CreateTable(new Name(tableSchema, currentTable));
				statements.add(createTable);
			}

			int dbType = columnsRs.getInt("DATA_TYPE");

			dataType = getType(dbType);

			Column col = new Column(new Name(columnsRs.getString("COLUMN_NAME")), dataType);

			if (!(dataType instanceof NumericType) && !(dataType instanceof BooleanType)) {
				col.setSize(columnsRs.getInt("COLUMN_SIZE"));
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
				// TODO: handle nicely for those RDBMS that do not support this
				//sqle.printStackTrace();
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
								System.out.println("Unknown yodate reference");
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
								System.out.println("Unknown delete reference");
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
			return null;
		}

		// query indexes and associate with table
		CreateIndex[] indexes = getTableIndexes(meta, createTable.getName());

		CreateIndex primaryKey = null;

		try {
			primaryKey = getTablePrimaryKey(meta, createTable.getName());
		} catch (SQLException sqle) {
			// TODO: handle nicely for those RDBMS that do not support this
			//sqle.printStackTrace();
		}

		if (primaryKey != null) {
			if (primaryKey.getColumns().length == 1) {
				//setSingleColumnPrimarKey(createTable, primaryKey);
				Column column = getTableColumn(createTable, primaryKey.getColumns()[0]);

				if (column != null) {
					column.addColumnOption(ColumnOption.PRIMARY_KEY);
				} else {
					System.out.println("error.. couldn't find primary key column");
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
				statements.add(index);

				if (index.isUnique()) {
					System.out.println("possible implicit database index: " + index.getTableName().getObjectName() + " " + index.getIndexName().getObjectName() + " " + index.getColumns().length + " " + index.isUnique());
				}
			}
		}

		if (convertDataToInsert) {
			InsertFromValues[] inserts = getTableData(createTable.getName());

			for (InsertFromValues insert : inserts) {
				statements.add(insert);
			}
		}

		return statements.toArray(new Statement[] {});
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

	private InsertFromValues[] getTableData(Name tableName) throws SQLException {
		PreparedStatement dataPs = con.prepareStatement("SELECT * FROM \"" + tableName.getObjectName() + "\"");

		ResultSet dataRs = dataPs.executeQuery();

		ResultSetMetaData meta = dataRs.getMetaData();

		boolean[] isNumeric = new boolean[meta.getColumnCount()];

		for (int i=1; i<=meta.getColumnCount(); i++) {
			Type type = getType(meta.getColumnType(i));

			isNumeric[i - 1] = type instanceof NumericType;
		}

		ArrayList<InsertFromValues> inserts = new ArrayList<InsertFromValues>();

		while (dataRs.next()) {
			InsertFromValues insert = new InsertFromValues(tableName);

			for (int i=0; i<meta.getColumnCount(); i++) {
				String col = dataRs.getString(i + 1);

				if (col == null) {
					continue;
				}

				if (isNumeric[i]) {
					insert.setNumeric(i, Double.parseDouble(col));
				} else {
					insert.setString(i, col);
				}
			}

			inserts.add(insert);
		}

		return inserts.toArray(new InsertFromValues[] {});
	}

	private CreateIndex[] getTableIndexes(DatabaseMetaData meta, Name tableName) throws SQLException {
		// TODO: make sure only one index for each column is returned (no primary key + unique index like access shows)
		ResultSet indexesRs = meta.getIndexInfo(catalog, tableName.getSchemaName(), tableName.getObjectName(), false, false);

		ArrayList<CreateIndex> indexes = new ArrayList<CreateIndex>();

		while (indexesRs.next()) {
			String indexName = indexesRs.getString("INDEX_NAME");
			String columnName = indexesRs.getString("COLUMN_NAME");

			if (indexName == null) {
				if (columnName == null) {
					// TODO: find out if this is unique to access (and what it means)
					//System.out.println("both column name and index name is null :(");
					continue;
				}

				System.out.println("ok, index name is null, but we have a column name, lets do this");

				/*if (!indexesRs.getBoolean("NON_UNIQUE")) {
					//for (Column column : createTable.getColumns()) {

					System.out.println("searching for column: " + columnName);

					for (Iterator<Column> i = createTable.getColumnsIter(); i.hasNext(); ) {
						Column column = i.next();

						if (column.getName().getObjectName().equals(columnName)) {
							column.addConstraint(new Constraint(ConstraintType.UNIQUE));
							System.out.println("LOL");
							break;
						}
					}
				} else {
					System.out.println("Index name is null, but it's not unique.. ");
				}*/

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
						System.out.println("Unknown sort sequence");
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

		return indexes.toArray(new CreateIndex[] {});
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

	private Type getType(int dbType) {
		Type dataType = null;

		switch(dbType) {
			case Types.ARRAY:
				
			break;
			case Types.BIGINT:
				dataType = ExactNumericType.BIGINT;
			break;
			case Types.BINARY:
				dataType = BinaryType.BINARY;
			break;
			case Types.BIT:
				//dataType = BinaryType.BIT;
				dataType = BooleanType.BOOLEAN;
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
			case Types.DATALINK:

			break;
			case Types.DATE:
				// TODO: make sure JDBC DATE is same as our DATETIME
				dataType = DateTimeType.DATETIME;
			break;
			case Types.DECIMAL:

			break;
			case Types.DISTINCT:

			break;
			case Types.DOUBLE:
				dataType = ApproximateNumericType.DOUBLE;
			break;
			case Types.FLOAT:
				dataType = ApproximateNumericType.FLOAT;
			break;
			case Types.INTEGER:
				dataType = ExactNumericType.INTEGER;
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
				dataType = StringType.NCHAR;
			break;
			case Types.NCLOB:

			break;
			case Types.NULL:

			break;
			case Types.NUMERIC:
				dataType = ExactNumericType.NUMERIC;
			break;
			case Types.NVARCHAR:
				dataType = StringType.NVARCHAR;
			break;
			case Types.OTHER:

			break;
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
			case Types.SQLXML:

			break;
			case Types.STRUCT:

			break;
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
				dataType = new UnhandledType(String.valueOf(dbType));
			break;
		}

		return dataType;
	}
}
