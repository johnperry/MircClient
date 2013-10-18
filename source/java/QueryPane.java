/*---------------------------------------------------------------
*  Copyright 2012 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.mde;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import org.rsna.util.HttpUtil;
import org.w3c.dom.*;

public class QueryPane extends JPanel implements ActionListener {

	private static final Font titleFont = new Font( "SansSerif", Font.BOLD, 18 );
	private static final Font labelFont = new Font( "SansSerif", Font.BOLD, 12 );
	private static final Font textFont = new Font( "Monospaced", Font.PLAIN, 12 );
	private static final int fieldWidth = 70;

	MircClient parent;

	JTextField titleField = new JTextField(fieldWidth);
	JTextField authorField = new JTextField(fieldWidth);
	JTextField ownerField = new JTextField(fieldWidth);
	JTextField freetextField = new JTextField(fieldWidth);

	JButton query;
	JButton login;
	JButton directory;

	Color bgColor;

	public QueryPane(MircClient parent) {
		super();
		this.parent = parent;
		bgColor = MircClient.bgColor;
		setLayout(new BorderLayout());
		setBackground(bgColor);

		JPanel header = new JPanel();
		header.setBackground(bgColor);
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		JLabel title = new JLabel("MIRC Query");
		title.setFont(titleFont);
		header.add(title);
		add(header, BorderLayout.NORTH);

		JPanel center = new CenterPane();
		center.setBackground(bgColor);

		JPanel x = new JPanel();
		x.setBackground(bgColor);
		x.add(center);

		JScrollPane jsp = new JScrollPane();
		jsp.setViewportView(x);

		add(jsp, BorderLayout.CENTER);

		JPanel footer = new JPanel();
		footer.setBackground(bgColor);
		login = new JButton("Login");
		login.addActionListener(this);
		footer.add(login);
		footer.add(Box.createHorizontalStrut(20));
		query = new JButton("Query");
		query.addActionListener(this);
		footer.add(query);
		footer.add(Box.createHorizontalStrut(20));
		directory = new JButton("Directory");
		directory.addActionListener(this);
		footer.add(directory);
		add(footer, BorderLayout.SOUTH);
	}

	class CenterPane extends JPanel {
		public CenterPane() {
			super();
			setLayout( new RowLayout() );
			setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			addRow("Free text:", freetextField);
			addRow("Title:", titleField);
			addRow("Author:", authorField);
			addRow("Owner:", ownerField);
		}
		private void addRow(String s, JTextField field) {
			JLabel sLabel = new JLabel(s);
			sLabel.setFont(labelFont);
			add(sLabel);
			field.setFont(textFont);
			add(field);
			add(RowLayout.crlf());
		}
	}

	public void actionPerformed(ActionEvent e) {
		Configuration config = Configuration.getInstance();
		Object source = e.getSource();
		if (source.equals(query)) doQuery();
		else if (source.equals(login)) config.doLogin(this);
		else if (source.equals(directory)) config.doDirectory(this);
	}

	private void doQuery() {
		Configuration config = Configuration.getInstance();
		String host = UserPane.getInstance().getHost();
		if (config.getSession() == null) {
			JOptionPane.showMessageDialog(this, "You must login first.");
			return;
		}

		String path = "/query";
		String qs = "?xml=yes";
		qs += "&server="+config.getLibraries()+"&firstresult=1&maxresults="+Integer.MAX_VALUE;

		String freetext = freetextField.getText().trim();
		String title = titleField.getText().trim();
		String author = authorField.getText().trim();
		String owner = ownerField.getText().trim();

		try {
			if (!freetext.equals("")) qs += "&document=" + URLEncoder.encode(freetext, "UTF-8");
			if (!title.equals("")) qs += "&title=" + URLEncoder.encode(title, "UTF-8");
			if (!author.equals("")) qs += "&author=" + URLEncoder.encode(author, "UTF-8");
			if (!owner.equals("")) qs += "&owner=" + URLEncoder.encode(owner, "UTF-8");
		}
		catch (Exception doBlankQuery) { }

		String result = Util.doPost(host + path + qs);
		if (result != null) {
			try {
				Document doc = Util.getDocument(result);
				QueryResultsPane qrp = parent.getResultsPane();
				parent.setSelectedComponent(qrp);
				qrp.loadResults(doc);
			}
			catch (Exception ex) {
				System.out.println(result);
			}
		}
	}

}
