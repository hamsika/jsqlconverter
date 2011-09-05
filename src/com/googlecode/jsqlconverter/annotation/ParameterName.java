package com.googlecode.jsqlconverter.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface ParameterName {
	String value();
}
