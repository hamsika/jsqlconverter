package com.googlecode.jsqlconverter.producer.xml;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.type.ApproximateNumericType;
import com.googlecode.jsqlconverter.definition.type.BinaryType;
import com.googlecode.jsqlconverter.definition.type.BooleanType;
import com.googlecode.jsqlconverter.definition.type.DateTimeType;
import com.googlecode.jsqlconverter.definition.type.DecimalType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.definition.type.MonetaryType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.producer.Producer;
import com.googlecode.jsqlconverter.producer.ProducerException;
import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;
import com.googlecode.jsqlconverter.producer.interfaces.FinalInterface;

@ServiceName("Turbine")
public class TurbineXMLProducer extends Producer implements CreateTableInterface, FinalInterface {
	// TODO: support compound keys (primary, foreign, unique)
	private Document document;
	private Element databaseElement;

	public TurbineXMLProducer(@ParameterName("Output") PrintStream out) throws ParserConfigurationException, TransformerException {
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
				case LONGBLOB:
				case MEDIUMBLOB:
				case TINYBLOB:
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
					return "TIME";
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
					return "DOUBLE";
			}
		} else if (type instanceof StringType) {
			switch ((StringType)type) {
				case CHAR:
				case NCHAR:
					return "CHAR";
				case LONGTEXT:
				case MEDIUMTEXT:
				case NTEXT:
				case TEXT:
				case TINYTEXT:
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
