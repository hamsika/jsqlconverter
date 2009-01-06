package com.googlecode.jsqlconverter.producer.interfaces;

import com.googlecode.jsqlconverter.definition.create.index.CreateIndex;
import com.googlecode.jsqlconverter.producer.ProducerException;

public interface CreateIndexInterface {
	void doCreateIndex(CreateIndex index) throws ProducerException;
}
