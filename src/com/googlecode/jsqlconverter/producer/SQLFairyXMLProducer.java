package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyAction;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyMatch;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;
import com.googlecode.jsqlconverter.producer.interfaces.FinalInterface;

import java.io.PrintStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

public class SQLFairyXMLProducer extends Producer implements CreateTableInterface, FinalInterface {
	private Document document;
	private Element schemaElement;
	private Element tablesElement;

	public SQLFairyXMLProducer(PrintStream out) throws ParserConfigurationException, TransformerException {
		super(out);

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

		document = documentBuilder.newDocument();

		schemaElement = document.createElement("schema");
		schemaElement.setAttribute("name", "");
		schemaElement.setAttribute("database", "");
		schemaElement.setAttribute("xmlns", "http://sqlfairy.sourceforge.net/sqlfairy.xml");

		document.appendChild(schemaElement);
	}

	/*public void doCreateIndex(CreateIndex index) {
		// TODO: support adding indices to existing <table> elements.
		// should this support unique indexes also?

		//<indices>
		//	<index name="foobar" type="NORMAL" fields="foo,bar" options="" />
		//</indices>
	}*/

	public void doCreateTable(CreateTable table) {
		if (tablesElement == null) {
			tablesElement = document.createElement("tables");
		}

		Element tableElement = document.createElement("table");

		tableElement.setAttribute("name", table.getName().getObjectName());

		Element fieldsElement = document.createElement("fields");

		for (Column column : table.getColumns()) {
			Element fieldElement = document.createElement("field");

			fieldElement.setAttribute("name", column.getName().getObjectName());
			fieldElement.setAttribute("data_type", getType(column.getType()));

			if (column.containsOption(ColumnOption.PRIMARY_KEY)) {
				fieldElement.setAttribute("is_primary_key", "1");
			}

			if (column.containsOption(ColumnOption.AUTO_INCREMENT)) {
				fieldElement.setAttribute("is_auto_increment", "1");
			}

			if (column.containsOption(ColumnOption.NULL)) {
				fieldElement.setAttribute("is_nullable", "1");
			}

			if (column.getSize() != 0) {
				fieldElement.setAttribute("size", String.valueOf(column.getSize()));
			}

			fieldsElement.appendChild(fieldElement);
		}

		tableElement.appendChild(fieldsElement);

		// add foreign keys & uniques
		Element constraints = addConstraints(table);

		if (constraints != null) {
			tableElement.appendChild(constraints);
		}

		schemaElement.appendChild(tablesElement);
		tablesElement.appendChild(tableElement);
	}

	private Element addConstraints(CreateTable table) {
		Element constraints = document.createElement("constraints");

		for (Column column : table.getColumns()) {
			if (column.containsOption(ColumnOption.NOT_NULL)) {
				Element nullConstraint = document.createElement("constraint");

				nullConstraint.setAttribute("type", "NOT NULL");
				nullConstraint.setAttribute("fields", column.getName().getObjectName());

				constraints.appendChild(nullConstraint);
			}

			ForeignKeyConstraint fkey = column.getForeignKeyConstraint();

			if (fkey != null) {
				Element fkeyConstraint = document.createElement("constraint");

				fkeyConstraint.setAttribute("type", "FOREIGN KEY");
				fkeyConstraint.setAttribute("fields", column.getName().getObjectName());
				fkeyConstraint.setAttribute("reference_table", fkey.getTableName().getObjectName());
				fkeyConstraint.setAttribute("reference_fields", fkey.getColumnName().getObjectName());

				if (fkey.getUpdateAction() != null) {
					fkeyConstraint.setAttribute("on_update", getAction(fkey.getUpdateAction()));
				}

				if (fkey.getDeleteAction() != null) {
					fkeyConstraint.setAttribute("on_delete", getAction(fkey.getDeleteAction()));
				}

				if (fkey.getMatch() != null) {
					fkeyConstraint.setAttribute("match_type", getMatch(fkey.getMatch()));
				}

				constraints.appendChild(fkeyConstraint);
			}
		}

		if (constraints.getChildNodes().getLength() == 0) {
			return null;
		}

		return constraints;
	}

	private String getAction(ForeignKeyAction action) {
		switch (action) {
			case CASCADE:
				return "cascade";
			case NO_ACTION:
				return "no_action";
			case RESTRICT:
				return "restrict";
			case SET_DEFAULT:
				return "set default";
			case SET_NULL:
				return "set null";
		}

		log.warning("Unknown action: " + action);

		return null;
	}

	private String getMatch(ForeignKeyMatch match) {
		switch (match) {
			case FULL:
				return "full";
			case PARTIAL:
				return "partial";
			case SIMPLE:
				return "simple";
		}

		log.warning("Unknown action: " + match);

		return null;
	}

	private String getType(Type type) {
		String dataTypeString = null;

		if (type instanceof ApproximateNumericType) {
			dataTypeString = getType((ApproximateNumericType)type);
		} else if (type instanceof BinaryType) {
			dataTypeString = getType((BinaryType)type);
		} else if (type instanceof BooleanType) {
			dataTypeString = getType((BooleanType)type);
		} else if (type instanceof DateTimeType) {
			dataTypeString = getType((DateTimeType)type);
		} else if (type instanceof DecimalType) {
			dataTypeString = getType((DecimalType)type);
		} else if (type instanceof ExactNumericType) {
			dataTypeString = getType((ExactNumericType)type);
		} else if (type instanceof MonetaryType) {
			dataTypeString = getType((MonetaryType)type);
		} else if (type instanceof StringType) {
			dataTypeString = getType((StringType)type);
		}

		if (dataTypeString == null) {
			dataTypeString = type.toString();
		}

		return dataTypeString;
	}

	public String getType(ApproximateNumericType type) {
		switch (type) {
			case DOUBLE:
				return "double";
			case FLOAT:
				return "float";
			case REAL:
				return "real";
			default:
				return null;
		}
	}

	public String getType(BinaryType type) {
		switch(type) {
			case BINARY:
				return "binary";
			case BIT:
				return "bit";
			case BLOB:
				return "blob";
			case LONGBLOB:
				return "longblob";
			case MEDIUMBLOB:
				return "mediumblob";
			case TINYBLOB:
				return "tinyblob";
			case VARBINARY:
				return "varbinary";
			default:
				return null;
		}
	}

	public String getType(BooleanType type) {
		switch(type) {
			case BOOLEAN:
				return "boolean";
			default:
				return null;
		}
	}

	public String getType(DateTimeType type) {
		switch(type) {
			case DATE:
				return "date";
			case DATETIME:
				return "datetime";
			case TIME:
				return "time";
			case TIMESTAMP:
				return "timestamp";
			default:
				return null;
		}
	}

	public String getType(DecimalType type) {
		return "numeric";
	}

	public String getType(ExactNumericType type) {
		switch(type) {
			case BIGINT:
				return "bigint";
			case INTEGER:
				return "int";
			case MEDIUMINT:
				return "mediumint";
			case SMALLINT:
				return "smallint";
			case TINYINT:
				return "tinyint";
			default:
				return null;
		}
	}

	public String getType(MonetaryType type) {
		switch(type) {
			case MONEY:
				return "money";
			case SMALLMONEY:
				return "smallmoney";
			default:
				return null;
		}
	}

	public String getType(StringType type) {
		switch(type) {
			case CHAR:
				return "char";
			case LONGTEXT:
				return "longtext";
			case MEDIUMTEXT:
				return "mediumtext";
			case NCHAR:
				return "nchar";
			case NTEXT:
				return "ntext";
			case NVARCHAR:
				return "nvarchar";
			case TEXT:
				return "text";
			case TINYTEXT:
				return "tinytext";
			case VARCHAR:
				return "varchar";
			default:
				return null;
		}
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

		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new ProducerException(e.getMessage(), e.getCause());
		}
	}
}
