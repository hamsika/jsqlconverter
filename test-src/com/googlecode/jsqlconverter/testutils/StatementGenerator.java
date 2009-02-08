package com.googlecode.jsqlconverter.testutils;

import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.create.table.constraint.KeyConstraint;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.producer.Producer;
import com.googlecode.jsqlconverter.producer.ProducerException;
import com.googlecode.jsqlconverter.producer.MySQLProducer;

import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

public class StatementGenerator {
	private Random gen = new Random();
	private enum NameType {
		DATABASE,
		SCHEMA,
		OBJECT
	}

	/**
	 * Generates a number of CreateTable objects and randomly sets up valid foreign key constraints between them
	 * 
	 * The returned order will also be valid for execution top to bottom in a database.
	 *
	 * @param   num number of statements to generate
	 * @return      an array of CreateTable objects
	 */
	public CreateTable[] generateCreateTableStatements(int num) {
		ArrayList<CreateTable> statements = new ArrayList<CreateTable>();

		// TODO: make one of the tables offlimits to adding fkeys

		for (int i=0; i<num; i++) {
			statements.add(generateCreateTableStatement());
		}

		if (statements.size() > 1) {
			for (int i=0; i<statements.size()*2; i++) {
				CreateTable tableA = statements.get(gen.nextInt(statements.size()));
				Column columnA = getRandomColumn(tableA);

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

	/**
	 * Returns a CreateTable object with randomly generated names, columns, keys, etc
	 *
	 * @return the generated CreateTable object
	 */
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

	/**
	 * Creates a valid index for the table specified with columns and options randomly selected
	 *
	 * @param   table   the table to create indexes for
	 * @return          the index that was created for the table
	 */
	public CreateIndex generateCreateIndexStatement(CreateTable table) {
		CreateIndex index = new CreateIndex(generateName(NameType.OBJECT), table.getName());

		index.setUnique(gen.nextBoolean());

		for (int i=0; i<generateInt(1, 5); i++) {
			index.addColumn(getRandomColumn(table).getName());
		}

		if (gen.nextBoolean()) {
			if (gen.nextBoolean()) {
				index.setType(CreateIndex.IndexType.HASHED);
			} else {
				index.setType(CreateIndex.IndexType.CLUSTERED);
			}
		}

		if (gen.nextBoolean()) {
			if (gen.nextBoolean()) {
				index.setSortSequence(CreateIndex.SortSequence.ASC);
			} else {
				index.setSortSequence(CreateIndex.SortSequence.DESC);
			}
		}

		return index;
	}

	/**
	 * Generates a valid insert statement. With a random number of columns.
	 * If any of the columns has the NOT NULL constraint then they will always be included
	 * 
	 * Note this does not handle foreign key constraints.
	 *
	 * @param   table   the table to generate the insert statement for
	 * @return          the insert object
	 */
	public InsertFromValues generateInsertFromValues(CreateTable table) {
		InsertFromValues insert = new InsertFromValues(table.getName(), table.getColumns());

		// find table columns that are NOT NULL and make sure they have values
		// make sure values are generated within getSize() limit

		// TODO: do something if there are references. Maybe accept create table array

		return insert;
	}

	/**
	 * Generates a full database schema
	 *
	 * @param numTables     number of tables to create. must be > 0
	 * @param numIndexes    number of indexes to create
	 * @param minRows       minimum number of inserts to create
	 * @param maxRows       maximum number of inserts to create
	 * @return              CreateTable, CreateIndex and InsertFromValue statements
	 */
	public Statement[] generateSchema(int numTables, int numIndexes, int minRows, int maxRows) {
		return null;
	}

	private void generateOptions(CreateTable ct) {
		// TODO: don't set sizes for columns that don't allow it
		// TODO: set sizes for columns that require it
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
			// TODO: don't set for columns that don't allow it
			getRandomColumn(ct).addColumnOption(ColumnOption.PRIMARY_KEY);
		}

		if (gen.nextBoolean()) {
			getRandomColumn(ct).addColumnOption(ColumnOption.NOT_NULL);
		}

		if (gen.nextBoolean()) {
			getRandomColumn(ct).addColumnOption(ColumnOption.UNIQUE);
		}

		if (gen.nextBoolean()) {
			// TODO: don't set for columns that don't allow it
			getRandomColumn(ct).addColumnOption(ColumnOption.AUTO_INCREMENT);
		}

		// compound pkeuy

		// unique compound key
		if (gen.nextBoolean()) {
			ArrayList<Column> columns = new ArrayList<Column>();
			ArrayList<Name> names = new ArrayList<Name>();

			for (int i=0; i<gen.nextInt(5); i++) {
				Column column;

				do {
					column = getRandomColumn(ct);
				} while (columns.contains(column));

				columns.add(column);
				names.add(column.getName());
			}

			if (names.size() != 0) {
				ct.addUniqueCompoundKeyConstraint(new KeyConstraint(names.toArray(new Name[names.size()])));
			}
		}
	}

	private Name generateName(NameType nameType) {
		switch(nameType) {
			case DATABASE:
				return new Name(generateString(1, 12), generateString(1, 12), generateString(1, 12));
			case SCHEMA:
				return new Name(generateString(1, 12), generateString(1, 12));
		}

		return new Name(generateString(1, 12));
	}

	private Column getRandomColumn(CreateTable table) {
		return table.getColumn(gen.nextInt(table.getColumnCount()));
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

	private int generateInt() {
		int seed = gen.nextInt(100);
		int num;

		if (1 == 1) {
			return gen.nextInt(30);
		}

		if (seed > 30) {
			// small
			num = gen.nextInt(30);
		} else if (seed > 10) {
			// possibly larger
			num = gen.nextInt(255);
		} else {
			// largest, least common
			num = gen.nextInt(4000);
		}

		return num;
	}

	private int generateInt(int min, int max) {
		int num;

		do {
			num = gen.nextInt(max);
		} while (num > max || num < min);

		return num;
	}

	public static void main(String args[]) throws ProducerException {
		//Producer producer = new PostgreSQLProducer(System.out);
		Producer producer = new MySQLProducer(System.out);

		StatementGenerator sg = new StatementGenerator();

		CreateTable[] statements = sg.generateCreateTableStatements(3);

		for (Statement statement : statements) {
			producer.produce(statement);
		}

		/*for (int i=0; i<30; i++) {
			producer.produce(sg.generateCreateIndexStatement());
		}*/

		/*for (int i=0; i<10; i++) {
			producer.produce(sg.generateInsertFromValues(sg.generateCreateTableStatement()));
		}*/
	}
}
