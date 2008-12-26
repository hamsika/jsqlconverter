package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.truncate.table.Truncate;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.create.table.*;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Column;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class AccessMDBProducer extends Producer {
	private Database db;

	public AccessMDBProducer(File mdbFile) {
		try {
			// TODO: check if the file already exists, if it does then 'open' it instead of creating new db
			db = Database.create(mdbFile);
		} catch (IOException e) {
			//throw new ProducerException(e.getMessage(), e.getCause());
			// TODO: complete this
			e.printStackTrace();
		}
	}

	public void doCreateIndex(CreateIndex index) {
		
	}

	public void doCreateTable(CreateTable table) {
		TableBuilder tb = new TableBuilder(table.getName().getObjectName());

		for (com.googlecode.jsqlconverter.definition.create.table.Column column : table.getColumns()) {
			Column jcol = new Column();

			jcol.setName(column.getName().getObjectName());
			jcol.setType(getType(column.getType()));

			// TODO: setType, setSize

			tb.addColumn(jcol);
		}

		try {
			tb.toTable(db);
		} catch (IOException e) {
			// TODO: complete this
			//throw new ProducerException(e.getMessage(), e.getCause());
			e.printStackTrace();
		}

		// TODO: indexes
	}

	public void doInsertFromValues(InsertFromValues insert) {
		Table table;

		try {
			table = db.getTable(insert.getTableName().getObjectName());
		} catch (IOException e) {
			e.printStackTrace();  // TODO: complete this
			System.exit(0);
			return;
		}

		try {
			table.addRow(getRowData(insert));
		} catch (IOException e) {
			e.printStackTrace(); // TODO: complete this
			System.exit(0);
		}
	}

	public void doTruncate(Truncate truncate) {
		
	}

	private Object getRowData(InsertFromValues insert) {
		ArrayList<Object> objectArray = new ArrayList<Object>();

		for (int i=0; i<insert.getColumnCount(); i++) {
			Object value = insert.getObject(i);

			objectArray.add(value);
		}

		return objectArray.toArray(new Object[] {});
	}

	private DataType getType(Type type) {
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

		if (dataType == null) {
			// TOOD: message unable to get datatype
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
			case DATETIME:
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
