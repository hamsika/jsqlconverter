package com.googlecode.jsqlconverter.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.definition.Name;
import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.TableOption;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ColumnForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.definition.type.ApproximateNumericType;
import com.googlecode.jsqlconverter.definition.type.BinaryType;
import com.googlecode.jsqlconverter.definition.type.BooleanType;
import com.googlecode.jsqlconverter.definition.type.DateTimeType;
import com.googlecode.jsqlconverter.definition.type.DecimalType;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;
import com.googlecode.jsqlconverter.definition.type.MonetaryType;
import com.googlecode.jsqlconverter.definition.type.NumericType;
import com.googlecode.jsqlconverter.definition.type.StringType;
import com.googlecode.jsqlconverter.definition.type.Type;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;

@ServiceName("Generator")
public class GeneratorParser extends Parser {
	private Random gen = new Random();
	private int numCreateTable;

	private enum NameType {
		DATABASE,
		SCHEMA,
		OBJECT
	}

	public GeneratorParser(@ParameterName("Num Tables") Integer numCreateTable) {
		this.numCreateTable = numCreateTable;
	}

	@Override
	public void parse(ParserCallback callback) throws ParserException {
		for (CreateTable table : generateCreateTableStatements()) {
			callback.produceStatement(table);
		}
	}

	/**
	 * Generates a number of CreateTable objects and randomly sets up valid foreign key constraints between them
	 *
	 * The returned order will also be valid for execution top to bottom in a database.
	 *
	 * @param   num number of statements to generate
	 * @return      an array of CreateTable objects
	 */
	public CreateTable[] generateCreateTableStatements() {
		ArrayList<CreateTable> statements = new ArrayList<CreateTable>();

		for (int i=0; i<numCreateTable; i++) {
			CreateTable table = generateCreateTableStatement(statements.toArray(new CreateTable[statements.size()]));

			statements.add(table);
		}

		Collections.sort(statements);

		return statements.toArray(new CreateTable[statements.size()]);
	}

	private CreateTable generateCreateTableStatement(CreateTable[] previousTables) {
		// TODO: support generating table name with a SCHEMA. (schema must exist..)
		Name tableName;

		if (previousTables != null) {
			tableName = generateUniqueName(NameType.OBJECT, previousTables);
		} else {
			tableName = generateName(NameType.OBJECT);
		}

		CreateTable ct = new CreateTable(tableName);

		for (Type type : ApproximateNumericType.values()) {
			Column a = new Column(generateUniqueName(NameType.OBJECT, ct.getColumns()), type);

			// TODO: support setSize() here

			ct.addColumn(a);
		}

		for (Type type : BinaryType.values()) {
			Column a = new Column(generateUniqueName(NameType.OBJECT, ct.getColumns()), type);

			a.setSize(generateInt(1, 30));

			ct.addColumn(a);
		}

		for (Type type : BooleanType.values()) {
			Column a = new Column(generateUniqueName(NameType.OBJECT, ct.getColumns()), type);

			ct.addColumn(a);
		}

		for (Type type : DateTimeType.values()) {
			Column a = new Column(generateUniqueName(NameType.OBJECT, ct.getColumns()), type);

			ct.addColumn(a);
		}

		for (Type type : ExactNumericType.values()) {
			Column a = new Column(generateUniqueName(NameType.OBJECT, ct.getColumns()), type);

			ct.addColumn(a);
		}

		for (Type type : MonetaryType.values()) {
			Column a = new Column(generateUniqueName(NameType.OBJECT, ct.getColumns()), type);

			ct.addColumn(a);
		}

		for (Type type : StringType.values()) {
			Column a = new Column(generateUniqueName(NameType.OBJECT, ct.getColumns()), type);

			a.setSize(generateInt(1, 255));

			ct.addColumn(a);
		}

		Column a = new Column(generateUniqueName(NameType.OBJECT, ct.getColumns()), new DecimalType(generateInt(), generateInt()));
		Column b = new Column(generateUniqueName(NameType.OBJECT, ct.getColumns()), new DecimalType(generateInt()));

		ct.addColumn(a);
		ct.addColumn(b);

		generateTableOptions(ct);

		generateTableReferences(ct, previousTables);

		return ct;
	}

	private void generateTableOptions(CreateTable ct) {
		// table options
		if (gen.nextBoolean()) {
			ct.addOption(TableOption.TEMPORARY);
		}

		if (gen.nextBoolean()) {
			getRandomColumn(ct).addColumnOption(ColumnOption.NOT_NULL);
		}


		boolean createPKey = false;
		boolean pkeyCreated = false;

		if (gen.nextBoolean()) {
			createPKey = true;
		}

		for (Column column : ct.getColumns()) {
			if (column.getType() instanceof NumericType) {
				if (createPKey && !pkeyCreated && gen.nextBoolean()) {
					column.addColumnOption(ColumnOption.PRIMARY_KEY);

					if (gen.nextBoolean()) {
						column.addColumnOption(ColumnOption.AUTO_INCREMENT);
					}

					pkeyCreated = true;

					continue;
				}
			}

			if (column.getType() instanceof NumericType ||
				column.getType() instanceof DateTimeType ||
				column.getType() instanceof MonetaryType ||
				column.getType() instanceof StringType
			) {
				if (gen.nextBoolean()) {
					column.addColumnOption(ColumnOption.UNIQUE);
				}
			}
		}

		// TODO: compound primary key

		// TODO: unique compound key
		/*if (gen.nextBoolean()) {
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
				// TODO: don't set for columns that don't allow it
				//	ct.adudUniqueCompoundKeyConstraint(new KeyConstraint(names.toArray(new Name[names.size()])));
			}
		}*/
	}

	private void generateTableReferences(CreateTable ct, CreateTable[] previousTables) {
		if (previousTables.length == 0) {
			return;
		}

		// make sure this table isn't temporary
		if (ct.containsOption(TableOption.TEMPORARY)) {
			return;
		}

		CreateTable referencedTable = previousTables[gen.nextInt(previousTables.length)];

		// make sure the table to be referenced isn't temporary
		if (referencedTable.containsOption(TableOption.TEMPORARY)) {
			return;
		}

		for (int i=0; i<10; i++) {
			Column column = getRandomColumn(ct);
			Column refColumn = null;

			if (column.getForeignKeyConstraint() != null) {
				continue;
			}

			// get valid column from reference table
			for (Column refCol : referencedTable.getColumns()) {
				// ensure the types match and the ref column is indexed
				if (column.getType() == refCol.getType() && (refCol.containsOption(ColumnOption.PRIMARY_KEY) || refCol.containsOption(ColumnOption.UNIQUE))) {
					refColumn = refCol;
				}
			}

			if (refColumn == null) {
				return;
			}

			// create the reference
			column.setForeignKeyConstraint(new ColumnForeignKeyConstraint(referencedTable.getName(), refColumn.getName()));
		}
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

	private Name generateUniqueName(NameType nameType, Column[] columns) {
		boolean nameAlreadyExists;
		Name name;

		do {
			name = generateName(nameType);

			nameAlreadyExists = false;

			for (Column column : columns) {
				if (column.getName().getObjectName().equalsIgnoreCase(name.getObjectName())) {
					nameAlreadyExists = true;
					break;
				}
			}
		} while (nameAlreadyExists);

		return name;
	}

	private Name generateUniqueName(NameType nameType, CreateTable[] tables) {
		boolean nameAlreadyExists;
		Name name;

		do {
			name = generateName(nameType);

			nameAlreadyExists = false;

			for (CreateTable table : tables) {
				if (table.getName().getObjectName().equalsIgnoreCase(name.getObjectName())) {
					nameAlreadyExists = true;
					break;
				}
			}
		} while (nameAlreadyExists);

		return name;
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
		return gen.nextInt(30);
	}

	private int generateInt(int min, int max) {
		int num;

		do {
			num = gen.nextInt(max);
		} while (num > max || num < min);

		return num;
	}
}
