//
// OMEShapeROI.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.roi.ome;

import imagej.util.Log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import ome.xml.model.Shape;
import ome.xml.model.enums.EnumerationException;

import org.w3c.dom.Document;

/**
 * @author leek
 *The OmeroShapeROI serves as the base for all Omero shape ROIs
 *and handles serialization, deserialization and presentation of
 *the represented class to the outside world.
 */
public class OMEShapeROI<T extends Shape> implements Externalizable {
	protected T omeShape;
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		try {
			//
			// Code largely borrowed from
			// http://www.coderanch.com/how-to/java/DocumentToString
			//
			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			omeShape.asXMLElement(d);
			DOMSource src = new DOMSource(d);
			StringWriter writer = new StringWriter();
			StreamResult dest = new StreamResult(writer);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
	        transformer.transform(src, dest);
	        out.writeUTF(writer.getBuffer().toString());
		} catch (ParserConfigurationException e) {
			Log.warn("Failed to create XML document", e);
			throw new IOException("Failed to create XML document");
		} catch (TransformerException e) {
			Log.warn("Failed to transform XML document", e);
			throw new IOException("Failed to transform XML document");
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		try {
			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			String xml = in.readUTF();
			StringReader reader = new StringReader(xml);
			StreamSource src = new StreamSource(reader);
			DOMResult dest = new DOMResult(d);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
	        transformer.transform(src, dest);
	        omeShape.update(d.getDocumentElement());
		} catch (TransformerConfigurationException e) {
			Log.warn("Failed to create XML document", e);
			throw new IOException("Failed to create XML transformer factory");
		} catch (TransformerException e) {
			Log.warn("Failed to parse embedded XML document", e);
			throw new IOException("Failed to parse the embedded Omero XML");
		} catch (EnumerationException e) {
			Log.warn("XML document did not contain a top-level element", e);
			throw new IOException("XML did not contain a top-level element");
		} catch (ParserConfigurationException e) {
			Log.warn("Misconfigured DOM", e);
			throw new IOException("Misconfigured DOM - could not create a DOM document");
		}
	}

	/**
	 * Access the underlying OME shape, for instance from JHotDraw
	 * @return the Omero shape that holds the ROI properties
	 */
	public T getOMEShape() {
		return omeShape;
	}

}
