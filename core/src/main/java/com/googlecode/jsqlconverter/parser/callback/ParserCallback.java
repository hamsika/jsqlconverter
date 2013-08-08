package com.googlecode.jsqlconverter.parser.callback;

import com.googlecode.jsqlconverter.definition.Statement;

public interface ParserCallback {
	void produceStatement(Statement statement);

	void log(String message);
}
