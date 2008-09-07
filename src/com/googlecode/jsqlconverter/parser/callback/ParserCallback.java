package com.googlecode.jsqlconverter.parser.callback;

import com.googlecode.jsqlconverter.definition.Statement;

public interface ParserCallback {
	public void produceStatement(Statement statement);
}
