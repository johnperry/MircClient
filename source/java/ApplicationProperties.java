/*---------------------------------------------------------------
*  Copyright 2012 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.mde;

import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

/**
 * An extension of java.util.Properties.
 */
public class ApplicationProperties extends Properties {

	String filename = "application.properties";
	EventListenerList listenerList;
	boolean notifyOnChange = false;

	/**
	 * Class constructor; creates a new Properties object and
	 * loads the properties file, ignoring exceptions.
	 *
	 * @param filename the path to the properties file.
	 */
	public ApplicationProperties(String filename) {
		this(filename,false);
	}

	/**
	 * Class constructor; creates a new Properties object with
	 * the specified automatic notification on changes and
	 * loads the properties file, ignoring exceptions.
	 *
	 * @param filename the path to the properties file.
	 * @param notifyOnChange true if every property change is
	 * to generate PropertyEvents; false otherwise.
	 */
	public ApplicationProperties(String filename, boolean notifyOnChange) {
		super();
		this.filename = filename;
		load();
	}

	/**
	 * Get the filename.
	 * @return the filename.
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Load the properties file.
	 * @return true if the load was successful; false otherwise.
	 */
	public boolean load() {
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(filename);
			super.load(stream);
			stream.close();
			return true;
		}
		catch (Exception e) {
			if (stream != null) {
				try { stream.close(); }
				catch (Exception ignore) { }
			}
			return false;
		}
	}

	/**
	 * Save the properties file.
	 * @return true if the save was successful; false otherwise.
	 */
	public boolean store() {
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(filename);
			super.store(stream,filename);
			stream.flush();
			stream.close();
			return true;
		}
		catch (Exception e) {
			if (stream != null) {
				try { stream.close(); }
				catch (Exception ignore) { }
			}
			return false;
		}
	}

}
