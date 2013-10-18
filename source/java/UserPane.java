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
import java.nio.charset.Charset;
import org.rsna.util.*;
import org.w3c.dom.*;

public class UserPane extends JPanel {

	private JTextField username;
	private JPasswordField password;
	private JTextField host;

	static UserPane pane = null;

	private static final Font labelFont = new Font( "SansSerif", Font.BOLD, 12 );
	private static final Font textFont = new Font( "Monospaced", Font.PLAIN, 12 );
	private static final int fieldWidth = 30;

	public static synchronized UserPane getInstance() {
		if (pane == null) pane = new UserPane();
		return pane;
	}

	private UserPane() {
		super();
		//setLayout(new FlowLayout(FlowLayout.LEFT));
		Configuration config = Configuration.getInstance();

		host = new JTextField(fieldWidth);
		host.setText(config.get("host"));
		host.setFont(textFont);

		username = new JTextField(fieldWidth);
		username.setText(config.get("username"));
		username.setFont(textFont);

		password = new JPasswordField(fieldWidth);
		password.setText(config.get("password"));
	}

	public JPanel getPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new RowLayout());
		addRow(panel, "Host: ", host);
		addRow(panel, "Username: ", username);
		addRow(panel, "Password: ", password);
		return panel;
	}

	private void addRow(JPanel panel, String s, JTextField field) {
		JLabel label = new JLabel(s);
		label.setFont(labelFont);
		panel.add(label);
		field.setFont(textFont);
		panel.add(field);
		panel.add(RowLayout.crlf());
	}

	public String getHost() {
		return host.getText().trim();
	}

	public String getUsername() {
		return username.getText().trim();
	}

	public String getPassword() {
		return new String(password.getPassword()).trim();
	}

}
