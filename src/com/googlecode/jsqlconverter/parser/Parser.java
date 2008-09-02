package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.logging.MyLogger;

public abstract class Parser {
	protected static MyLogger log = MyLogger.getLogger(Parser.class.getName());

	public abstract Statement[] parse() throws ParserException;
}
