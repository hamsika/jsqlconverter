package com.googlecode.jsqlconverter.service;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import com.googlecode.jsqlconverter.annotation.ServiceName;
import com.googlecode.jsqlconverter.parser.Parser;

public class Service {
	private ArrayList<EntryPoint> entryPointList = new ArrayList<EntryPoint>();
	private Class<?> serviceClass;
	private String name;
	private String flattenedName;
	private boolean isParser;

	public Service(Class<?> serviceClass) {
		ServiceName a = serviceClass.getAnnotation(ServiceName.class);

		if (a == null) {
			throw new RuntimeException("ServiceName not defined for " + serviceClass.getName());
		}

		this.serviceClass = serviceClass;
		name = a.value();
		flattenedName = name.toLowerCase().replace(" ", "-");
		isParser = Parser.class.isAssignableFrom(serviceClass);

		Constructor<?>[] cs = serviceClass.getConstructors();

		for (Constructor<?> c : cs) {
			entryPointList.add(new EntryPoint(c));
		}
	}

	public String getName() {
		return name;
	}

	public String getFlattenedName() {
		return flattenedName;
	}

	public boolean isParser() {
		return isParser;
	}

	public Object newInstance(Object[] parameters) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		ArrayList<Class<?>> classParams = new ArrayList<Class<?>>();

		for (Object obj : parameters) {
			if (obj == null) {
				return null;
			}

			classParams.add(obj.getClass());
		}

		return serviceClass.getConstructor(
			classParams.toArray(new Class[classParams.size()])
		).newInstance(parameters);
	}

	public EntryPoint[] getEntryPoints() {
		return entryPointList.toArray(new EntryPoint[entryPointList.size()]);
	}

	public Object[] getMatchingConstructorArguments(HashMap<String, Object> argMap) throws FileNotFoundException {
		EntryPoint matchedEp = null;
		ArrayList<Object> finalArgs = new ArrayList<Object>();
		boolean matchAll;
		boolean returnPrintStream = false;

		for (EntryPoint ep : entryPointList) {
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
}
