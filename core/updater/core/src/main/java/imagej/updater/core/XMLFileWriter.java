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

package imagej.updater.core;

import imagej.updater.core.FilesCollection.UpdateSite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

public class XMLFileWriter {

	protected FilesCollection files;
	protected TransformerHandler handler;
	protected final String XALAN_INDENT_AMOUNT = "{http://xml.apache.org/xslt}"
		+ "indent-amount";
	protected final static String dtd = "<!DOCTYPE pluginRecords [\n"
		+ "<!ELEMENT pluginRecords (update-site*, plugin*)>\n"
		+ "<!ELEMENT update-site EMPTY>\n"
		+ "<!ELEMENT plugin (platform*, category*, version?, previous-version*)>\n"
		+ "<!ELEMENT version (description?, dependency*, link*, author*)>\n"
		+ "<!ELEMENT previous-version EMPTY>\n"
		+ "<!ELEMENT description (#PCDATA)>\n" + "<!ELEMENT dependency EMPTY>\n"
		+ "<!ELEMENT link (#PCDATA)>\n" + "<!ELEMENT author (#PCDATA)>\n"
		+ "<!ELEMENT platform (#PCDATA)>\n" + "<!ELEMENT category (#PCDATA)>\n"
		+ "<!ATTLIST update-site name CDATA #REQUIRED>\n"
		+ "<!ATTLIST update-site url CDATA #REQUIRED>\n"
		+ "<!ATTLIST update-site ssh-host CDATA #IMPLIED>\n"
		+ "<!ATTLIST update-site upload-directory CDATA #IMPLIED>\n"
		+ "<!ATTLIST update-site timestamp CDATA #REQUIRED>\n"
		+ "<!ATTLIST plugin update-site CDATA #IMPLIED>\n"
		+ "<!ATTLIST plugin filename CDATA #REQUIRED>\n"
		+ "<!ATTLIST plugin executable CDATA #IMPLIED>\n"
		+ "<!ATTLIST dependency filename CDATA #REQUIRED>\n"
		+ "<!ATTLIST dependency timestamp CDATA #IMPLIED>\n"
		+ "<!ATTLIST dependency overrides CDATA #IMPLIED>\n"
		+ "<!ATTLIST version timestamp CDATA #REQUIRED>\n"
		+ "<!ATTLIST version checksum CDATA #REQUIRED>\n"
		+ "<!ATTLIST version filesize CDATA #REQUIRED>\n"
		+ "<!ATTLIST previous-version timestamp CDATA #REQUIRED>\n"
		+ "<!ATTLIST previous-version checksum CDATA #REQUIRED>]>\n";

	public XMLFileWriter(final FilesCollection files) {
		this.files = files;
	}

	public byte[] toByteArray(final boolean local) throws SAXException,
		TransformerConfigurationException, IOException,
		ParserConfigurationException
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		write(out, local);
		return out.toByteArray();
	}

	public byte[] toCompressedByteArray(final boolean local) throws SAXException,
		TransformerConfigurationException, IOException,
		ParserConfigurationException
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		write(new GZIPOutputStream(out), local);
		return out.toByteArray();
	}

	public void validate(final boolean local) throws SAXException,
		TransformerConfigurationException, IOException,
		ParserConfigurationException
	{
		final ByteArrayInputStream in =
			new ByteArrayInputStream(toByteArray(local));
		validate(in);
	}

	public void write(final OutputStream out, final boolean local)
		throws SAXException, TransformerConfigurationException, IOException,
		ParserConfigurationException
	{
		createHandler(out);

		handler.startDocument();
		final AttributesImpl attr = new AttributesImpl();

		handler.startElement("", "", "pluginRecords", attr);
		if (local) {
			for (final String name : files.getUpdateSiteNames()) {
				attr.clear();
				final UpdateSite site = files.getUpdateSite(name);
				setAttribute(attr, "name", name);
				setAttribute(attr, "url", site.url);
				if (site.sshHost != null) setAttribute(attr, "ssh-host", site.sshHost);
				if (site.uploadDirectory != null) setAttribute(attr,
					"upload-directory", site.uploadDirectory);
				setAttribute(attr, "timestamp", "" + site.timestamp);
				writeSimpleTag("update-site", null, attr);
			}
		}

		for (final FileObject file : files.managedFiles()) {
			attr.clear();
			assert (file.updateSite != null && !file.updateSite.equals(""));
			if (local) setAttribute(attr, "update-site", file.updateSite);
			setAttribute(attr, "filename", file.filename);
			if (file.executable) setAttribute(attr, "executable", "true");
			handler.startElement("", "", "plugin", attr);
			writeSimpleTags("platform", file.getPlatforms());
			writeSimpleTags("category", file.getCategories());

			final FileObject.Version current = file.current;
			if (file.getChecksum() != null) {
				attr.clear();
				setAttribute(attr, "checksum", file.getChecksum());
				setAttribute(attr, "timestamp", file.getTimestamp());
				setAttribute(attr, "filesize", file.filesize);
				handler.startElement("", "", "version", attr);
				if (file.description != null) writeSimpleTag("description",
					file.description);

				for (final Dependency dependency : file.getDependencies()) {
					attr.clear();
					setAttribute(attr, "filename", dependency.filename);
					setAttribute(attr, "timestamp", dependency.timestamp);
					if (dependency.overrides) setAttribute(attr, "overrides", "true");
					writeSimpleTag("dependency", null, attr);
				}

				writeSimpleTags("link", file.getLinks());
				writeSimpleTags("author", file.getAuthors());
				handler.endElement("", "", "version");
			}
			if (current != null && !current.checksum.equals(file.getChecksum())) file
				.addPreviousVersion(current.checksum, current.timestamp);
			for (final FileObject.Version version : file.getPrevious()) {
				attr.clear();
				setAttribute(attr, "timestamp", version.timestamp);
				setAttribute(attr, "checksum", version.checksum);
				writeSimpleTag("previous-version", null, attr);
			}
			handler.endElement("", "", "plugin");
		}
		handler.endElement("", "", "pluginRecords");
		handler.endDocument();
		out.flush();
		out.close();
	}

	protected void setAttribute(final AttributesImpl attributes,
		final String key, final long value)
	{
		setAttribute(attributes, key, "" + value);
	}

	protected void setAttribute(final AttributesImpl attributes,
		final String key, final String value)
	{
		attributes.addAttribute("", "", key, "CDATA", value);
	}

	protected void writeSimpleTags(final String tagName,
		final Iterable<String> values) throws SAXException
	{
		for (final String value : values)
			writeSimpleTag(tagName, value);
	}

	protected void writeSimpleTag(final String tagName, final String value)
		throws SAXException
	{
		writeSimpleTag(tagName, value, new AttributesImpl());
	}

	protected void writeSimpleTag(final String tagName, final String value,
		final AttributesImpl attributes) throws SAXException
	{
		handler.startElement("", "", tagName, attributes);
		if (value != null) handler.characters(value.toCharArray(), 0, value
			.length());
		handler.endElement("", "", tagName);
	}

	protected void createHandler(final OutputStream outputStream)
		throws TransformerConfigurationException
	{
		final StreamResult streamResult =
			new StreamResult(new DTDInserter(outputStream));
		final SAXTransformerFactory tf =
			(SAXTransformerFactory) TransformerFactory.newInstance();

		handler = tf.newTransformerHandler();
		final Transformer serializer = handler.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.setOutputProperty(XALAN_INDENT_AMOUNT, "4");
		handler.setResult(streamResult);
	}

	protected void validate(final InputStream inputStream)
		throws ParserConfigurationException, SAXException, IOException
	{
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		final SAXParser parser = factory.newSAXParser();

		final XMLReader xr = parser.getXMLReader();
		xr.setErrorHandler(new XMLFileErrorHandler());
		xr.parse(new InputSource(inputStream));
		inputStream.close();
	}

	/*
	 * This is an ugly hack; SAX does not let us embed the DTD in a
	 * transformer, but we really want to.
	 *
	 * So this class wraps an output stream, and inserts the DTD when
	 * it sees the string "<fileRecords>".
	 *
	 * It fails if that string is not written in one go.
	 */
	static class DTDInserter extends OutputStream {

		private final OutputStream out;
		private boolean dtdInserted;

		DTDInserter(final OutputStream out) {
			this.out = out;
		}

		@Override
		public void close() throws IOException {
			if (!dtdInserted) throw new IOException("DTD not inserted!");
			out.close();
		}

		@Override
		public void flush() throws IOException {
			out.flush();
		}

		@Override
		public void write(final byte[] b, final int off, final int len)
			throws IOException
		{
			if (!insertDTDIfNecessary(b, off, len)) out.write(b, off, len);
		}

		@Override
		public void write(final byte[] b) throws IOException {
			if (!insertDTDIfNecessary(b, 0, b.length)) out.write(b);
		}

		@Override
		public void write(final int b) throws IOException {
			out.write(b);
		}

		private boolean insertDTDIfNecessary(final byte[] b, final int off,
			final int len) throws IOException
		{
			if (dtdInserted) return false;
			final int found =
				off + new String(b, off, len).indexOf("<pluginRecords>");
			if (found < 0) return false;

			if (found > off) out.write(b, off, found - off);
			out.write(dtd.getBytes());
			out.write(b, found, len - found);
			dtdInserted = true;
			return true;
		}
	}
}
