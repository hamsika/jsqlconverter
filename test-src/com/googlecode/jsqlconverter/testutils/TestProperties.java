package com.googlecode.jsqlconverter.testutils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class TestProperties {
	private static String propertiesFile = "test.properties";
	private static Properties properties = null;

	private TestProperties() { }

	private static void initialise() {
		// only initialise if it hasn't already been
		if (properties != null)
			return;

		// initialise our application properties with default values incase they are not set yet
		properties = new Properties();

		try {
			FileInputStream in = new FileInputStream(propertiesFile);
			properties.load(in);
			in.close();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			System.exit(0);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(0);
		}
	}

	public static String getProperty(String property) {
		initialise();

		return properties.getProperty(property);
	}

	public static boolean getBoolean(String property) {
		String prop = getProperty(property);

		return prop.equals("true");
	}

	public static char getChar(String property) {
		String prop = getProperty(property);

		return prop.charAt(0);
	}
}
