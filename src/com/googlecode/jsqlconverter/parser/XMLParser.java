package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.parser.callback.ParserCallback;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.type.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public abstract class XMLParser extends Parser {
	private InputStream in;

	public XMLParser(InputStream in) {
		this.in = in;
	}

	public void parse(ParserCallback callback) throws ParserException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;
		Document document;

		try {
			parser = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ParserException(e.getMessage(), e.getCause());
		}

		try {
			document = parser.parse(in);
		} catch (Exception e) {
			throw new ParserException(e.getMessage(), e.getCause());
		}

		NodeList databaseNodes = document.getElementsByTagName(getRootTagName());

		for (int i=0; i<databaseNodes.getLength(); i++) {
			Element database = (Element) databaseNodes.item(i);

			doTables(callback, database);
		}
	}

	private void doTables(ParserCallback callback, Element database) {
		// get table
		NodeList tableNodes = database.getElementsByTagName(getTableTagName());

		for (int i=0; i<tableNodes.getLength(); i++) {
			doTable(callback, (Element)tableNodes.item(i));
		}
	}

	private void doTable(ParserCallback callback, Element table) {
		CreateTable ct = new CreateTable(new Name(table.getAttribute("name")));

		NodeList columnNodes = table.getElementsByTagName(getColumnTagName());
		NodeList indexNodes = table.getElementsByTagName("index");

		for (int i=0; i<columnNodes.getLength(); i++) {
			Column column = getColumn((Element)columnNodes.item(i));

			ct.addColumn(column);
		}

		// detect foreign-keys
		if (supportsForeignKeyTag()) {
			NodeList fkeyNodes = table.getElementsByTagName("foreign-key");

			setForeignKeys(ct, fkeyNodes);
		}

		// detect unique columns
		if (supportsUniqueKeyTag()) {
			NodeList uniqueNodes = table.getElementsByTagName("unique");

			setUniques(ct, uniqueNodes);
		}

		// find indices
		CreateIndex[] indexes = null;

		if (supportsIndicesKeyTag()) {
			indexes = doIndices(ct, indexNodes);
		}

		// send create table statement to be produced
		callback.produceStatement(ct);

		// detect non-unique table indexes
		if (supportsIndexKeyTag()) {
			doIndexes(callback, ct.getName(), indexNodes);
		}

		if (supportsIndicesKeyTag() && indexes != null) {
			for (CreateIndex ci : indexes) {
				callback.produceStatement(ci);
			}
		}
	}

	private void setForeignKeys(CreateTable ct, NodeList fkeyNodes) {
		for (int i=0; i<fkeyNodes.getLength(); i++) {
			Element fkeyElement = (Element)fkeyNodes.item(i);

			Name tableName = new Name(fkeyElement.getAttribute("foreignTable"));

			NodeList referenceNodes = fkeyElement.getElementsByTagName("reference");

			for (int k=0; k<referenceNodes.getLength(); k++) {
				Element referenceElement = (Element)referenceNodes.item(k);

				ForeignKeyConstraint fkey = new ForeignKeyConstraint(tableName, new Name(referenceElement.getAttribute("foreign")));

				String localColumn = referenceElement.getAttribute("local");

				// TODO: may need to add some checking / error message to make sure that the column was def found
				for (Column column : ct.getColumns()) {
					if (column.getName().getObjectName().equals(localColumn)) {
						column.setForeignKeyConstraint(fkey);
						break;
					}
				}
			}
		}
	}

	private void setUniques(CreateTable ct, NodeList uniqueNodes) {
		for (int i=0; i<uniqueNodes.getLength(); i++) {
			Element indexElement = (Element)uniqueNodes.item(i);

			NodeList uniqueColumnNodes = indexElement.getElementsByTagName("unique-column");

			for (int k=0; k<uniqueColumnNodes.getLength(); k++) {
				Element referenceElement = (Element)uniqueColumnNodes.item(k);

				String indexColumn = referenceElement.getAttribute("name");

				// TODO: may need to add some checking / error message to make sure that the column was def found
				for (Column column : ct.getColumns()) {
					if (column.getName().getObjectName().equals(indexColumn)) {
						column.addColumnOption(ColumnOption.UNIQUE);
						break;
					}
				}
			}
		}
	}

	private CreateIndex[] doIndices(CreateTable ct, NodeList indecesNodes) {
		// TODO: find out if unique is supported for these (type param?)
		ArrayList<CreateIndex> indexList = new ArrayList<CreateIndex>();

		for (int i=0; i<indecesNodes.getLength(); i++) {
			Element indexElement = (Element)indecesNodes.item(i);

			CreateIndex ci = new CreateIndex(new Name(indexElement.getAttribute("name")), ct.getName());

			String[] fields = indexElement.getAttribute("fields").split(",");

			for (String field : fields) {
				ci.addColumn(new Name(field.trim()));
			}

			indexList.add(ci);
		}

		return indexList.toArray(new CreateIndex[indexList.size()]);
	}

	private void doIndexes(ParserCallback callback, Name tableName, NodeList indexNodes) {
		for (int i=0; i<indexNodes.getLength(); i++) {
			Element indexElement = (Element)indexNodes.item(i);

			NodeList indexColumnNodes = indexElement.getElementsByTagName("index-column");

			CreateIndex ci = new CreateIndex(new Name(indexElement.getAttribute("name")), tableName);

			for (int k=0; k<indexColumnNodes.getLength(); k++) {
				Element referenceElement = (Element)indexColumnNodes.item(k);

				String indexColumn = referenceElement.getAttribute("name");

				ci.addColumn(new Name(indexColumn));
			}

			callback.produceStatement(ci);
		}
	}

	private Column getColumn(Element columnElement) {
		String isPrimaryKey = columnElement.getAttribute(getPrimaryKeyAttributeName());
		String isAutoIncr = columnElement.getAttribute(getAutoIncrementAttributeName());
		String isRequired = columnElement.getAttribute(getIsRequiredAttributeName());
		String colSize = columnElement.getAttribute(getSizeAttributeName());
		String defaultValue = columnElement.getAttribute("defaultValue");
		int size = 0;

		if (!colSize.equals("")) {
			size = Integer.parseInt(colSize);
		}

		Column column = new Column(new Name(columnElement.getAttribute("name")), getType(columnElement.getAttribute(getDataTypeAttributeName()), size));
		column.setSize(size);

		if (isPrimaryKey.equals("true") || isPrimaryKey.equals("1")) {
			column.addColumnOption(ColumnOption.PRIMARY_KEY);
		}

		if (isAutoIncr.equals("true") || isAutoIncr.equals("1")) {
			column.addColumnOption(ColumnOption.AUTO_INCREMENT);
		}

		// if it's 0, then it shoudl be SQL Fairy. Otherwise this would be a bad bag!
		if (isRequired.equals("true") || isRequired.equals("0")) {
			column.addColumnOption(ColumnOption.NOT_NULL);
		}

		if (!defaultValue.equals("")) {
			column.setDefaultConstraint(new DefaultConstraint(defaultValue));
		}

		return column;
	}

	public abstract String getRootTagName();
	public abstract String getTableTagName();
	public abstract String getColumnTagName();

	public abstract String getDataTypeAttributeName();

	public abstract String getPrimaryKeyAttributeName();
	public abstract String getAutoIncrementAttributeName();
	public abstract String getIsRequiredAttributeName();
	public abstract String getSizeAttributeName();

	public abstract boolean supportsForeignKeyTag();
	public abstract boolean supportsUniqueKeyTag();
	public abstract boolean supportsIndexKeyTag();
	public abstract boolean supportsIndicesKeyTag();

	public abstract Type getType(String type, int size);
}
