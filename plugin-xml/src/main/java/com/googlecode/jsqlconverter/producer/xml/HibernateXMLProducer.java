package com.googlecode.jsqlconverter.producer.xml;

import java.io.FileWriter;
import java.io.IOException;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ColumnForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.type.ApproximateNumericType;
import com.googlecode.jsqlconverter.definition.type.DateTimeType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.producer.Producer;
import com.googlecode.jsqlconverter.producer.ProducerException;
import com.googlecode.jsqlconverter.producer.interfaces.CreateTableInterface;

@ServiceName("Hibernate")
public class HibernateXMLProducer extends Producer implements CreateTableInterface {
	private String packageName;

	private String outputFolder;
	private String capitaliseAfter = "_";

	public HibernateXMLProducer(@ParameterName("Folder") String outputFolder, @ParameterName("Package Name") String packageName) {
		this.outputFolder = outputFolder;
		this.packageName = packageName;
	}

	@Override
	public void doCreateTable(CreateTable table) throws ProducerException {
		FileWriter outXml;
		FileWriter outJava;

		try {
			outXml = new FileWriter(outputFolder + "/" + getFileName(table, true));

			outXml.append("<?xml version=\"1.0\"?>\n");
			outXml.append("<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n");
			outXml.append("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");

			outXml.append("<hibernate-mapping>\n");
			outXml.append("\t<class name=\"" + packageName + "." + getClassName(table, true) + "\" table=\"" + table.getName().getObjectName() + "\">\n");

			for (Column column : table.getColumns()) {
				if (column.containsOption(ColumnOption.PRIMARY_KEY)) {
					outXml.append("\t\t<id name=\"" + getClassName(column, false) + "\" column=\"" + column.getName().getObjectName() + "\"/>\n");
				} else if (column.getForeignKeyConstraint() == null) {
					outXml.append("\t\t<property name=\"" + getClassName(column, false) + "\" column=\"" + column.getName().getObjectName() + "\"/>\n");
				} else {
					ColumnForeignKeyConstraint fk = column.getForeignKeyConstraint();
	
					outXml.append("\t\t<set name=\"competencies\" table=\"user_competency\">\n");
					outXml.append("\t\t\t<key column=\"" + fk.getColumnName().getObjectName() + "\" />\n");
					outXml.append("\t\t\t<one-to-many column=\"" + column.getName().getObjectName() + "\" class=\"" + packageName + "." + getClassName(fk.getTableName().getObjectName(), true) + "\"/>\n");
					outXml.append("\t\t</set>\n");
				}
			}

			outXml.append("\t</class>\n");
			outXml.append("</hibernate-mapping>");

			outXml.close();
		} catch (IOException e) {
			throw new ProducerException(e.getMessage(), e.getCause());
		}

		try {
			outJava = new FileWriter(outputFolder + "/" + getFileName(table, false));

			outJava.append("package " + packageName + ";\n\n");

			outJava.append("import java.util.HashSet;\n");
			outJava.append("import java.util.Set;\n");
			outJava.append("import java.sql.Date;\n\n");

			outJava.append("public class " + getClassName(table, true) + " {");

			// output variables
			for (Column column : table.getColumns()) {
				if (column.getForeignKeyConstraint() == null) {
					outJava.append("\n\tprivate " + getType(column) + " " + getClassName(column, false) + ";");
				} else {
					outJava.append("\n\tprivate Set " + getClassName(column, false) + " = new HashSet();");
				}
			}

			// output getters
			for (Column column : table.getColumns()) {
				outJava.append("\n\n\tpublic " + getType(column) + " get" + getClassName(column, true) + "() {\n");
				outJava.append("\t\treturn " + getClassName(column, false) + ";\n");
				outJava.append("\t}");
			}

			// output setters
			for (Column column : table.getColumns()) {
				outJava.append("\n\n\tpublic void set" + getClassName(column, true) + "(" + getType(column) + " " + getClassName(column, false) + ") {\n");
				outJava.append("\t\tthis." + getClassName(column, false) + " = " + getClassName(column, false) + ";\n");
				outJava.append("\t}");
			}

			outJava.append("\n}\n");

			outJava.close();
		} catch (IOException e) {
			throw new ProducerException(e.getMessage(), e.getCause());
		}
	}

	private String getType(Column column) {
		if (column.getForeignKeyConstraint() == null) {
			if (column.getType() instanceof ExactNumericType) {
				return "int";
			} else if (column.getType() instanceof ApproximateNumericType) {
				return "float";
			} else if (column.getType() instanceof DateTimeType) {
				return "Date";
			} else {
				return "String";
			}
		} else {
			return "Set";
		}
	}

	private String getFileName(CreateTable table, boolean isXML) {
		String name = getClassName(table.getName().getObjectName(), true);

		if (isXML) {
			name += ".hbm.xml";
		} else {
			name += ".java";
		}

		return name;
	}

	private String getClassName(CreateTable table, boolean capFirst) {
		return getClassName(table.getName().getObjectName(), capFirst);
	}

	private String getClassName(Column column, boolean capFirst) {
		return getClassName(column.getName().getObjectName(), capFirst);
	}

	private String getClassName(String name, boolean capFirst) {
		String returnName = "";

		String[] names = name.toLowerCase().split(capitaliseAfter);

		for (String n : names) {
			returnName += Character.toUpperCase(n.charAt(0)) + n.substring(1);;
		}

		if (!capFirst) {
			returnName = Character.toLowerCase(returnName.charAt(0)) + returnName.substring(1);
		}

		return returnName;
	}
}
