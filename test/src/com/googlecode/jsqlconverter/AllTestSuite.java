package com.googlecode.jsqlconverter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.googlecode.jsqlconverter.parser.TestDelimitedParser;
import com.googlecode.jsqlconverter.parser.TestParserTypeMappingHandled;
import com.googlecode.jsqlconverter.producer.TestAccessMDBProducer;
import com.googlecode.jsqlconverter.producer.TestPostgreSQLProducer;
import com.googlecode.jsqlconverter.producer.TestProducerTypeMappingHandled;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// parser
	TestParserTypeMappingHandled.class,
	TestDelimitedParser.class,

	// producer
	TestProducerTypeMappingHandled.class,
	TestAccessMDBProducer.class,
	TestPostgreSQLProducer.class
})

public class AllTestSuite {

}