package com.googlecode.jsqlconverter.producer;

import com.googlecode.jsqlconverter.definition.type.*;
import com.googlecode.jsqlconverter.definition.Name;

public interface SQLProducer extends Producer {
	// inherited:
	// public void produce(Statement[] statements);

	public String getType(StringType type);
	public String getType(ApproximateNumericType type);
	public String getType(BinaryType type);
	public String getType(BooleanType type);
	public String getType(DateTimeType type);
	public String getType(ExactNumericType type);
	public String getType(MonetaryType type);

	public String getValidName(Name name);
}
