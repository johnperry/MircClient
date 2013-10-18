/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
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

public class MircClient extends JFrame {

    static final String windowTitle = "MIRC Client Utility";
	static final Color bgColor = new Color(0xc6d8f9);

	final int minWidth = 700;
	final int minHeight = 500;

	JTabbedPane jtp;

	//Stuff for the Progress Pane
	JPanel jp;
	JScrollPane jsp;
    ColorPane text;

    UserPane user;
    QueryPane qPane;
    QueryResultsPane qrPane;

    public static void main(String args[]) {
        new MircClient();
    }

    public MircClient() {
		super();
		setTitle(windowTitle);
		JPanel panel = new JPanel(new BorderLayout());
		getContentPane().add(panel,BorderLayout.CENTER);

		Configuration.load();

		//Make the tabbed pane for the center
		jtp = new JTabbedPane();
		panel.add(jtp, BorderLayout.CENTER);

		//Put in the Query pane
		qPane = new QueryPane(this);
		jtp.add("Query", qPane);

		//Put in the Query Results pane
		qrPane = new QueryResultsPane(this);
		jtp.add("Results", qrPane);

		//Make the text pane for console output
		text = new ColorPane();
		text.setContentType("text/plain");
		text.setScrollableTracksViewportWidth(false);
		jp = new JPanel(new BorderLayout());
		jp.add(text, BorderLayout.CENTER);
		jsp = new JScrollPane();
		jsp.setViewportView(jp);
		jtp.add("Progress", jsp);

        addWindowListener(new WindowCloser(this));

        pack();
        centerFrame();
        setVisible(true);
	}

	public void setSelectedComponent(Component c) {
		jtp.setSelectedComponent(c);
	}

	public QueryResultsPane getResultsPane() {
		return qrPane;
	}

	public ColorPane getTextPane() {
		return text;
	}

	public Component getProgressPane() {
		return jsp;
	}

    private void centerFrame() {
        Toolkit t = getToolkit();
        Dimension scr = t.getScreenSize ();
        setSize(minWidth, minHeight);
        setLocation (new Point ( (scr.width - minWidth)/2,
                                 (scr.height - minHeight)/2) );
    }

    class WindowCloser extends WindowAdapter {
		private Component parent;
		public WindowCloser(JFrame parent) {
			super();
			this.parent = parent;
			parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}
		public void windowClosing(WindowEvent evt) {
			Configuration.getInstance().save();
			System.exit(0);
		}
    }

}
