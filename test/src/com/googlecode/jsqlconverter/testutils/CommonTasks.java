package com.googlecode.jsqlconverter.testutils;

import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.parser.GeneratorParser;

import java.util.ArrayList;
import java.util.Arrays;

public final class CommonTasks {
	private static final GeneratorParser gp = new GeneratorParser(50);
	private static final CreateTable[] createTables = gp.generateCreateTableStatements();

	private CommonTasks() {}

	public static Type[] getTypes() {
		ArrayList<Type> types = new ArrayList<Type>();

		types.addAll(Arrays.asList(ApproximateNumericType.values()));
		types.addAll(Arrays.asList(BinaryType.values()));
		types.addAll(Arrays.asList(BooleanType.values()));
		types.addAll(Arrays.asList(DateTimeType.values()));
		types.addAll(Arrays.asList(ExactNumericType.values()));
		types.addAll(Arrays.asList(MonetaryType.values()));
		types.addAll(Arrays.asList(StringType.values()));

		// Note decimal type is not returned.

		return types.toArray(new Type[types.size()]);
	}

	public static CreateTable[] getCreateTables() {
		return createTables;
	}
}
