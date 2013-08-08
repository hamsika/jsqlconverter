package com.googlecode.jsqlconverter.frontend.gui;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SQLConverterGUI {
	private SQLConverterGUIMain mainApp;
	private JFrame frmJsqlconverter;
	private JComboBox parserComboBox;
	private JComboBox producerComboBox;

	/**
	 * Create the application.
	 * @param mainApp 
	 */
	public SQLConverterGUI(SQLConverterGUIMain mainApp) {
		this.mainApp = mainApp;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmJsqlconverter = new JFrame();
		frmJsqlconverter.setTitle("JSQLConverter");
		frmJsqlconverter.setBounds(100, 100, 650, 484);
		frmJsqlconverter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setEnabled(false);
		frmJsqlconverter.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel leftPanel = new JPanel();
		final JPanel parserArgPanel = new JPanel();
		final JPanel producerArgPanel = new JPanel();

		splitPane.setLeftComponent(leftPanel);
		
		parserComboBox = new JComboBox();
		parserComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String serviceName = (String)((JComboBox)evt.getSource()).getSelectedItem();

				mainApp.setParserName(serviceName);
				mainApp.addPanelComponents(getFrame(), parserArgPanel, producerArgPanel, true);
			}
		});
		JLabel parserLabel = new JLabel("Parser");

		GroupLayout gl_leftPanel = new GroupLayout(leftPanel);
		gl_leftPanel.setHorizontalGroup(
			gl_leftPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_leftPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_leftPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(parserArgPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
						.addGroup(gl_leftPanel.createSequentialGroup()
							.addComponent(parserLabel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(parserComboBox, 0, 245, Short.MAX_VALUE)))
					.addContainerGap())
		);
		gl_leftPanel.setVerticalGroup(
			gl_leftPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_leftPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_leftPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(parserLabel)
						.addComponent(parserComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(parserArgPanel, GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
					.addContainerGap())
		);
		GridBagLayout gbl_parserArgPanel = new GridBagLayout();
		gbl_parserArgPanel.columnWidths = new int[] {100};
		gbl_parserArgPanel.rowHeights = new int[]{20};
		gbl_parserArgPanel.columnWeights = new double[]{0.4};
		gbl_parserArgPanel.rowWeights = new double[]{0.0};
		parserArgPanel.setLayout(gbl_parserArgPanel);
		leftPanel.setLayout(gl_leftPanel);
		
		JPanel rightPanel = new JPanel();
		splitPane.setRightComponent(rightPanel);
		
		JLabel producerLabel = new JLabel("Producer");
		
		producerComboBox = new JComboBox();
		producerComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String serviceName = (String)((JComboBox)evt.getSource()).getSelectedItem();

				mainApp.setProducerName(serviceName);
				mainApp.addPanelComponents(getFrame(), parserArgPanel, producerArgPanel, false);
			}
		});

		GridBagLayout gbl_producerArgPanel = new GridBagLayout();
		gbl_producerArgPanel.columnWidths = new int[] {100};
		gbl_producerArgPanel.rowHeights = new int[] {20};
		gbl_producerArgPanel.columnWeights = new double[]{0.4};
		gbl_producerArgPanel.rowWeights = new double[]{0.0};
		producerArgPanel.setLayout(gbl_producerArgPanel);

		GroupLayout gl_rightPanel = new GroupLayout(rightPanel);
		gl_rightPanel.setHorizontalGroup(
			gl_rightPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_rightPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_rightPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(producerArgPanel, GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
						.addGroup(gl_rightPanel.createSequentialGroup()
							.addComponent(producerLabel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(producerComboBox, 0, 297, Short.MAX_VALUE)))
					.addContainerGap())
		);
		gl_rightPanel.setVerticalGroup(
			gl_rightPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_rightPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_rightPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(producerLabel)
						.addComponent(producerComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(producerArgPanel, GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
					.addContainerGap())
		);
		gbl_parserArgPanel.columnWidths = new int[] {100};
		gbl_parserArgPanel.rowHeights = new int[]{20};
		gbl_parserArgPanel.columnWeights = new double[]{0.4};
		gbl_parserArgPanel.rowWeights = new double[]{0.0};
		rightPanel.setLayout(gl_rightPanel);
		
		JScrollPane scrollPane = new JScrollPane();
		frmJsqlconverter.getContentPane().add(scrollPane, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JTextArea logTextArea = new JTextArea();
		logTextArea.setLineWrap(true);
		logTextArea.setEditable(false);
		logTextArea.setRows(4);
		panel.add(logTextArea);
	}

	public JComboBox getParserComboBox() {
		return parserComboBox;
	}

	public JFrame getFrame() {
		return frmJsqlconverter;
	}

	public JComboBox getProducerComboBox() {
		return producerComboBox;
	}
}