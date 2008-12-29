package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.parser.callback.ParserCallback;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.DefaultConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.logging.LogLevel;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;

public class TurbineXMLParser extends Parser {
	private InputStream in;

	public TurbineXMLParser(InputStream in) {
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

		NodeList databaseNodes = document.getElementsByTagName("database");
		int numDbs = databaseNodes.getLength();

		if (numDbs > 1) {
			log.log(LogLevel.WARNING, "More than one database definition found");
		}

		for (int i=0; i<numDbs; i++) {
			Element database = (Element) databaseNodes.item(i);

			doTables(callback, database);
		}
	}

	private void doTables(ParserCallback callback, Element database) {
		// get table
		NodeList tableNodes = database.getElementsByTagName("table");

		for (int i=0; i<tableNodes.getLength(); i++) {
			doTable(callback, (Element)tableNodes.item(i));
		}
	}

	private void doTable(ParserCallback callback, Element table) {
		CreateTable ct = new CreateTable(new Name(table.getAttribute("name")));

		NodeList columnNodes = table.getElementsByTagName("column");
		NodeList fkeyNodes = table.getElementsByTagName("foreign-key");
		NodeList uniqueNodes = table.getElementsByTagName("unique");
		NodeList indexNodes = table.getElementsByTagName("index");

		for (int i=0; i<columnNodes.getLength(); i++) {
			Column column = getColumn((Element)columnNodes.item(i));

			ct.addColumn(column);
		}

		// detect foreign-keys
		setForeignKeys(ct, fkeyNodes);

		// detect unique columns
		setUniques(ct, uniqueNodes);

		// send create table statement to be produced
		callback.produceStatement(ct);

		// detect indexes
		doIndexes(callback, ct.getName(), indexNodes);
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
		String isPrimaryKey = columnElement.getAttribute("primaryKey");
		String isAutoIncr = columnElement.getAttribute("autoIncrement");
		String isRequired = columnElement.getAttribute("required");
		String colSize = columnElement.getAttribute("size");
		String defaultValue = columnElement.getAttribute("defaultValue");
		int size = 0;

		if (!colSize.equals("")) {
			size = Integer.parseInt(colSize);
		}

		Column column = new Column(new Name(columnElement.getAttribute("name")), getType(columnElement.getAttribute("type"), size));
		column.setSize(size);

		if (isPrimaryKey.equals("true")) {
			column.addColumnOption(ColumnOption.PRIMARY_KEY);
		}

		if (isAutoIncr.equals("true")) {
			column.addColumnOption(ColumnOption.AUTO_INCREMENT);
		}

		if (isRequired.equals("true")) {
			column.addColumnOption(ColumnOption.NOT_NULL);
		}



		if (!defaultValue.equals("")) {
			column.setDefaultConstraint(new DefaultConstraint(defaultValue));
		}

		return column;
	}

	private Type getType(String type, int size) {
		if (type.equals("ARRAY")) {
		} else if (type.equals("BIGINT")) {
			return ExactNumericType.BIGINT;
		} else if (type.equals("BINARY")) {
			return BinaryType.BINARY;
		} else if (type.equals("BLOB")) {
			return BinaryType.BLOB;
		} else if (type.equals("BOOLEANCHAR")) {
			return BooleanType.BOOLEAN; // TODO: check this
		} else if (type.equals("BOOLEANINT")) {
			return BooleanType.BOOLEAN; // TODO: check this
		} else if (type.equals("BIT")) {
			return BinaryType.BIT;
		} else if (type.equals("CHAR")) {
			return StringType.CHAR;
		} else if (type.equals("CLOB")) {

		} else if (type.equals("DATE")) {

		} else if (type.equals("DECIMAL")) {
			return new DecimalType(size);
		} else if (type.equals("DISTINCT")) {
		} else if (type.equals("DOUBLE")) {
			return ApproximateNumericType.DOUBLE;
		} else if (type.equals("FLOAT")) {
			return ApproximateNumericType.FLOAT;
		} else if (type.equals("INTEGER")) {
			return ExactNumericType.INTEGER;
		} else if (type.equals("JAVA_OBJECT")) {
		} else if (type.equals("LONGVARBINARY")) {
		} else if (type.equals("LONGVARCHAR")) {
		} else if (type.equals("NULL")) {
		} else if (type.equals("NUMERIC")) {
		} else if (type.equals("OTHER")) {
		} else if (type.equals("REAL")) {
			return ApproximateNumericType.REAL;
		} else if (type.equals("REF")) {
		} else if (type.equals("SMALLINT")) {
			return ExactNumericType.SMALLINT;
		} else if (type.equals("STRUCT")) {
		} else if (type.equals("TIME")) {
		} else if (type.equals("TIMESTAMP")) {
		} else if (type.equals("TINYINT")) {
			return ExactNumericType.TINYINT;
		} else if (type.equals("VARBINARY")) {
			return BinaryType.VARBINARY;
		} else if (type.equals("VARCHAR")) {
			return StringType.VARCHAR;
		}

		log.log(LogLevel.UNHANDLED, "Unhandled type: " + type);

		return StringType.VARCHAR;
	}
}
