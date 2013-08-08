package com.googlecode.jsqlconverter.frontend.gui;

import com.googlecode.jsqlconverter.parser.Parser;
import com.googlecode.jsqlconverter.parser.ParserException;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;
import com.googlecode.jsqlconverter.producer.Producer;
import com.googlecode.jsqlconverter.producer.ProducerException;
import com.googlecode.jsqlconverter.producer.interfaces.FinalInterface;

public class ProcessThread extends Thread {
	private Parser parser;
	private Producer producer;
	private ParserCallback callback;

	public ProcessThread(Parser parser, Producer producer, ParserCallback callback) {
		this.parser = parser;
		this.producer = producer;
		this.callback = callback;
	}

	@Override
	public void run() {
		long beforeMili = System.currentTimeMillis();

		try {
			parser.parse(callback);

			if (producer instanceof FinalInterface) {
				((FinalInterface)producer).doFinal();
			}
		} catch (ParserException e) {
			e.printStackTrace();
		} catch (ProducerException e) {
			e.printStackTrace();
		}

		long runTimeMili = System.currentTimeMillis() - beforeMili;
		System.out.println("Runtime: " + runTimeMili + "ms");
	}
}
