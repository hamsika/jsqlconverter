package com.googlecode.jsqlconverter.testutils;

import com.googlecode.jsqlconverter.definition.type.*;

import java.util.ArrayList;
import java.util.Arrays;

public final class CommonTasks {
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

		// TODO: decimal type

		return types.toArray(new Type[types.size()]);
	}
}
