package com.googlecode.jsqlconverter.producer.interfaces;

import com.googlecode.jsqlconverter.definition.truncate.table.Truncate;
import com.googlecode.jsqlconverter.producer.ProducerException;

public interface TruncateInterface {
	void doTruncate(Truncate truncate) throws ProducerException;
}
