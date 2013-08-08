package com.googlecode.jsqlconverter.frontend.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.HashMap;

import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;

import com.googlecode.jsqlconverter.definition.Statement;
import com.googlecode.jsqlconverter.parser.Parser;
import com.googlecode.jsqlconverter.parser.callback.ParserCallback;
import com.googlecode.jsqlconverter.producer.Producer;
import com.googlecode.jsqlconverter.producer.ProducerException;
import com.googlecode.jsqlconverter.service.EntryPoint;
import com.googlecode.jsqlconverter.service.Parameter;
import com.googlecode.jsqlconverter.service.Service;
import com.googlecode.jsqlconverter.service.ServiceUtil;

public class SQLConverterGUIMain implements ParserCallback {
	private ServiceUtil su;
	private String parserName;
	private String producerName;
	private Producer producer;
	private ProcessThread pThread;

	public SQLConverterGUIMain() throws IOException, ClassNotFoundException {
		su = new ServiceUtil();
	}

	public void populateDropdown(JComboBox comboBox, boolean isParser) {
		comboBox.addItem("");

		for (Service s : su.getServices()) {
			if (s.isParser() == isParser) {
				comboBox.addItem(s.getName());
			}
		}
	}

	public void addPanelComponents(final JFrame frame, final JPanel parserArgPanel, final JPanel producerArgPanel, boolean isParser) {
		String serviceName = (isParser) ? parserName : producerName;

		if (pThread != null && pThread.isAlive()) {
			pThread.interrupt();

			while (!pThread.isInterrupted()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (serviceName.isEmpty()) {
			return;
		}

		JPanel panel = (isParser) ? parserArgPanel : producerArgPanel;

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;

		Service[] services = su.getService(serviceName);

		panel.removeAll();

		for (EntryPoint ep : services[0].getEntryPoints()) {
			for (Parameter p : ep.getParameters()) {
				JLabel lbl = new JLabel();
				Component comp = lbl;

				if (p.getClassType().equals(String.class) || p.getClassType().equals(Character.class)) {
					lbl = new JLabel(getLabelText(p));
					JTextField jtf = new JTextField();
					jtf.setName(p.getFlattenedName());
					jtf.setText(p.getDefaultValue());

					if (p.getClassType().equals(Character.class)) {
						jtf.setInputVerifier(new InputVerifier() {
							@Override
							public boolean verify(JComponent comp) {
								JTextField jtf = (JTextField)comp;
	
								if (jtf.getText().length() > 1) {
									return false;
								} else {
									return true;
								}
							}
						});
					}

					comp = jtf;
				} else if (p.getClassType().equals(Boolean.class)) {
					comp = new JCheckBox(getLabelText(p), Boolean.parseBoolean(p.getDefaultValue()));
				} else if (p.getClassType().equals(BufferedInputStream.class) || p.getClassType().equals(PrintStream.class)) {
					lbl = new JLabel("Content");
					JTextArea jta = new JTextArea(18, 1);
					jta.setText(p.getDefaultValue());

					JScrollPane jsp = new JScrollPane(jta);
					jsp.setName(p.getFlattenedName());

					comp = jsp;
				} else if (p.getClassType().equals(Integer.class)) {
					lbl = new JLabel(getLabelText(p));
					JFormattedTextField tftf = new JFormattedTextField(NumberFormat.getIntegerInstance());
					tftf.setName(p.getFlattenedName());

					if (p.getDefaultValue() != null) {
						tftf.setValue(Integer.parseInt(p.getDefaultValue()));
					}

					comp = tftf;
				}

				if (!p.isOptional()) {
					lbl.setForeground(Color.RED);
				}

				if (lbl != null) {
					panel.add(lbl, gbc);
				}

				if (comp != lbl) {
					panel.add(comp, gbc);

					comp.addFocusListener(new FocusAdapter() {
						@Override
						public void focusLost(FocusEvent evt) {
							try {
								process(parserArgPanel, producerArgPanel);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else {
					System.out.println("Unhandled argument: " + p.getClassType());
				}
			}
		}

		frame.validate();
		frame.repaint();

		try {
			process(parserArgPanel, producerArgPanel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void process(JPanel parserArgPanel, JPanel producerArgPanel) throws FileNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (parserName.isEmpty() || producerName.isEmpty()) {
			return;
		}

		Parser parser;

		HashMap<String, Object> parserArgs = getArgs(parserArgPanel, true);
		HashMap<String, Object> producerArgs = getArgs(producerArgPanel, false);

		Service parserService = su.getFirstService(parserName, true);
		Service producerService = su.getFirstService(producerName, false);

		Object[] matchedParserArgs = parserService.getMatchingConstructorArguments(parserArgs);
		Object[] matchedProducerArgs = producerService.getMatchingConstructorArguments(producerArgs);

		parser = (Parser) parserService.newInstance(matchedParserArgs);
		producer = (Producer) producerService.newInstance(matchedProducerArgs);

		if (parser != null && producer != null) {
			pThread = new ProcessThread(parser, producer, this);
			pThread.start();
		}
	}

	private HashMap<String, Object> getArgs(JPanel panel, boolean isParser) {
		HashMap<String, Object> argMap = new HashMap<String, Object>();

		for (Component comp : panel.getComponents()) {
			Object value;

			if (comp.getName() != null) {
				if (comp instanceof JTextField) {
					value = ((JTextField)comp).getText();
				} else if (comp instanceof JCheckBox) {
					value = ((JCheckBox)comp).isSelected();
				} else if (comp instanceof JFormattedTextField) {
					value = ((JFormattedTextField)comp).getText();
				} else if (comp instanceof JScrollPane) {
					JViewport viewPort = (JViewport)((JScrollPane)comp).getComponent(0);
					JTextArea textArea = (JTextArea)viewPort.getComponent(0);
					
					if (isParser) {
						value = new BufferedInputStream(new ByteArrayInputStream(textArea.getText().getBytes()));
					} else {
						textArea.setText("");
						PrintStream ps = new PrintStream(new TextAreaPrintStream(textArea, System.out));  

						value = ps;
					}
				} else {
					System.out.println("Unsupported class type: " + comp.getClass());
					continue;
				}

				argMap.put(comp.getName(), value);
			}
		}

		return argMap;
	}

	public String getLabelText(Parameter p) {
		return p.getName() + (p.isOptional() ? "" : " *");
	}

	public void setParserName(String name) {
		this.parserName = name;
	}

	public void setProducerName(String name) {
		this.producerName = name;
	}

	@Override
	public void produceStatement(Statement statement) {
		try {
			producer.produce(statement);
		} catch (ProducerException e) {
			e.printStackTrace();
			log(e.getMessage());
		}
	}

	@Override
	public void log(String message) {
		
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					SQLConverterGUIMain mainApp = new SQLConverterGUIMain();
					SQLConverterGUI app = new SQLConverterGUI(mainApp);

					mainApp.populateDropdown(app.getParserComboBox(), true);
					mainApp.populateDropdown(app.getProducerComboBox(), false);

					app.getFrame().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
