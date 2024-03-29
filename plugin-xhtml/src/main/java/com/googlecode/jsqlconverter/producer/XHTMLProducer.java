package com.googlecode.jsqlconverter.producer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ColumnForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;
import com.googlecode.jsqlconverter.producer.interfaces.FinalInterface;

// TODO: support showing create index
@ServiceName("XHTML")
public class XHTMLProducer extends Producer implements CreateTableInterface, FinalInterface {
	private ArrayList<CreateTable> tableStatements = new ArrayList<CreateTable>();

	public XHTMLProducer(@ParameterName("Output") PrintStream out) {
		super(out);
	}

	@Override
	public void doCreateTable(CreateTable table) throws ProducerException {
		tableStatements.add(table);
	}

	@Override
	public void doFinal() throws ProducerException {
		printHeader();

		// sort table list
		Collections.sort(tableStatements);

		printSummary();

		printTables();

		printFooter();
	}

	private void printSummary() {
		out.println("	<h2>Summary</h2>\n");

		out.println(
			"	<div id=\"summary\">\n" +
			"		<table>\n" +
			"			<tr>\n" +
			"				<th>Table</th>\n" +
			"				<th>Columns</th>\n" +
			"			</tr>"
		);

		for (CreateTable table : tableStatements) {
			String tableName = table.getName().getObjectName();

			out.print(
				"			<tr>\n" +
				"				<td><a href=\"#" + tableName + "\">" + tableName + "</a></td>\n" +
				"				<td>" + table.getColumns().length + "</td>\n" +
				"			</tr>\n"
			);
		}

		out.println(
			"		</table>\n" +
			"	</div>\n"
		);
	}

	private void printTables() {
		out.println("	<h2>Tables</h2>\n");

		for (CreateTable table : tableStatements) {
			String tableName = table.getName().getObjectName();

			out.println("	<h3 id=\"" + tableName + "\">" + tableName + "</h3>\n");
			out.println("	<table>");
			out.println("		<tr>");
			out.println("			<th>Name</th>");
			out.println("			<th>Type</th>");
			out.println("			<th>Size</th>");
			out.println("			<th>Primary Key</th>");
			out.println("			<th>Auto Increment</th>");
			out.println("			<th>Not Null</th>");
			out.println("			<th>Unique</th>");
			out.println("			<th>Default Value</th>");
			out.println("			<th>Foreign Key</th>");
			out.println("		</tr>");

			for (Column column : table.getColumns()) {
				out.println("		<tr>");
				out.println("			<td>" + column.getName().getObjectName() + "</td>");
				out.println("			<td>" + column.getType() + "</td>");
				out.println("			<td>" + column.getSize() + "</td>");
				out.println("			<td>" + column.containsOption(ColumnOption.PRIMARY_KEY) + "</td>");
				out.println("			<td>" + column.containsOption(ColumnOption.AUTO_INCREMENT) + "</td>");
				out.println("			<td>" + column.containsOption(ColumnOption.NOT_NULL) + "</td>");
				out.println("			<td>" + column.containsOption(ColumnOption.UNIQUE) + "</td>");
				out.println("			<td>" + getDefaultValue(column.getDefaultConstraint()) + "</td>");
				out.println("			<td>" + getForeignKeyXHTML(column.getForeignKeyConstraint()) + "</td>");
				out.println("		</tr>");
			}

			// TODO: support compound primary keys
			// TODO: support compound unique keys
			// TODO: support compound foreign keys

			out.println("	</table>\n\n");
		}
	}

	private String getDefaultValue(DefaultConstraint defaultConstraint) {
		if (defaultConstraint == null) {
			return "";
		}

		return defaultConstraint.getValue();
	}

	private String getForeignKeyXHTML(ColumnForeignKeyConstraint foreignKeyConstraint) {
		if (foreignKeyConstraint == null) {
			return "";
		}

		String tableName = foreignKeyConstraint.getTableName().getObjectName();

		return "<a href=\"#" + tableName + "\">" + tableName + "</a> (" + foreignKeyConstraint.getColumnName().getObjectName() + ")";
	}

	private void printHeader() {
		out.println(
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n" +
			"    \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n" +
			"  <head>\n" +
			"    <title>XHTML Database Schema - jsqlconverter</title>\n" +
			"    <meta http-equiv=\"Content-Type\" content=\"application/xhtml+xml; charset=utf-8\" />\n" +
			"    <style type=\"text/css\">\n" +
			"	td, th {\n" +
			"		border: 1px solid black;\n" +
			"	}\n" +
			"\n" +
			"	th {\n" +
			"		text-align: left;\n" +
			"		background: lightgrey;\n" +
			"	}\n" +
			"    </style>" +
			"  </head>\n" +
			"  <body>\n"
		);
	}

	private void printFooter() {
		out.println(
			"	<p id=\"footer\">- generated by <a href=\"http://code.google.com/p/jsqlconverter/\">jsqlconverter</a></p>\n\n" +
			"  </body>\n" +
			"</html>"
		);
	}
}
