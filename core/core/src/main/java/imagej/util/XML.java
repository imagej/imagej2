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

package imagej.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper class for working with XML documents.
 * 
 * @author Curtis Rueden
 */
public class XML {

	/** The parsed XML DOM. */
	private final Document doc;

	/** XPath evaluation mechanism. */
	private final XPath xpath;

	/** Parses XML from the given file. */
	public XML(final File file) throws ParserConfigurationException,
		SAXException, IOException
	{
		this(loadXML(file));
	}

	/** Parses XML from the given input stream. */
	public XML(final InputStream in) throws ParserConfigurationException,
		SAXException, IOException
	{
		this(loadXML(in));
	}

	/** Parses XML from the given string. */
	public XML(final String s) throws ParserConfigurationException,
		SAXException, IOException
	{
		this(loadXML(s));
	}

	/** Creates an XML object for an existing document. */
	public XML(final Document doc) {
		this.doc = doc;
		xpath = XPathFactory.newInstance().newXPath();
	}

	// -- XML methods --

	/** Gets the XML's DOM representation. */
	public Document getDocument() {
		return doc;
	}

	/** Obtains the CDATA identified by the given XPath expression. */
	public String cdata(final String expression) {
		final NodeList nodes = xpath(expression);
		if (nodes == null || nodes.getLength() == 0) return null;
		return getCData(nodes.item(0));
	}

	/** Obtains the nodes identified by the given XPath expression. */
	public NodeList xpath(final String expression) {
		final Object result;
		try {
			result = xpath.evaluate(expression, doc, XPathConstants.NODESET);
		}
		catch (final XPathExpressionException e) {
			return null;
		}
		return (NodeList) result;
	}

	// -- Object methods --

	@Override
	public String toString() {
		try {
			return dumpXML(doc);
		}
		catch (final TransformerException exc) {
			// NB: Return the exception stack trace as the string.
			// Although this is a bad idea, I find it somehow hilarious.
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			exc.printStackTrace(new PrintStream(out));
			return out.toString();
		}
	}

	// -- Helper methods --

	/** Loads an XML document from the given file. */
	private static Document loadXML(final File file)
		throws ParserConfigurationException, SAXException, IOException
	{
		return createBuilder().parse(file.getAbsolutePath());
	}

	/** Loads an XML document from the given input stream. */
	protected static Document loadXML(final InputStream in)
		throws ParserConfigurationException, SAXException, IOException
	{
		return createBuilder().parse(in);
	}

	/** Loads an XML document from the given input stream. */
	protected static Document loadXML(final String s)
		throws ParserConfigurationException, SAXException, IOException
	{
		return createBuilder().parse(new ByteArrayInputStream(s.getBytes()));
	}

	/** Creates an XML document builder. */
	private static DocumentBuilder createBuilder()
		throws ParserConfigurationException
	{
		return DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}

	/** Gets the CData beneath the given node. */
	private static String getCData(final Node item) {
		final NodeList children = item.getChildNodes();
		if (children == null || children.getLength() == 0) return null;
		for (int i = 0; i < children.getLength(); i++) {
			final Node child = children.item(i);
			if (child.getNodeType() != Node.TEXT_NODE) continue;
			return child.getNodeValue();
		}
		return null;
	}

	/** Converts the given DOM to a string. */
	private static String dumpXML(final Document doc)
		throws TransformerException
	{
		final Source source = new DOMSource(doc);
		final StringWriter stringWriter = new StringWriter();
		final Result result = new StreamResult(stringWriter);
		final TransformerFactory factory = TransformerFactory.newInstance();
		final Transformer transformer = factory.newTransformer();
		transformer.transform(source, result);
		return stringWriter.getBuffer().toString();
	}

}
