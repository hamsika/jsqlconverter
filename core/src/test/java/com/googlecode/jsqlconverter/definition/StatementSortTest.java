package com.googlecode.jsqlconverter.definition;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

import com.googlecode.jsqlconverter.definition.create.table.Column;
import com.googlecode.jsqlconverter.definition.create.table.ColumnOption;
import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.create.table.constraint.ColumnForeignKeyConstraint;
import com.googlecode.jsqlconverter.definition.type.ExactNumericType;

public class StatementSortTest {
	@Test
	public void testCreateTableSort() {
		ArrayList<CreateTable> statements1 = new ArrayList<CreateTable>();
		ArrayList<CreateTable> statements2 = new ArrayList<CreateTable>();
		ArrayList<CreateTable> statements3 = new ArrayList<CreateTable>();

		CreateTable c = new CreateTable(new Name("c"));
		CreateTable b = new CreateTable(new Name("b"));
		CreateTable a = new CreateTable(new Name("a"));

		c.addColumn(new Column(new Name("c"), ExactNumericType.INTEGER));
		b.addColumn(new Column(new Name("b"), ExactNumericType.INTEGER));
		a.addColumn(new Column(new Name("a"), ExactNumericType.INTEGER));

		c.getColumn(0).addColumnOption(ColumnOption.PRIMARY_KEY);
		b.getColumn(0).addColumnOption(ColumnOption.PRIMARY_KEY);

		b.getColumn(0).setForeignKeyConstraint(new ColumnForeignKeyConstraint(new Name("c"), new Name("c")));
		a.getColumn(0).setForeignKeyConstraint(new ColumnForeignKeyConstraint(new Name("b"), new Name("b")));

		statements1.add(a);
		statements1.add(b);
		statements1.add(c);

		statements2.add(c);
		statements2.add(a);
		statements2.add(b);

		statements3.add(c);
		statements3.add(b);
		statements3.add(a);

		Collections.sort(statements1);
		Collections.sort(statements2);
		Collections.sort(statements3);

		validateSorting(statements1);
		validateSorting(statements2);
		validateSorting(statements3);
	}

	private void validateSorting(ArrayList<CreateTable> statements) {
		assertEquals("c", statements.get(0).getName().getObjectName());
		assertEquals("b", statements.get(1).getName().getObjectName());
		assertEquals("a", statements.get(2).getName().getObjectName());
	}
}
