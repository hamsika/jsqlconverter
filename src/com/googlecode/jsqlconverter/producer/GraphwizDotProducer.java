package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;
import com.googlecode.jsqlconverter.producer.interfaces.FinalInterface;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

public class GraphwizDotProducer extends Producer implements CreateTableInterface, FinalInterface {
	private ArrayList<CreateTable> tableStatements = new ArrayList<CreateTable>();

	public GraphwizDotProducer(PrintStream out) {
		super(out);
	}

	public void doCreateTable(CreateTable table) throws ProducerException {
		tableStatements.add(table);
	}

	public void doFinal() throws ProducerException {
		// sort table list
		Collections.sort(tableStatements);

		out.println("digraph G {");
		out.println("	node [");
		out.println("		shape = \"record\"");
		out.println("	]");

		// create the nodes
		for (CreateTable table : tableStatements) {
			String tableName = table.getName().getObjectName();

			out.println();
			out.println("	" + tableName + " [");
			out.println("		label = \"{" + tableName + "|" + getColumnList(table) + "}\"");
			out.println("	]");
		}

		// create the links
		for (CreateTable table : tableStatements) {
			for (Column column : table.getColumns()) {
				ForeignKeyConstraint fkey = column.getForeignKeyConstraint();

				// TODO: check target table exists and output warning if it does not
				if (fkey != null) {
					out.println("	" + table.getName().getObjectName() + " -> " + fkey.getTableName().getObjectName());
				}
			}
		}

		out.println("}");
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
