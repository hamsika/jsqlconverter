package com.googlecode.jsqlconverter.parser.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.parser.Parser;
import com.googlecode.jsqlconverter.parser.ParserException;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;

public abstract class XMLParser extends Parser {
	protected BufferedInputStream in;
	private XPath xpath;

	public XMLParser(BufferedInputStream in) {
		this.in = in;
	}

	public XMLParser(File zipFile, String xmlFileName) throws IOException {
		ZipFile zip = new ZipFile(zipFile, ZipFile.OPEN_READ);

		in = new BufferedInputStream(zip.getInputStream(zip.getEntry(xmlFileName)));
	}

	public void parse(ParserCallback callback) throws ParserException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ParserException(e.getMessage(), e.getCause());
		}

		Document document;

		try {
			document = builder.parse(in);
		} catch (Exception e) {
			throw new ParserException(e.getMessage(), e.getCause());
		}

		xpath = XPathFactory.newInstance().newXPath();

		// TODO: support several DB (schema)

		try {
			NodeList nodes = (NodeList) xpath.compile(getTableListPath()).evaluate(document, XPathConstants.NODESET);

			doTables(callback, nodes);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	private void doTables(ParserCallback callback, NodeList tableNodes) throws XPathExpressionException {
		for (int i = 0; i < tableNodes.getLength(); i++) {
			doTable(callback, (Element)tableNodes.item(i));
		}
	}

	private void doTable(ParserCallback callback, Element tableNode) throws XPathExpressionException {
		String tableName = (String) xpath.evaluate(getTableNamePath(), tableNode, XPathConstants.STRING);

		CreateTable ct = new CreateTable(new Name(tableName));

		NodeList columnNodes = (NodeList) xpath.compile(getColumnListPath(tableNode)).evaluate(tableNode, XPathConstants.NODESET);

		for (int i=0; i<columnNodes.getLength(); i++) {
			Column column = getColumn((Element)columnNodes.item(i));

			ct.addColumn(column);
		}

		// TODO detect foreign-keys
		// getFkeyBlah
		//  getDeleteAction
		//  getUpdateAction
		// maybe even use isNoAction, isUpdateAction, isCascadeAction, etc

		// TODO detect unique columns
		// getUniqueColumnsPath(tableNode)

		// TODO find indices
		// getIndexesPath();

		// send create table statement to be produced
		callback.produceStatement(ct);

		//System.exit(0);

		// TODO detect non-unique table indexes
	}

	private Column getColumn(Element columnElement) throws XPathExpressionException {
		String columnName = (String) xpath.evaluate(getColumnNamePath(), columnElement, XPathConstants.STRING);
		boolean isPrimaryKey = Boolean.parseBoolean((String) xpath.evaluate(getPrimaryKeyPath(), columnElement, XPathConstants.STRING));
		boolean isAutoIncr = Boolean.parseBoolean((String) xpath.evaluate(getAutoIncrementPath(), columnElement, XPathConstants.STRING));
		boolean isRequired = Boolean.parseBoolean((String) xpath.evaluate(getIsRequiredPath(), columnElement, XPathConstants.STRING));
		String defaultValue = (String) xpath.evaluate(getDefaultValuePath(), columnElement, XPathConstants.STRING);
		String colType = (String) xpath.evaluate(getDataTypePath(columnElement), columnElement, XPathConstants.STRING);
		String colSize = "";

		if (columnPathContainsSize()) {
			if (colType.contains("(")) {
				colSize = colType.substring(
					colType.indexOf('(') + 1,
					colType.indexOf(')')
				);
			}
		} else {
			colSize = (String) xpath.evaluate(getColumnSizePath(), columnElement, XPathConstants.STRING);
		}

		int size = 0;

		if (!colSize.isEmpty()) {
			size = Integer.parseInt(colSize);
		}

		Column column = new Column(new Name(columnName), getType(colType, size));
		column.setSize(size);

		if (isPrimaryKey) {
			column.addColumnOption(ColumnOption.PRIMARY_KEY);
		}

		if (isAutoIncr) {
			column.addColumnOption(ColumnOption.AUTO_INCREMENT);
		}

		if (isRequired) {
			column.addColumnOption(ColumnOption.NOT_NULL);
		}

		if (!defaultValue.isEmpty()) {
			column.setDefaultConstraint(new DefaultConstraint(defaultValue));
		}

		return column;
	}

	public abstract String getTableListPath();
	public abstract String getTableNamePath();

	public abstract String getColumnListPath(Element tableNode);
	public abstract String getColumnNamePath();
	public abstract String getPrimaryKeyPath();
	public abstract String getAutoIncrementPath();
	public abstract String getIsRequiredPath();
	public abstract String getColumnSizePath();
	public abstract String getDefaultValuePath();
	public abstract String getDataTypePath(Element columnElement);

	public abstract boolean columnPathContainsSize();

	public abstract Type getType(String type, int size);
}
