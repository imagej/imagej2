package imagej.updater.core;

import imagej.updater.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class POMParser extends DefaultHandler {
	protected FileObject file;
	protected String body, prefix;

	public POMParser(final FileObject file) {
		this.file = file;
	}

	public static void fillMetadataFromJar(final FileObject object, final File file) throws ParserConfigurationException, IOException, SAXException {
		final JarFile jar = new JarFile(file);
		for (final JarEntry entry : Util.iterate(jar.entries())) {
			if (entry.getName().matches("META-INF/maven/.*/pom.xml")) {
				new POMParser(object).read(jar.getInputStream(entry));
				break;
			}
		}
	}

	public void read(final InputStream in) throws ParserConfigurationException, IOException, SAXException {
		final InputSource inputSource = new InputSource(in);
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);

		// commented-out as per Postel's law
		// factory.setValidating(true);

		final SAXParser parser = factory.newSAXParser();
		final XMLReader xr = parser.getXMLReader();
		xr.setContentHandler(this);
		xr.setErrorHandler(new XMLFileErrorHandler());
		xr.parse(inputSource);
	}

	@Override
	public void startDocument() {
		body = "";
		prefix = "";
	}

	@Override
	public void endDocument() {}

	@Override
	public void startElement(final String uri, final String name,
		final String qName, final Attributes atts)
	{
		prefix += ">" + qName;
		body = "";
	}

	@Override
	public void
		endElement(final String uri, final String name, final String qName)
	{
		if (prefix.equals(">project>description")) {
			if (!"".equals(body)) {
				file.description = body;
			}
		}
		else if (prefix.equals(">project>developers>developer>name")) {
			file.addAuthor(body);
		}

		prefix = prefix.substring(0, prefix.length() - 1 - qName.length());
	}

	@Override
	public void characters(final char ch[], final int start, final int length) {
		body += new String(ch, start, length);
	}

}
