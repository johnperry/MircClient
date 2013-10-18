/*---------------------------------------------------------------
*  Copyright 2012 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.mde;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import org.rsna.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;

public class Util {

	public static final Charset utf8 = Charset.forName("UTF-8");

	public static String login() {
		try {
			UserPane up = UserPane.getInstance();
			String host = up.getHost();
			String username = URLEncoder.encode(up.getUsername(), "UTF-8");
			String password = URLEncoder.encode(up.getPassword(), "UTF-8");
			String urlString = host + "/login/ajax?username="+username+"&password="+password;
			URL url = new URL(urlString);
			HttpURLConnection conn = HttpUtil.getConnection(url);
			conn.setRequestMethod("GET");
			conn.setDoOutput(false);
			conn.connect();

			String setCookie = conn.getHeaderField("Set-Cookie");
			if ((setCookie != null) && setCookie.startsWith("RSNASESSION")) {
				String id = setCookie.substring( setCookie.indexOf("=") + 1 );
				return id.trim();
			}
		}
		catch (Exception ex) { ex.printStackTrace(); }
		return null;
	}

	public static String getLocalLibraries() {
		UserPane up = UserPane.getInstance();
		String host = up.getHost();
		String path = "/mirc/sortedlibraries";
		String result = doGet(host+path);
		if (result != null) {
			try {
				Document doc = getDocument(result);
				Element root = doc.getDocumentElement();
				NodeList nl = root.getElementsByTagName("Library");
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<nl.getLength(); i++) {
					Element e = (Element)nl.item(i);
					if (e.getAttribute("local").equals("yes")) {
						if (sb.length() > 0) sb.append(":");
						sb.append(Integer.toString(i));
					}
				}
				return sb.toString();
			}
			catch (Exception ex) { }
		}
		return "";
	}

	public static String doGet(String urlString) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = HttpUtil.getConnection(url);
			conn.setRequestMethod("GET");
			conn.setDoOutput(false);
			setAuthHeader(conn);
			conn.connect();
			return getContent(conn);
		}
		catch (Exception failed) { return null; }
	}

	public static String doPost(String urlString) {
		try {
			int k = urlString.indexOf("?");
			String query = "";
			if (k != -1) {
				query = urlString.substring( k+1 );
				urlString = urlString.substring(0, k);
			}
			URL url = new URL(urlString);
			HttpURLConnection conn = HttpUtil.getConnection(url);
			conn.setRequestMethod("POST");
			conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
			conn.setDoOutput(true);
			setAuthHeader(conn);
			conn.connect();
			OutputStreamWriter osw = new OutputStreamWriter( conn.getOutputStream(), utf8 );
			osw.write(query);
			osw.close();
			return getContent(conn);
		}
		catch (Exception failed) { return null; }
	}

	public static void setAuthHeader(HttpURLConnection conn) {
		String session = Configuration.getInstance().getSession();
		if (session != null) {
			conn.setRequestProperty( "Cookie", "RSNASESSION="+session );
		}
	}

	private static String getContent(HttpURLConnection conn) throws Exception {

		//See what kind of response we got
		String contentType = conn.getHeaderField("Content-Type");
		if ((contentType != null) && contentType.toLowerCase().contains("text")) {
			//Get the text response
			BufferedReader svrrdr =
				new BufferedReader(
					new InputStreamReader( conn.getInputStream(), utf8 ) );
			StringBuffer sb = new StringBuffer();
			char[] buf = new char[1024];
			int n;
			while ( (n = svrrdr.read(buf, 0, 1024)) != -1) sb.append(buf,0,n);
			svrrdr.close();
			return sb.toString();
		}
		else {
			//Just read the result and ignore it.
			long count = 0;
			BufferedInputStream bis =
				new BufferedInputStream( conn.getInputStream() );
			byte[] buf = new byte[1024];
			int n;
			while ( (n = bis.read(buf,0,1024)) != -1) count += n; //do nothing
			bis.close();
			return null;
		}
	}

	/**
	 * Parse an XML string.
	 * @param xmlString the file containing the XML to parse.
	 * @return the XML DOM document.
	 */
	public static Document getDocument(String xmlString) throws Exception {
		StringReader sr = new StringReader(xmlString);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(new InputSource(sr));
	}

}
