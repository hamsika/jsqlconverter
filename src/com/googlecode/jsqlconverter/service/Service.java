package com.googlecode.jsqlconverter.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.googlecode.jsqlconverter.annotation.ServiceName;

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
		isParser = serviceClass.getSimpleName().contains("Parser"); // TODO: find a better way to do this

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
			classParams.add(obj.getClass());
		}

		return serviceClass.getConstructor(
			classParams.toArray(new Class[classParams.size()])
		).newInstance(parameters);
	}

	public EntryPoint[] getEntryPoints() {
		return entryPointList.toArray(new EntryPoint[entryPointList.size()]);
	}
}
