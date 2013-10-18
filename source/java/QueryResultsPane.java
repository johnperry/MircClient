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

public class QueryResultsPane extends JPanel implements ActionListener {

	private static final Font titleFont = new Font( "SansSerif", Font.BOLD, 18 );
	private static final Font labelFont = new Font( "SansSerif", Font.BOLD, 12 );
	private static final Font textFont = new Font( "Monospaced", Font.PLAIN, 12 );

	MircClient parent;
	CenterPane center;

	JButton save;
	JButton directory;
	JButton selectAll;
	JButton clearAll;

	Color bgColor;

	public QueryResultsPane(MircClient parent) {
		super();
		this.parent = parent;
		bgColor = MircClient.bgColor;
		setLayout(new BorderLayout());
		setBackground(bgColor);

		JPanel header = new JPanel();
		header.setBackground(bgColor);
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		JLabel title = new JLabel("MIRC Query Results");
		title.setFont(titleFont);
		header.add(title);
		add(header, BorderLayout.NORTH);

		center = new CenterPane();
		center.setBackground(bgColor);

		JPanel x = new JPanel();
		x.setLayout(new FlowLayout(FlowLayout.LEFT));
		x.setBackground(bgColor);
		x.add(center);

		JScrollPane jsp = new JScrollPane();
		jsp.getVerticalScrollBar().setUnitIncrement(10);
		jsp.setViewportView(x);

		add(jsp, BorderLayout.CENTER);

		JPanel footer = new JPanel();
		footer.setBackground(bgColor);

		directory = new JButton("Directory");
		directory.addActionListener(this);
		footer.add(directory);

		footer.add(Box.createHorizontalStrut(20));

		selectAll = new JButton("Select All");
		selectAll.addActionListener(this);
		footer.add(selectAll);

		footer.add(Box.createHorizontalStrut(20));

		clearAll = new JButton("Clear All");
		clearAll.addActionListener(this);
		footer.add(clearAll);

		footer.add(Box.createHorizontalStrut(20));

		save = new JButton("Import");
		save.addActionListener(this);
		footer.add(save);

		add(footer, BorderLayout.SOUTH);
	}

	public void loadResults(Document doc) {
		center.loadResults(doc);
	}

	class CenterPane extends JPanel implements ActionListener {
		public CenterPane() {
			super(new RowLayout(5, 0));
		}
		public void loadResults(Document doc) {
			removeAll();
			Element root = doc.getDocumentElement();
			NodeList nl = root.getElementsByTagName("MIRCdocument");
			for (int i=0; i<nl.getLength(); i++) {
				Element result = (Element)nl.item(i);
				MIRCdocument md = new MIRCdocument((Element)nl.item(i));
				CB cb = new CB(md);
				add(cb);

				BLabel bl = new BLabel(md.title, cb);
				bl.setToolTipText(md.docref);
				bl.addActionListener(this);
				add(bl);

				add(RowLayout.crlf());
			}
		}
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source instanceof BLabel) doHandleBLabel((BLabel)source);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Configuration config = Configuration.getInstance();
		Object source = e.getSource();
		if (source.equals(directory)) config.doDirectory(this);
		else if (source.equals(save)) doSave();
		else if (source.equals(selectAll)) doSetCBs(true);
		else if (source.equals(clearAll)) doSetCBs(false);
	}

	private void doSetCBs(boolean select) {
		Component[] components = center.getComponents();
		for (Component c : components) {
			if (c instanceof CB) ((CB)c).setSelected(select);
		}
	}

	private void doHandleBLabel(BLabel bl) {
		bl.cb.setSelected( !bl.cb.isSelected() );
	}

	class CB extends JCheckBox {
		public final MIRCdocument md;
		public CB(MIRCdocument md) {
			super();
			setBackground(bgColor);
			setSelected(true);
			this.md = md;
		}
	}

	class BLabel extends JButton {
		public final CB cb;
		public BLabel(String label, CB cb) {
			super(label);
			this.cb = cb;
			setContentAreaFilled(false);
			setBorderPainted(false);
			setMargin( new Insets(0, 0, 0, 0) );
		}
	}

	private void doSave() {
		Component[] components = center.getComponents();
		LinkedList<MIRCdocument> mds = new LinkedList<MIRCdocument>();
		for (Component c : components) {
			if (c instanceof CB) {
				CB cb = (CB)c;
				if (cb.isSelected()) mds.add(cb.md);
			}
		}
		if (mds.size() > 0) {
			ColorPane text = parent.getTextPane();
			text.setText("");
			File dir = Configuration.getInstance().getDirectory();
			new ImportThread(mds, text, dir).start();
			parent.setSelectedComponent(parent.getProgressPane());
		}
		else JOptionPane.showMessageDialog(this, "No MIRCdocuments are selected.");
	}

	class MIRCdocument {
		public String title = "No title found";
		public String docref = "";
		public MIRCdocument(String title, String docref) {
			this.title = title;
			this.docref = docref;
		}
		public MIRCdocument(Element md) {
			this.docref = md.getAttribute("docref");
			NodeList nlt = md.getElementsByTagName("title");
			if (nlt.getLength() > 0) title = nlt.item(0).getTextContent();
		}
	}

	class ImportThread extends Thread {
		LinkedList<MIRCdocument> mds;
		ColorPane text;
		File dir;
		public ImportThread(LinkedList<MIRCdocument> mds, ColorPane text, File dir) {
			super();
			this.mds = mds;
			this.text = text;
			this.dir = dir;
		}
		public void run() {
			int n = 1;
			for (MIRCdocument md : mds) {
				String name = importDocument(md.docref+"?zip");
				if (name != null) text.println(Color.black, (n++) + ": " + md.title + "\n          ["+name+"]\n");
				else text.println(Color.red, (n++) + ": " + md.title + "\n          ["+md.docref+"]\n");
			}
			text.println("Done.");
		}
		private String importDocument(String urlString) {
			try {
				URL url = new URL(urlString);
				HttpURLConnection conn = HttpUtil.getConnection(url);
				conn.setRequestMethod("GET");
				conn.setDoOutput(false);
				Util.setAuthHeader(conn);
				conn.connect();

				String contentType = conn.getHeaderField("Content-Type");
				boolean contentTypeOK = (contentType != null) && contentType.toLowerCase().contains("application/zip");

				String disposition = conn.getHeaderField("Content-Disposition");
				File file = null;
				if (contentTypeOK && (disposition != null)) {
					int k = disposition.indexOf("\"");
					if (k != -1) {
						k++;
						int kk = disposition.indexOf("\"", k);
						file = new File(dir, disposition.substring(k, kk));
					}
				}
				if (file != null) {
					BufferedOutputStream bos =
						new BufferedOutputStream( new FileOutputStream(file) );
					BufferedInputStream bis =
						new BufferedInputStream( conn.getInputStream() );
					byte[] buf = new byte[1024];
					int n;
					while ( (n = bis.read(buf,0,1024)) != -1) bos.write(buf,0,n);
					bos.close();
					bis.close();
					return file.getName();
				}
				else {
					//Just read the result and ignore it.
					BufferedInputStream bis =
						new BufferedInputStream( conn.getInputStream() );
					byte[] buf = new byte[1024];
					int n;
					while ( (n = bis.read(buf,0,1024)) != -1) ; //do nothing
					bis.close();
				}
			}
			catch (Exception ex) { ex.printStackTrace(); }
			return null;
		}
	}

}
