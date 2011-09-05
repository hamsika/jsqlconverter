package com.googlecode.jsqlconverter.frontend.cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.parser.Parser;
import com.googlecode.jsqlconverter.parser.ParserException;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;
import com.googlecode.jsqlconverter.producer.Producer;
import com.googlecode.jsqlconverter.producer.ProducerException;
import com.googlecode.jsqlconverter.producer.interfaces.FinalInterface;
import com.googlecode.jsqlconverter.service.EntryPoint;
import com.googlecode.jsqlconverter.service.Parameter;
import com.googlecode.jsqlconverter.service.Service;
import com.googlecode.jsqlconverter.service.ServiceUtil;

public class SQLConverterCLI implements ParserCallback {
	private static final String FROM = "--from";
	private static final String TO = "--to";
	private ServiceUtil su;
	private Parser parser;
	private Producer producer;

	public SQLConverterCLI(String[] args) throws IOException, ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		su = new ServiceUtil();
		CLIParser cli = new CLIParser(args);

		if (cli.isEmpty()) {
			printHelp(su);
			System.exit(0);
		}

		String parserName = cli.getString(FROM);
		String producerName = cli.getString(TO);

		HashMap<String, String> parserArgs = cli.getArgsWithPrefix(FROM + "-");
		HashMap<String, String> producerArgs = cli.getArgsWithPrefix(TO + "-");

		Service parserService = getService(parserName, true);
		Service producerService = getService(producerName, false);

		Object[] matchedParserArgs = getMatchingArgs(parserService, parserArgs);
		Object[] matchedProducerArgs = getMatchingArgs(producerService, producerArgs);

		parser = (Parser) parserService.newInstance(matchedParserArgs);
		producer = (Producer) producerService.newInstance(matchedProducerArgs);
	}

	private Service getService(String name, boolean isParser) {
		for (Service service : su.getService(name)) {
			if (service.isParser() == isParser) {
				return service;
			}
		}

		throw new RuntimeException("Service with name " + name + " not found");
	}
	
	private Object[] getMatchingArgs(Service service, HashMap<String, String> argMap) throws FileNotFoundException {
		EntryPoint[] eps = service.getEntryPoints();
		EntryPoint matchedEp = null;
		ArrayList<Object> finalArgs = new ArrayList<Object>();
		boolean matchAll;
		boolean returnPrintStream = false;

		for (EntryPoint ep : eps) {
			matchAll = true;
			returnPrintStream = false;

			for (Parameter p : ep.getParameters()) {
				if (!argMap.containsKey(p.getFlattenedName()) && !p.isOptional()) {
					if (p.getClassType().equals(PrintStream.class)) {
						returnPrintStream = true;
						continue;
					}

					matchAll = false;
					break;
				}
			}

			if (matchAll) {
				matchedEp = ep;
				break;
			}
		}

		if (returnPrintStream) {
			return new Object[] { System.out };
		}

		if (matchedEp == null) {
			throw new RuntimeException("No matching EntryPoint found");
		}

		// match constructor order and set defaults
		for (Parameter p : matchedEp.getParameters()) {
			if (argMap.containsKey(p.getFlattenedName())) {
				finalArgs.add(p.toObject(argMap.get(p.getFlattenedName())));
			} else if (p.isOptional()) {
				finalArgs.add(p.toObject(argMap.get(p.getDefaultValue())));
			} else {
				throw new RuntimeException("Argment matching, this should never happen");
			}
		}

		return finalArgs.toArray(new Object[finalArgs.size()]);
	}
	
	public void run() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ParserException, ProducerException {
		long beforeMili = System.currentTimeMillis();

		parser.parse(this);

		if (producer instanceof FinalInterface) {
			((FinalInterface)producer).doFinal();
		}

		long runTimeMili = System.currentTimeMillis() - beforeMili;

		log("Runtime: " + runTimeMili + "ms");
	}

	private void printHelp(ServiceUtil su) {
		System.out.println("Parsers");

		for (Service s : su.getServices()) {
			if (s.isParser()) {
				for (EntryPoint ep : s.getEntryPoints()) {
					System.out.print("\t" + FROM + " " + s.getFlattenedName());

					for (Parameter p : ep.getParameters()) {
						printParameter(p, true);
					}

					System.out.println();
				}
			}
		}

		System.out.println();
		System.out.println("Producers");

		for (Service s : su.getServices()) {
			if (!s.isParser()) {
				for (EntryPoint ep : s.getEntryPoints()) {
					System.out.print("\t" + TO + " " + s.getFlattenedName());

					for (Parameter p : ep.getParameters()) {
						// If the output is a PrintStream, hide it as we always use System.out
						if (p.getClassType() != PrintStream.class) {
							printParameter(p, false);
						}
					}

					System.out.println();
				}
			}
		}
	}

	private void printParameter(Parameter p, boolean isParser) {
		System.out.print(" ");

		if (p.isOptional()) System.out.print("[");

		String inputName = p.getClassType().getSimpleName();

		if (inputName.endsWith("InputStream")) {
			inputName = "file";
		} else if (inputName.equals("File")) {
			inputName = "file";
		} else if (inputName.equals("Integer")) {
			inputName = "number";
		} else if (inputName.equals("Boolean")) {
			inputName = "true/false";
		} else if (inputName.equals("Character")) {
			inputName = "char";
		} else if (inputName.equals("String")) {
			inputName = "text";
		} else if (p.getClassType().isEnum()) {
			Enum<?>[] enumList = (Enum<?>[]) p.getClassType().getEnumConstants();
			inputName = "";

			for (int i=0; i<enumList.length; i++) {
				if (i > 0) {
					inputName += " | ";
				}

				inputName += enumList[i];
			}
		}

		if (p.isOptional() && p.getDefaultValue() != null) {
			inputName += " = " + p.getDefaultValue();
		}
		
		System.out.print(((isParser) ? FROM : TO) + "-" + p.getFlattenedName() + " <" + inputName + ">");

		if (p.isOptional()) System.out.print("]");
	}

	@Override
	public void produceStatement(Statement statement) {
		try {
			producer.produce(statement);
		} catch (ProducerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void log(String message) {
		System.err.println(message);
	}

	public static void main(String args[]) throws IOException, ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		new SQLConverterCLI(args).run();
	}
}
