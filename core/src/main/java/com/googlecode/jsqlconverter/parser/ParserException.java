package com.googlecode.jsqlconverter.parser;

import java.io.IOException;

public class ParserException extends IOException {
	public ParserException(String message) {
		super(message);
	}

	public ParserException(String message, Throwable cause) {
		super(message, cause);
	}
}
