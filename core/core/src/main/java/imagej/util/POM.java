/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper class for working with Maven POMs.
 * 
 * @author Curtis Rueden
 */
public class POM {

	/** The POM's parsed XML DOM. */
	private final Document doc;

	/** XPath evaluation mechanism. */
	private final XPath xpath;

	/** Parses a POM from the given file. */
	public POM(final File file) throws ParserConfigurationException,
		SAXException, IOException
	{
		this(loadXML(file));
	}

	/** Parses a POM from the given input stream. */
	public POM(final InputStream in) throws ParserConfigurationException,
		SAXException, IOException
	{
		this(loadXML(in));
	}

	/** Parses a POM from the given string. */
	public POM(final String s) throws ParserConfigurationException,
		SAXException, IOException
	{
		this(loadXML(s));
	}

	private POM(final Document doc) {
		this.doc = doc;
		xpath = XPathFactory.newInstance().newXPath();
	}

	// -- POM methods --

	/** Gets the POM's groupId. */
	public String getGroupId() {
		final String groupId = xpath("//project/groupId");
		if (groupId != null) return groupId;
		return xpath("//project/parent/groupId");
	}

	/** Gets the POM's artifactId. */
	public String getArtifactId() {
		return xpath("//project/artifactId");
	}

	/** Gets the POM's version. */
	public String getVersion() {
		final String version = xpath("//project/version");
		if (version != null) return version;
		return xpath("//project/parent/version");
	}

	// -- Helper methods --

	/** Obtains the CDATA identified by the given XPath expression. */
	private String xpath(final String expression) {
		final Object result;
		try {
			result = xpath.evaluate(expression, doc, XPathConstants.NODESET);
		}
		catch (final XPathExpressionException e) {
			return null;
		}
		final NodeList nodes = (NodeList) result;
		if (nodes == null || nodes.getLength() == 0) return null;
		return getCData(nodes.item(0));
	}

	/** Loads an XML document from the given file. */
	private static Document loadXML(final File file)
		throws ParserConfigurationException, SAXException, IOException
	{
		return createBuilder().parse(file.getAbsolutePath());
	}

	/** Loads an XML document from the given input stream. */
	private static Document loadXML(final InputStream in)
		throws ParserConfigurationException, SAXException, IOException
	{
		return createBuilder().parse(in);
	}

	/** Loads an XML document from the given input stream. */
	private static Document loadXML(final String s)
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

}
