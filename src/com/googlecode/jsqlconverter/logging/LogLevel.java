package com.googlecode.jsqlconverter.logging;

import java.util.logging.Level;

public class LogLevel extends Level {
	public LogLevel(String name, int value) {
		super(name, value);
	}

	public static final Level ERROR = new LogLevel("ERROR", Level.WARNING.intValue() + 1);
	public static final Level UNHANDLED = new LogLevel("UNHANDLED", Level.SEVERE.intValue() + 1);
}
