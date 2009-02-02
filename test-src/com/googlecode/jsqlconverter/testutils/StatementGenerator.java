package com.googlecode.jsqlconverter.testutils;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;
import com.googlecode.jsqlconverter.producer.Producer;
import com.googlecode.jsqlconverter.producer.ProducerException;
import com.googlecode.jsqlconverter.producer.PostgreSQLProducer;

import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

public class StatementGenerator {
	private Random gen = new Random();

	public CreateTable[] generateCreateTableStatements(int num) {
		ArrayList<CreateTable> statements = new ArrayList<CreateTable>();

		for (int i=0; i<num; i++) {
			statements.add(generateCreateTableStatement());
		}

		if (statements.size() > 1) {
			for (int i=0; i<statements.size()*2; i++) {
				CreateTable tableA = statements.get(gen.nextInt(statements.size()));
				Column columnA = tableA.getColumn(gen.nextInt(tableA.getColumnCount()));

				CreateTable tableB;
				Column columnB = null;

				// make sure tableB isn't the same as this table
				do {
					tableB = statements.get(gen.nextInt(statements.size()));
				} while (tableA == tableB);

				// make sure columnB is the same type as column a
				for (Column column : tableB.getColumns()) {
					if (column.getType().equals(columnA.getType())) {
						columnB = column;
						break;
					}
				}

				if (columnB != null) {
					columnA.setForeignKeyConstraint(new ForeignKeyConstraint(tableB.getName(), columnB.getName()));
				}
			}
		}

		Collections.sort(statements);

		return statements.toArray(new CreateTable[statements.size()]);
	}

	public CreateTable generateCreateTableStatement() {
		CreateTable ct = new CreateTable(generateName(NameType.SCHEMA));

		for (Type type : ApproximateNumericType.values()) {
			Column a = new Column(generateName(NameType.OBJECT), type);

			ct.addColumn(a);
		}

		for (Type type : BinaryType.values()) {
			Column a = new Column(generateName(NameType.OBJECT), type);

			ct.addColumn(a);
		}

		for (Type type : BooleanType.values()) {
			Column a = new Column(generateName(NameType.OBJECT), type);

			ct.addColumn(a);
		}

		for (Type type : DateTimeType.values()) {
			Column a = new Column(generateName(NameType.OBJECT), type);

			ct.addColumn(a);
		}

		for (Type type : ExactNumericType.values()) {
			Column a = new Column(generateName(NameType.OBJECT), type);

			ct.addColumn(a);
		}

		for (Type type : MonetaryType.values()) {
			Column a = new Column(generateName(NameType.OBJECT), type);

			ct.addColumn(a);
		}

		for (Type type : StringType.values()) {
			Column a = new Column(generateName(NameType.OBJECT), type);

			ct.addColumn(a);
		}

		Column a = new Column(generateName(NameType.OBJECT), new DecimalType(generateInt(), generateInt()));
		Column b = new Column(generateName(NameType.OBJECT), new DecimalType(generateInt()));

		ct.addColumn(a);
		ct.addColumn(b);

		generateOptions(ct);

		return ct;
	}

	private void generateOptions(CreateTable ct) {
		// column sizes
		for (Column column : ct.getColumns()) {
			column.setSize(generateInt());
		}

		// table options

		if (gen.nextBoolean()) {
			ct.addOption(TableOption.TEMPORARY);
		} else if (gen.nextBoolean()) {
			ct.addOption(TableOption.LOCAL);
		} else if (gen.nextBoolean()) {
			ct.addOption(TableOption.GLOBAL);
		}

		// column options
		if (gen.nextBoolean()) {
			ct.getColumn(gen.nextInt(ct.getColumnCount())).addColumnOption(ColumnOption.PRIMARY_KEY);
		}

		if (gen.nextBoolean()) {
			ct.getColumn(gen.nextInt(ct.getColumnCount())).addColumnOption(ColumnOption.NOT_NULL);
		}

		if (gen.nextBoolean()) {
			ct.getColumn(gen.nextInt(ct.getColumnCount())).addColumnOption(ColumnOption.UNIQUE);
		}

		if (gen.nextBoolean()) {
			ct.getColumn(gen.nextInt(ct.getColumnCount())).addColumnOption(ColumnOption.AUTO_INCREMENT);
		}

		// compound pkeuy

		// unique compound key
		if (gen.nextBoolean()) {
			ArrayList<Column> columns = new ArrayList<Column>();
			ArrayList<Name> names = new ArrayList<Name>();

			for (int i=0; i<gen.nextInt(5); i++) {
				Column column;

				do {
					column = ct.getColumn(gen.nextInt(ct.getColumnCount()));
				} while (columns.contains(column));

				columns.add(column);
				names.add(column.getName());
			}

			if (names.size() != 0) {
				ct.addUniqueCompoundKeyConstraint(new KeyConstraint(names.toArray(new Name[names.size()])));
			}
		}
	}

	public Name generateName(NameType nameType) {
		switch(nameType) {
			case DATABASE:
				return new Name(generateString(1, 12), generateString(1, 12), generateString(1, 12));
			case SCHEMA:
				return new Name(generateString(1, 12), generateString(1, 12));
		}

		return new Name(generateString(1, 12));
	}

	private String generateString(int min, int max) {
		String charset = "$abcdefFGHIJKLMghijkluvwxyz0123456_789ABCDENOmnopqrstPQRSTUVWXYZ";

		int chars;

		do {
			chars = gen.nextInt(max);
		} while (chars < min);

		String name = "";

		for (int i=0; i<chars; i++) {
			name += charset.charAt(gen.nextInt(charset.length()));
		}

		return name;
	}

	public int generateInt() {
		int seed = gen.nextInt(100);
		int num;

		if (seed > 30) {
			// small
			num = gen.nextInt(30);
		} else if (seed > 10) {
			// possibly larger
			num = gen.nextInt(255);
		} else {
			num = gen.nextInt(4000);
		}

		return num;

	}

	private enum NameType {
		DATABASE,
		SCHEMA,
		OBJECT
	}
}
