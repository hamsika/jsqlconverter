package com.googlecode.jsqlconverter.producer;

import java.io.IOException;

public class ProducerException  extends IOException {
	public ProducerException(String message) {
		super(message);
	}

	public ProducerException(String message, Throwable cause) {
		super(message, cause);
	}
}
