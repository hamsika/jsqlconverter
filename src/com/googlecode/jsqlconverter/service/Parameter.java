package com.googlecode.jsqlconverter.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;

import com.googlecode.jsqlconverter.annotation.ParameterName;
import com.googlecode.jsqlconverter.annotation.ParameterOptional;

public class Parameter {
	private Class<?> classType;
	private String name;
	private String flattenedName;
	private String defaultValue = null;
	private boolean isOptional = false;

	public Parameter(Class<?> paramClass, Annotation[] annotations) {
		this.classType = paramClass;

		for (Annotation a : annotations) {
			if (a instanceof ParameterName) {
				name = ((ParameterName)a).value();
			} else if (a instanceof ParameterOptional) {
				isOptional = true;
				defaultValue = ((ParameterOptional)a).defaultValue();

				if (defaultValue.equals("null")) {
					defaultValue = null;
				}
			}
		}

		if (name == null) {
			throw new RuntimeException("Constructor parameter of type " + paramClass.getName() + " does not have " + ParameterName.class.getName() + " annotation defined");
		}

		if (paramClass.isPrimitive()) {
			throw new RuntimeException("Constructor parameter of type " + paramClass.getName() + " is not supported (primitives are not supported)");
		}

		flattenedName = name.toLowerCase().replace(" ", "-");
	}

	public Class<?> getClassType() {
		return classType;
	}

	public String getName() {
		return name;
	}

	public String getFlattenedName() {
		return flattenedName;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isOptional() {
		return isOptional;
	}

	public Object toObject(String value) throws FileNotFoundException {
		if (value == null) {
			value = getDefaultValue();
		}

		if (getClassType() == Integer.class) {
			return Integer.parseInt(value);
		} else if (getClassType() == Float.class) {
			return Float.parseFloat(value);
		} else if (getClassType() == Double.class) {
			return Double.parseDouble(value);
		} else if (getClassType() == Long.class) {
			return Long.parseLong(value);
		} else if (getClassType() == Short.class) {
			return Short.parseShort(value);
		} else if (getClassType() == Boolean.class) {
			return Boolean.parseBoolean(value);
		} else if (getClassType() == File.class) {
			return new File(value);
		} else if (getClassType() == String.class) {
			return value;
		} else if (getClassType() == Character.class) {
			if (value.length() != 1) {
				throw new RuntimeException("Input " + getFlattenedName() + " must be 1 character only");
			}

			return value.charAt(0);
		} else if (getClassType() == BufferedInputStream.class) {
			return new BufferedInputStream(new FileInputStream(value));
		} else if (getClassType().isEnum()) {
			Enum<?>[] enumList = (Enum<?>[]) getClassType().getEnumConstants();

			for (Enum<?> myEnum : enumList) {
				if (value.equalsIgnoreCase(myEnum.toString())) {
					return myEnum;
				}
			}

			throw new RuntimeException("Enum value: " + value + " not found");
		}

		throw new RuntimeException("Unsupported type for class " + getClassType());
	}
}
