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
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.rsna.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Configuration {

	private static Configuration config = null;
	private static final String propsName = "MircClient.properties";

	private String session = null;
	private String libraries = "";
	private File directory = null;
	private ApplicationProperties props;
	private JFileChooser chooser = null;

    protected Configuration() {
		props = new ApplicationProperties(propsName);
	}

	public static Configuration load() {
		config = new Configuration();
		String dir = config.get("dir");
		if (!dir.equals("")) config.setDirectory(new File(dir));
		else config.setDirectory(new File("docs"));
		return config;
	}

	public static Configuration getInstance() {
		return config;
	}

	public String get(String name) {
		return props.getProperty(name, "").trim();
	}

	public void set(String name, String value) {
		props.setProperty(name, value);
	}

	public void setSession(String session) {
		this.session = session;
	}

	public String getSession() {
		return session;
	}

	public void setLibraries(String libraries) {
		this.libraries = libraries;
	}

	public String getLibraries() {
		return libraries;
	}

	public void setDirectory(File directory) {
		set("dir", directory.getAbsolutePath());
		directory.mkdirs();
		this.directory = directory;
	}

	public File getDirectory() {
		if (directory == null) setDirectory(new File("docs"));
		return directory;
	}

	public void save() {
		UserPane up = UserPane.getInstance();
		props.setProperty("username", up.getUsername());
		//props.setProperty("password", up.getPassword()); //don't save the password
		props.setProperty("host", up.getHost());
		if (directory != null) props.setProperty("dir", directory.getAbsolutePath());
		props.store();
	}

	public void doDirectory(Component parent) {
		if (chooser == null) {
			String dir = config.get("dir");
			if (dir.equals("")) dir = System.getProperty("user.dir");
			chooser = new JFileChooser(new File(dir));
			chooser.setDialogTitle("Select a directory in which to store downloaded MIRCdocuments");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			File directory = chooser.getSelectedFile();
			if (directory.getAbsolutePath().equals(System.getProperty("user.dir"))) {
				directory = new File(directory, "docs");
			}
			setDirectory(directory);
			directory.mkdirs();
		}
	}

	public void doLogin(Component parent) {
		UserPane up = UserPane.getInstance();
		int result = JOptionPane.showOptionDialog(
						parent,
						up.getPane(),
						"Enter the MIRC URL and user credentials",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null, //icon
						null, //options
						null); //initialValue
		if (result == JOptionPane.OK_OPTION) {
			String session = Util.login();
			if (session != null) {
				setSession(session);
				setLibraries(Util.getLocalLibraries());
			}
			else {
				JOptionPane.showMessageDialog(parent, "Unable to log in.\n\nThe host field or credentials\nmay be incorrect.");
			}
		}
	}

}
