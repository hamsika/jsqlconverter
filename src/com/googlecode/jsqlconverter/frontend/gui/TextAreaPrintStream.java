package com.googlecode.jsqlconverter.frontend.gui;

import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;

public class TextAreaPrintStream extends PrintStream {
	private JTextArea textArea;

	public TextAreaPrintStream(JTextArea textArea, OutputStream out) {
		super(out);
		this.textArea = textArea;
	}

	@Override
	public void println(String x) {
		textArea.append(x);
	}

	/*
	@Override
	public void print(boolean b) {
		textArea.append(b);
	}

	@Override
	public void print(char c) {
		textArea.append(c);
	}

	@Override
	public void print(char[] s) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void print(double d) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void print(float f) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void print(int i) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void print(long l) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void print(Object obj) {
		System.out.println("PRINTING LINE!!!!");
	}
	 */

	@Override
	public void print(String s) {
		textArea.append(s);
	}

	@Override
	public void println() {
		textArea.append("\n");
	}

	@Override
	public void println(boolean x) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void println(char x) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void println(char[] x) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void println(double x) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void println(float x) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void println(int x) {
		System.out.println("PRINTING LINE!!!!");
	}

	@Override
	public void println(long x) {
		System.out.println("PRINTING LINE!!!!");
		
	}

	/*
	@Override
	public void println(Object x) {
		textArea.append(x);
	}
	*/

	@Override
	public void write(byte[] buf, int off, int len) {
		textArea.append(new String(buf, off, len));
	}

	@Override
	public void write(int b) {
		System.out.println("PRINTING LINE!!!!");
	}

	
}
