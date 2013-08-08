package com.googlecode.jsqlconverter.producer.interfaces;

import com.googlecode.jsqlconverter.definition.create.table.CreateTable;
import com.googlecode.jsqlconverter.producer.ProducerException;

public interface CreateTableInterface {
	void doCreateTable(CreateTable table) throws ProducerException;
}
