package com.googlecode.jsqlconverter.frontend.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
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

		HashMap<String, Object> parserArgs = cli.getArgsWithPrefix(FROM + "-");
		HashMap<String, Object> producerArgs = cli.getArgsWithPrefix(TO + "-");

		Service parserService = su.getFirstService(parserName, true);
		Service producerService = su.getFirstService(producerName, false);

		Object[] matchedParserArgs = parserService.getMatchingConstructorArguments(parserArgs);
		Object[] matchedProducerArgs = producerService.getMatchingConstructorArguments(producerArgs);

		parser = (Parser) parserService.newInstance(matchedParserArgs);
		producer = (Producer) producerService.newInstance(matchedProducerArgs);
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

		Class<?> inputClass = p.getClassType();
		String inputName = "";

		if (InputStream.class.isAssignableFrom(inputClass)) {
			inputName = "file";
		} else if (File.class.isAssignableFrom(inputClass)) {
			inputName = "file";
		} else if (Integer.class.isAssignableFrom(inputClass)) {
			inputName = "number";
		} else if (Boolean.class.isAssignableFrom(inputClass)) {
			inputName = "true/false";
		} else if (Character.class.isAssignableFrom(inputClass)) {
			inputName = "char";
		} else if (String.class.isAssignableFrom(inputClass)) {
			inputName = "text";
		} else if (Enum.class.isAssignableFrom(inputClass)) {
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
