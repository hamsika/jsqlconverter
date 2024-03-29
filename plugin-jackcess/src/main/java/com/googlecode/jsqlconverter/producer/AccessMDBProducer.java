package com.googlecode.jsqlconverter.producer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
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
import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;
import com.googlecode.jsqlconverter.producer.interfaces.InsertFromValuesInterface;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

@ServiceName("Access MDB")
public class AccessMDBProducer extends Producer implements CreateTableInterface, InsertFromValuesInterface {
	private Database db;

	public AccessMDBProducer(@ParameterName("File") File mdbFile) throws IOException {
		// TODO: check if the file already exists, if it does then 'open' it instead of creating new db
		// TODO: check if we should 'auto sync' or not
		db = Database.create(mdbFile, false);
	}

	/*public void doCreateIndex(CreateIndex index) throws ProducerException {
		Table table;

		try {
			table = db.getTable(index.getTableName().getObjectName());
		} catch (IOException e) {
			throw new ProducerException(e.getMessage(), e.getCause());
		}

		//Cursor.createIndexCursor(table, new Index.FIRST_ENTRY)
		//new CursorBuilder(table).setIndexByColumns()

		//new SimpleIndex(table, uniqueentrycount, uniqueentrycountoffset).is
	}*/

	@Override
	public void doCreateTable(CreateTable table) throws ProducerException {
		TableBuilder tb = new TableBuilder(table.getName().getObjectName());

		for (com.googlecode.jsqlconverter.definition.create.table.Column column : table.getColumns()) {
			Column jcol = new Column();

			jcol.setName(column.getName().getObjectName());
			jcol.setType(getType(column.getType()));

			if (column.getSize() != 0) {
				jcol.setLength((short)column.getSize());
			}

			// auto number is only supported by long int type
			if (column.containsOption(ColumnOption.AUTO_INCREMENT) && jcol.getType() == DataType.LONG) {
				jcol.setAutoNumber(true);
			}

			tb.addColumn(jcol);
		}

		// TODO: relationships

		try {
			tb.toTable(db);
		} catch (IOException e) {
			throw new ProducerException(e.getMessage(), e.getCause());
		}

		// TODO: indexes
		// compound indexes (primary, foreign, unique)
	}

	@Override
	public void doInsertFromValues(InsertFromValues insert) throws ProducerException {
		Table table;

		try {
			table = db.getTable(insert.getTableName().getObjectName());
		} catch (IOException e) {
			throw new ProducerException(e.getMessage(), e.getCause());
		}

		try {
			table.addRow(getRowData(insert));
		} catch (IOException e) {
			throw new ProducerException(e.getMessage(), e.getCause());
		}
	}

	private Object getRowData(InsertFromValues insert) {
		ArrayList<Object> objectArray = new ArrayList<Object>();

		for (int i=0; i<insert.getColumnCount(); i++) {
			Object value = insert.getObject(i);

			objectArray.add(value);
		}

		return objectArray.toArray(new Object[objectArray.size()]);
	}

	public DataType getType(Type type) {
		DataType dataType = null;

		if (type instanceof ApproximateNumericType) {
			dataType = getType((ApproximateNumericType)type);
		} else if (type instanceof BinaryType) {
			dataType = getType((BinaryType)type);
		} else if (type instanceof BooleanType) {
			dataType = getType((BooleanType)type);
		} else if (type instanceof DateTimeType) {
			dataType = getType((DateTimeType)type);
		} else if (type instanceof DecimalType) {
			dataType = getType((DecimalType)type);
		} else if (type instanceof ExactNumericType) {
			dataType = getType((ExactNumericType)type);
		} else if (type instanceof MonetaryType) {
			dataType = getType((MonetaryType)type);
		} else if (type instanceof StringType) {
			dataType = getType((StringType)type);
		}

		return dataType;
	}

	public DataType getType(StringType type) {
		switch(type) {
			case LONGTEXT:
			case MEDIUMTEXT:
			case NTEXT:
			case TEXT:
			case TINYTEXT:
			case VARCHAR:
				return DataType.TEXT;
			case CHAR:
			case NCHAR:
			case NVARCHAR:
				return DataType.MEMO;
			default:
				return null;
		}
	}

	public DataType getType(ApproximateNumericType type) {
		switch (type) {
			case DOUBLE:
				return DataType.DOUBLE;
			case FLOAT:
			case REAL:
				return DataType.FLOAT;
			default:
				return null;
		}
	}

	public DataType getType(BinaryType type) {
		switch(type) {
			case BIT:
			case BINARY:
				return DataType.BINARY;
			case BLOB:
			case LONGBLOB:
			case MEDIUMBLOB:
			case TINYBLOB:
			case VARBINARY:
				return DataType.OLE;
			default:
				return null;
		}
	}

	public DataType getType(BooleanType type) {
		switch(type) {
			case BOOLEAN:
				return DataType.BOOLEAN;
			default:
				return null;
		}
	}

	public DataType getType(DateTimeType type) {
		switch(type) {
			case DATE:
			case DATETIME:
			case TIME:
			case TIMESTAMP:
				return DataType.SHORT_DATE_TIME;
			default:
				return null;
		}
	}

	public DataType getType(ExactNumericType type) {
		switch(type) {
			case BIGINT:
			case INTEGER:
			case MEDIUMINT:
				return DataType.LONG;
			case SMALLINT:
				return DataType.INT;
			case TINYINT:
				return DataType.BYTE;
			default:
				return null;
		}
	}

	public DataType getType(MonetaryType type) {
		switch(type) {
			case MONEY:
			case SMALLMONEY:
				return DataType.MONEY;
			default:
				return null;
		}
	}

	public DataType getType(DecimalType type) {
		return DataType.NUMERIC;
	}
}
