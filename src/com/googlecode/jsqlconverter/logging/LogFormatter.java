package com.googlecode.jsqlconverter.logging;

import java.text.SimpleDateFormat;
import java.util.logging.*;
import java.util.Date;

public class LogFormatter extends java.util.logging.Formatter  {
	private static final int LEVEL_PADDING = 9;
	private final SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");

	public String format(LogRecord rec) {
		StringBuffer logMessage = new StringBuffer();

		logMessage.append(sf.format(new Date(rec.getMillis())));
		logMessage.append(" [");
		logMessage.append(padRight(rec.getLevel().getName(), LEVEL_PADDING));
		logMessage.append("] ");
		logMessage.append(rec.getSourceClassName().substring(rec.getSourceClassName().lastIndexOf('.') + 1));
		logMessage.append(" : ");

		/*logMessage.append(rec.getSourceClassName());
		logMessage.append(".");
		logMessage.append(rec.getSourceMethodName());
		logMessage.append(": ");*/

		logMessage.append(rec.getMessage());
		logMessage.append("\r\n");

		// create exception text for logfile
		if (rec.getThrown() != null) {
			StackTraceElement[] stackElement = rec.getThrown().getStackTrace();

			logMessage.append(rec.getThrown());
			logMessage.append("\r\n");

			for (StackTraceElement aStackElement : stackElement) {
				logMessage.append("\t");
				logMessage.append(aStackElement.toString());
				logMessage.append("\r\n");
			}
		}

		return logMessage.toString();
	}

	private String padRight(String text, int length) {
		StringBuffer paddedString = new StringBuffer(text);

		for (int i=text.length(); i<length; i++) {
			paddedString.append(" ");
		}

		return paddedString.toString();
	}
}
