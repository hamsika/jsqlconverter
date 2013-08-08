package com.googlecode.jsqlconverter.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ParameterOptional;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ColumnForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.type.ApproximateNumericType;
import com.googlecode.jsqlconverter.definition.type.BinaryType;
import com.googlecode.jsqlconverter.definition.type.BooleanType;
import com.googlecode.jsqlconverter.definition.type.DateTimeType;
import com.googlecode.jsqlconverter.definition.type.DecimalType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.definition.type.MonetaryType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.Table;

@ServiceName("Access MDB")
public class AccessMDBParser extends Parser {
	// TODO: support compound foreign keys
	private File mdbFile;
	private boolean convertDataToInsert;

	public AccessMDBParser(
		@ParameterName("File") File mdbPath,
		@ParameterName("Data") @ParameterOptional(defaultValue = "false") Boolean convertDataToInsert
	) {
		this.mdbFile = mdbPath;
		this.convertDataToInsert = convertDataToInsert;
	}

	@Override
	public void parse(ParserCallback callback) throws ParserException {
		Database db;
		Set<String> tableSet;

		try {
			db = Database.open(mdbFile, false);
			tableSet = db.getTableNames();
		} catch (IOException e) {
			throw new ParserException(e.getMessage(), e.getCause());
		}

		for (String tableName : tableSet) {
			Table table;

			try {
				table = db.getTable(tableName);
			} catch (IOException e) {
				throw new ParserException(e.getMessage(), e.getCause());
			}

			CreateTable ct = new CreateTable(new Name(table.getName()));
			ArrayList<CreateIndex> createIndexes = new ArrayList<CreateIndex>(); // normal non-unique indexes

			for (Column column : table.getColumns()) {
				com.googlecode.jsqlconverter.definition.create.table.Column col = new com.googlecode.jsqlconverter.definition.create.table.Column(new Name(column.getName()), getType(column.getType()));

				// TODO: set column size here
				
				ct.addColumn(col);
			}

			// check out indexes
			for (Index index : table.getIndexes()) {
				if (index.isForeignKey()) {
					// TODO: this probably doesn't work right. the column in "our" ct may not be the same column name in the referenced table
					String colName = index.getColumns().get(0).getName();

					ct.getColumn(colName).setForeignKeyConstraint(new ColumnForeignKeyConstraint(new Name(index.getTable().getName()), new Name(colName)));
				} else if (index.isPrimaryKey()) {
					List<ColumnDescriptor> indexes = index.getColumns();

					if (indexes.size() > 1) {
						ArrayList<Name> columnNames = new ArrayList<Name>();

						for (ColumnDescriptor cd : indexes) {
							columnNames.add(new Name(cd.getName()));
						}

						ct.setPrimaryCompoundKeyConstraint(new KeyConstraint(columnNames.toArray(new Name[columnNames.size()])));
					} else {
						// single column index
						// may need some additional error checking / message to ensure that the PRIMARY_KEY constraint was added
						String indexColumn = indexes.get(0).getName();

						ct.getColumn(indexColumn).addColumnOption(ColumnOption.PRIMARY_KEY);
					}
				} else if (index.isUnique()) {
					// add column constraint
					ct.getColumn(index.getName()).addColumnOption(ColumnOption.UNIQUE);
				} else {
					// just output it! lol
					CreateIndex ci = new CreateIndex(new Name(index.getName()), ct.getName());

					for (ColumnDescriptor cd : index.getColumns()) {
						ci.addColumn(new Name(cd.getName()));
					}

					createIndexes.add(ci);
				}
			}

			// send create table to producer
			callback.produceStatement(ct);

			// send create indexes to producer
			for (CreateIndex ci : createIndexes) {
				callback.produceStatement(ci);
			}


			// get data inserts
			if (convertDataToInsert) {
				doData(callback, ct, table);
			}
		}
	}

	private void doData(ParserCallback callback, CreateTable ct, Table table) {
		Name tableName = new Name(table.getName());

		for (Map<String, Object> row : table) {
			InsertFromValues insert = new InsertFromValues(tableName, ct.getColumns());

			int i = 0;

			if (row.values().size() != ct.getColumns().length) {
				System.out.println(row);

				// TODO: handle this

				System.exit(0);
			}

			for (Object value : row.values()) {
				insert.setValue(i, value);

				++i;
			}

			callback.produceStatement(insert);
		}
	}

	public Type getType(DataType type) {
		switch (type) {
			case BINARY:
				return BinaryType.BINARY;
			case BOOLEAN:
				return BooleanType.BOOLEAN;
			case BYTE:
				return ExactNumericType.TINYINT;
			case DOUBLE:
				return ApproximateNumericType.DOUBLE;
			case FLOAT:
				return ApproximateNumericType.FLOAT;
			case GUID:
				// xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
				return StringType.VARCHAR;
			case INT:
				return ExactNumericType.SMALLINT;
			case LONG:
				return ExactNumericType.INTEGER;
			case MEMO:
				return StringType.NVARCHAR;
			case MONEY:
				return MonetaryType.MONEY;
			case NUMERIC:
				return new DecimalType(type.getDefaultPrecision(), type.getDefaultScale());
			case OLE:
				return BinaryType.VARBINARY;
			case SHORT_DATE_TIME:
				return DateTimeType.DATETIME;
			case TEXT:
				return StringType.VARCHAR;
			case UNKNOWN_0D:
			case UNKNOWN_11:
				// "Unknown data. Handled like BINARY."
				return BinaryType.BINARY;
		}

		LOG.log(Level.WARNING, "Unhandled type: " + type);

		return null;
	}
}
