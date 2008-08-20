package com.googlecode.jsqlconverter.parser;

import com.googlecode.jsqlconverter.definition.Statement;

public interface Parser {
	public Statement[] parse() throws ParserException;
}
