package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.parser.callback.ParserCallback;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.logging.LogLevel;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Column;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class AccessMDBParser extends Parser {
	private File mdbFile;

	public AccessMDBParser(File mdbPath) {
		this.mdbFile = mdbPath;
	}

	public void parse(ParserCallback callback) throws ParserException {
		Database db;

		try {
			db = Database.open(mdbFile, false);
		} catch (IOException e) {
			throw new ParserException(e.getMessage(), e.getCause());
		}

		for (String tableName : db.getTableNames()) {
			Table table;

			try {
				table = db.getTable(tableName);
			} catch (IOException e) {
				throw new ParserException(e.getMessage(), e.getCause());
			}

			CreateTable ct = new CreateTable(new Name(table.getName()));
			ArrayList<CreateIndex> createIndexes = new ArrayList<CreateIndex>(); // normal non-unique indexes

			for (Column column : table.getColumns()) {
				com.googlecode.jsqlconverter.definition.create.table.Column col = new com.googlecode.jsqlconverter.definition.create.table.Column(new Name(column.getName()), getMyDataType(column.getType()));

				ct.addColumn(col);
			}

			// check out indexes
			for (Index index : table.getIndexes()) {
				if (index.isForeignKey()) {
					// TODO: this probably doesn't work right. the column in "our" ct may not be the same column name in the referenced table
					String colName = index.getColumns().get(0).getName();

					getColumn(ct, colName).setForeignKeyConstraint(new ForeignKeyConstraint(new Name(index.getTable().getName()), new Name(colName)));
				} else if (index.isPrimaryKey()) {
					List<Index.ColumnDescriptor> indexes = index.getColumns();

					if (indexes.size() > 1) {
						ArrayList<Name> columnNames = new ArrayList<Name>();

						for (Index.ColumnDescriptor cd : indexes) {
							columnNames.add(new Name(cd.getName()));
						}

						ct.setPrimaryCompoundKeyConstraint(new KeyConstraint(columnNames.toArray(new Name[] {})));
					} else {
						// single column index
						// may need some additional error checking / message to ensure that the PRIMARY_KEY constraint was added
						String indexColumn = indexes.get(0).getName();

						getColumn(ct, indexColumn).addColumnOption(ColumnOption.PRIMARY_KEY);
					}
				} else if (index.isUnique()) {
					// add column constraint
					getColumn(ct, index.getName()).addColumnOption(ColumnOption.UNIQUE);
				} else {
					// just output it! lol
					CreateIndex ci = new CreateIndex(new Name(index.getName()), ct.getName());

					for (Index.ColumnDescriptor cd : index.getColumns()) {
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
		}
	}

	private com.googlecode.jsqlconverter.definition.create.table.Column getColumn(CreateTable ct, String columnName) {
		for (com.googlecode.jsqlconverter.definition.create.table.Column column : ct.getColumns()) {
			if (columnName.equals(column.getName().getObjectName())) {
				return column;
			}
		}

		return null;
	}

	private Type getMyDataType(DataType type) {
		switch (type) {
			case BINARY:
				return BinaryType.BINARY;
			case BOOLEAN:
				return BooleanType.BOOLEAN;
			case BYTE:

			break;
			case DOUBLE:
				return ApproximateNumericType.DOUBLE;
			case FLOAT:
				return ApproximateNumericType.FLOAT;
			case GUID:

			break;
			case INT:
				// TODO: check this is right
				return ExactNumericType.SMALLINT;
			case LONG:
				// TODO: check this is right
				return ExactNumericType.INTEGER;
			case MEMO:
				// TODO: check this is right
				return StringType.LONGTEXT;
			case MONEY:

			case NUMERIC:

			case OLE:

			case SHORT_DATE_TIME:
				
			break;
			case TEXT:
				// TODO: check this is right
				return StringType.TEXT;
			case UNKNOWN_0D:
				
		}

		log.log(LogLevel.UNHANDLED, "Unhandled type: " + type);

		return StringType.VARCHAR;
	}
}
