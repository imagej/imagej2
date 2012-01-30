
package imagej.updater.core;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLFileErrorHandler implements ErrorHandler {

	@Override
	public void error(final SAXParseException e) throws SAXException {
		throwError(e);
	}

	@Override
	public void fatalError(final SAXParseException e) throws SAXException {
		throwError(e);
	}

	@Override
	public void warning(final SAXParseException e) throws SAXException {
		System.out.println("XML File Warning: " + e.getLocalizedMessage());
	}

	private void throwError(final SAXParseException e) {
		throw new Error(e.getLocalizedMessage() + "\n\nPublic ID: " +
			(e.getPublicId() == null ? "None" : e.getPublicId()) + ", System ID: " +
			(e.getPublicId() == null ? "None" : e.getPublicId()) +
			",\nLine number: " + e.getLineNumber() + ", Column number: " +
			e.getColumnNumber());
	}
}
