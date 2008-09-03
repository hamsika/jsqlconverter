package com.googlecode.jsqlconverter.logging;

import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyLogger extends Logger {
	protected MyLogger(String name, String resourceBundleName) {
		super(name, resourceBundleName);
	}

	public static synchronized MyLogger getLogger(String name) {
		LogManager manager = LogManager.getLogManager();
		MyLogger result = (MyLogger)manager.getLogger(name);

		if (result == null) {
			result = new MyLogger(name, null);
			manager.addLogger(result);
		}

		return result;
	}

	public void logApp(Level level, String msg) {
		log(new LogRecord(level, msg));
	}
}
