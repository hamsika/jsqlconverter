package com.googlecode.jsqlconverter.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class EntryPoint {
	private ArrayList<Parameter> parameterList = new ArrayList<Parameter>();

	public EntryPoint(Constructor<?> c) {
		Class<?>[] parameterTypes = c.getParameterTypes();
		Annotation[][] paramAnnotations = c.getParameterAnnotations();

		for (int i=0; i<parameterTypes.length; i++) {
			parameterList.add(new Parameter(parameterTypes[i], paramAnnotations[i]));
		}
	}

	public Parameter[] getParameters() {
		return parameterList.toArray(new Parameter[parameterList.size()]);
	}
}
