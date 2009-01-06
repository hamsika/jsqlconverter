package com.googlecode.jsqlconverter.producer.interfaces;

import com.googlecode.jsqlconverter.definition.insert.InsertFromValues;
import com.googlecode.jsqlconverter.producer.ProducerException;

public interface InsertFromValuesInterface {
	void doInsertFromValues(InsertFromValues insert) throws ProducerException;
}
