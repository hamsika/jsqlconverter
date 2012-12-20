package com.googlecode.jsqlconverter.testutils;

import java.util.Properties;

public class TestProperties extends Properties {
	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(getProperty(key, "false"));
	}

	public String[] getProperties(String string) {
		String val = getProperty("active");

		if (val == null || val.isEmpty()) {
			return new String[] {};
		}

		return val.split(",");
	}
}
