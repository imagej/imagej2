/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

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

/**
 * A helper class to read information from Maven-built .jar files.
 * 
 * @author Johannes Schindelin
 */
public class POMParser extends DefaultHandler {
	private FileObject file;
	private String body, prefix;

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
		jar.close();
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
				file.descriptionFromPOM = true;
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
