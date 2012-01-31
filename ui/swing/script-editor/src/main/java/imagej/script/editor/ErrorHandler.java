//
// ErrorHandler.java
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

package imagej.script.editor;

import imagej.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Position;

public class ErrorHandler {

	protected List<Error> list = new ArrayList<Error>();
	protected int current = -1;
	protected JTextArea textArea;
	protected int currentOffset;
	protected Parser parser;

	public ErrorHandler(final JTextArea textArea) {
		this.textArea = textArea;
	}

	public ErrorHandler(final ScriptEngineFactory language,
		final JTextArea textArea, final int startOffset)
	{
		this(textArea);
		final String name = language.getLanguageName();
		if (name.equals("Java") || name.equals("Fiji Build")) parser =
			new JavacErrorParser();
		else return;

		currentOffset = startOffset;

		try {
			parseErrors();
		}
		catch (final BadLocationException e) {
			Log.error(e);
		}
	}

	public int getErrorCount() {
		return list.size();
	}

	public boolean setCurrent(final int index) {
		if (index < 0 || index >= list.size()) return false;
		current = index;
		return true;
	}

	public boolean nextError(final boolean forward) {
		if (forward) {
			if (current + 1 >= list.size()) return false;
			current++;
		}
		else {
			if (current - 1 < 0) return false;
			current--;
		}
		return true;
	}

	public String getPath() {
		return list.get(current).path;
	}

	public int getLine() {
		return list.get(current).line;
	}

	public Position getPosition() {
		return list.get(current).position;
	}

	public void markLine() throws BadLocationException {
		final int offset = getPosition().getOffset();
		final int line = textArea.getLineOfOffset(offset);
		final int start = textArea.getLineStartOffset(line);
		final int end = textArea.getLineEndOffset(line);
		textArea.getHighlighter().removeAllHighlights();
		textArea.getHighlighter().addHighlight(start, end,
			DefaultHighlighter.DefaultPainter);
		scrollToVisible(start);
	}

	public void scrollToVisible(final int offset) throws BadLocationException {
		textArea.scrollRectToVisible(textArea.modelToView(textArea.getDocument()
			.getLength()));
		textArea.scrollRectToVisible(textArea.modelToView(offset));
	}

	static class Error {

		String path;
		int line;
		Position position;

		public Error(final String path, final int line) {
			this.path = path;
			this.line = line;
		}
	}

	public void addError(final String path, final int line, String text) {
		try {
			final Document document = textArea.getDocument();
			final int offset = document.getLength();
			if (!text.endsWith("\n")) text += "\n";
			textArea.insert(text, offset);
			if (path == null || line < 0) return;
			final Error error = new Error(path, line);
			error.position = document.createPosition(offset + 1);
			list.add(error);
		}
		catch (final BadLocationException e) {
			Log.error(e);
		}
	}

	interface Parser {

		Error getError(String line);
	}

	void parseErrors() throws BadLocationException {
		int line = textArea.getLineOfOffset(currentOffset);
		final int lineCount = textArea.getLineCount();
		for (;;) {
			if (++line >= lineCount) return;
			final int start = textArea.getLineStartOffset(line);
			final int end = textArea.getLineEndOffset(line);
			final String text = textArea.getText(start, end - start);
			final Error error = parser.getError(text);
			if (error != null) try {
				error.position = textArea.getDocument().createPosition(start);
				list.add(error);
			}
			catch (final BadLocationException e) {
				Log.error(e);
			}
		}
	}

	class JavacErrorParser implements Parser {

		@Override
		public Error getError(final String line) {
			int colon = line.indexOf(".java:");
			if (colon <= 0) return null;
			colon += 5;
			final int next = line.indexOf(':', colon + 1);
			if (next < colon + 2) return null;
			int lineNumber;
			try {
				lineNumber = Integer.parseInt(line.substring(colon + 1, next));
			}
			catch (final NumberFormatException e) {
				return null;
			}
			final String fileName = line.substring(0, colon);
			return new Error(fileName, lineNumber);
		}
	}
}
