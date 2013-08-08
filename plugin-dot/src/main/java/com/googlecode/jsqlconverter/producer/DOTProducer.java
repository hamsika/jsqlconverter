package com.googlecode.jsqlconverter.producer;

import java.io.PrintStream;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ColumnForeignKeyConstraint;
import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;
import com.googlecode.jsqlconverter.producer.interfaces.FinalInterface;

@ServiceName("DOT")
public class DOTProducer extends Producer implements CreateTableInterface, FinalInterface {
	// TODO: point out primary key, point out uniques, compound foreign keys!
	private StringBuffer relationshipBuffer = new StringBuffer();

	public DOTProducer(@ParameterName("Output") PrintStream out) {
		super(out);

		out.println("digraph DatabaseSchema {");
		out.println("	node [");
		out.println("		shape = \"record\"");
		out.println("	]");
	}

	@Override
	public void doCreateTable(CreateTable table) throws ProducerException {
		String tableName = table.getName().getObjectName();

		out.println();
		out.println("	" + tableName + " [");
		out.println("		label = \"{" + tableName + "|" + getColumnList(table) + "}\"");
		out.println("	]");

		for (Column column : table.getColumns()) {
			ColumnForeignKeyConstraint fkey = column.getForeignKeyConstraint();

			// if target table doesn't exist then an empty table will be created.
			// should probably output a warning if that is the case
			if (fkey != null) {
				relationshipBuffer.append("\t");
				relationshipBuffer.append(tableName);
				relationshipBuffer.append(" -> ");
				relationshipBuffer.append(fkey.getTableName().getObjectName());
				relationshipBuffer.append("\n");
			}
		}
	}

	@Override
	public void doFinal() throws ProducerException {
		out.println();

		out.print(relationshipBuffer);

		out.print("}");
	}

	private String getColumnList(CreateTable table) {
		StringBuffer sb = new StringBuffer();

		for (Column column : table.getColumns()) {
			sb.append(column.getName().getObjectName());

			sb.append("\\l");
		}

		return sb.toString();
	}
}
