package com.googlecode.jsqlconverter.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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

		if (Integer.class.isAssignableFrom(getClassType())) {
			return Integer.parseInt(value);
		} else if (Float.class.isAssignableFrom(getClassType())) {
			return Float.parseFloat(value);
		} else if (Double.class.isAssignableFrom(getClassType())) {
			return Double.parseDouble(value);
		} else if (Long.class.isAssignableFrom(getClassType())) {
			return Long.parseLong(value);
		} else if (Short.class.isAssignableFrom(getClassType())) {
			return Short.parseShort(value);
		} else if (Boolean.class.isAssignableFrom(getClassType())) {
			return Boolean.parseBoolean(value);
		} else if (File.class.isAssignableFrom(getClassType())) {
			return new File(value);
		} else if (String.class.isAssignableFrom(getClassType())) {
			return value;
		} else if (Character.class.isAssignableFrom(getClassType())) {
			if (value.length() != 1) {
				throw new RuntimeException("Input " + getFlattenedName() + " must be 1 character only");
			}

			return value.charAt(0);
		} else if (InputStream.class.isAssignableFrom(getClassType())) {
			return new BufferedInputStream(new FileInputStream(value));
		} else if (Enum.class.isAssignableFrom(getClassType())) {
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
