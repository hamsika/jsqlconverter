package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;
import com.googlecode.jsqlconverter.producer.interfaces.FinalInterface;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.PrintStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TurbineXMLProducer extends Producer implements CreateTableInterface, FinalInterface {
	private Document document;
	private Element databaseElement;

	public TurbineXMLProducer(PrintStream out) throws ParserConfigurationException, TransformerException {
		super(out);

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

		document = documentBuilder.newDocument();

		databaseElement = document.createElement("database");
		databaseElement.setAttribute("name", "mydatabase");

		document.appendChild(databaseElement);
	}

	/*public void doCreateIndex(CreateIndex index) {
		// TODO: support adding indexes to existing <table> elements.
		// should this support unique indexes also?


		//<index name="book_author_title">
		//	<index-column name="author_id"/>
		//	<index-column name="title_id"/>
		//</index>

		//<unique>
		//	<unique-column name="TABLE_NAME"/>
		//</unique>
	}*/

	public void doCreateTable(CreateTable table) {
		Element tableElement = document.createElement("table");

		tableElement.setAttribute("name", table.getName().getObjectName());

		for (Column column : table.getColumns()) {
			Element columnElement = document.createElement("column");

			columnElement.setAttribute("name", column.getName().getObjectName());
			columnElement.setAttribute("type", getType(column.getType()));

			if (column.containsOption(ColumnOption.PRIMARY_KEY)) {
				columnElement.setAttribute("primaryKey", "true");
			}

			if (column.containsOption(ColumnOption.AUTO_INCREMENT)) {
				columnElement.setAttribute("autoIncrement", "true");
			}

			if (column.containsOption(ColumnOption.NOT_NULL)) {
				columnElement.setAttribute("required", "true");
			}

			if (column.getSize() != 0) {
				columnElement.setAttribute("size", String.valueOf(column.getSize()));
			}

			if (column.getDefaultConstraint() != null) {
				columnElement.setAttribute("defaultValue", column.getDefaultConstraint().getValue());
			}

			tableElement.appendChild(columnElement);
		}

		// TODO: foreign keys

		// TODO: unique columns

		databaseElement.appendChild(tableElement);
	}

	public String getType(Type type) {
		if (type instanceof ApproximateNumericType) {
			switch ((ApproximateNumericType)type) {
				case DOUBLE:
					return "DOUBLE";
				case FLOAT:
					return "FLOAT";
				case REAL:
					return "REAL";
			}
		} else if (type instanceof BinaryType) {
			switch ((BinaryType)type) {
				case BINARY:
					return "BINARY";
				case BIT:
					return "BIT";
				case BLOB:
					return "BLOB";
				case VARBINARY:
					return "VARBINARY";
			}
		} else if (type instanceof BooleanType) {
			switch ((BooleanType)type) {
				case BOOLEAN:
					return "BOOLEANINT";
			}
		} else if (type instanceof DateTimeType) {
			switch ((DateTimeType)type) {
				case DATE:
				case DATETIME:
					return "DATE";
				case TIME:
				case TIMESTAMP:
					return "TIMESTAMP";
			}
		} else if (type instanceof DecimalType) {
			return "DECIMAL";
		} else if (type instanceof ExactNumericType) {
			switch ((ExactNumericType)type) {
				case BIGINT:
					return "BIGINT";
				case INTEGER:
					return "INTEGER";
				case MEDIUMINT:
					return "INTEGER";
				case SMALLINT:
					return "SMALLINT";
				case TINYINT:
					return "TINYINT";
			}
		} else if (type instanceof MonetaryType) {
			switch ((MonetaryType)type) {
				case MONEY:
				case SMALLMONEY:
			}
		} else if (type instanceof StringType) {
			switch ((StringType)type) {
				case CHAR:
				case NCHAR:
					return "CHAR";
				case NTEXT:
				case TEXT:
				case NVARCHAR:
				case VARCHAR:
					return "VARCHAR";
			}
		}

		return "OTHER";
	}

	public void doFinal() throws ProducerException {
		DOMSource source = new DOMSource(document);
		Result result;
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute("indent-number", 4);
		Transformer transformer;

		try {
			result = new StreamResult(new OutputStreamWriter(out, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new ProducerException(e.getMessage(), e.getCause());
		}

		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new ProducerException(e.getMessage(), e.getCause());
		}

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://db.apache.org/torque/dtd/database.dtd");

		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new ProducerException(e.getMessage(), e.getCause());
		}
	}
}
