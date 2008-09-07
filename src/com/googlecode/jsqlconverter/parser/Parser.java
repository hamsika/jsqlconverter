package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.logging.MyLogger;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;

public abstract class Parser {
	protected static MyLogger log = MyLogger.getLogger(Parser.class.getName());

	public abstract void parse(ParserCallback callback) throws ParserException;
}
