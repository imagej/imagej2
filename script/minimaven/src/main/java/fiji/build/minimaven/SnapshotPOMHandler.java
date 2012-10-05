package fiji.build.minimaven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SnapshotPOMHandler extends DefaultHandler {
	protected String qName;
	protected String snapshotVersion, timestamp, buildNumber;

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		this.qName = qName;
	}

	public void endElement(String uri, String localName, String qName) {
		this.qName = null;
	}

	public void characters(char[] ch, int start, int length) {
		if (qName == null)
			; // ignore
		else if (qName.equals("version")) {
			String version = new String(ch, start, length).trim();
			if (version.endsWith("-SNAPSHOT"))
				snapshotVersion = version.substring(0, version.length() - "-SNAPSHOT".length());
		}
		else if (qName.equals("timestamp"))
			timestamp = new String(ch, start, length).trim();
		else if (qName.equals("buildNumber"))
			buildNumber = new String(ch, start, length).trim();
	}

	public static String parse(File xml) throws IOException, ParserConfigurationException, SAXException {
		return SnapshotPOMHandler.parse(new FileInputStream(xml));
	}

	public static String parse(InputStream in) throws IOException, ParserConfigurationException, SAXException {
		SnapshotPOMHandler handler = new SnapshotPOMHandler();
		XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		reader.setContentHandler(handler);
		reader.parse(new InputSource(in));
		if (handler.snapshotVersion != null && handler.timestamp != null && handler.buildNumber != null)
			return handler.snapshotVersion + "-" + handler.timestamp + "-" + handler.buildNumber;
		throw new IOException("Missing timestamp/build number: " + handler.timestamp + ", " + handler.buildNumber);
	}
}
