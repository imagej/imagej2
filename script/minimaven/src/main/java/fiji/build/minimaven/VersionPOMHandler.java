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

public class VersionPOMHandler extends DefaultHandler {
	protected String qName;
	protected String version;

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
			version = new String(ch, start, length).trim();
		}
	}

	public static String parse(File xml) throws IOException, ParserConfigurationException, SAXException {
		return parse(new FileInputStream(xml));
	}

	public static String parse(InputStream in) throws IOException, ParserConfigurationException, SAXException {
		VersionPOMHandler handler = new VersionPOMHandler();
		XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		reader.setContentHandler(handler);
		reader.parse(new InputSource(in));
		if (handler.version != null)
			return handler.version;
		throw new IOException("Missing version");
	}
}
