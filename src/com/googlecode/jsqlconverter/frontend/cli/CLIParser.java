package com.googlecode.jsqlconverter.frontend.cli;

import java.util.HashMap;

public class CLIParser {
	private String[] args;

	public CLIParser(String[] args) {
		this.args = args;
	}

	public boolean isEmpty() {
		return args.length == 0;
	}

	public String getString(String name) {
		for (int i=0; i<args.length; i++) {
			if (args[i].equals(name)) {
				return args[i+1];
			}
		}

		return null;
	}

	public HashMap<String, String> getArgsWithPrefix(String prefix) {
		HashMap<String, String> paramMap = new HashMap<String, String>();

		for (int i=0; i<args.length; i++) {
			if (args[i].startsWith(prefix)) {
				paramMap.put(
					args[i].substring(prefix.length()),
					args[i+1]
				);
			}
		}

		return paramMap;
	}
}
