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

import fiji.scripting.Languages;
import fiji.scripting.Languages.Language;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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

	public ErrorHandler(JTextArea textArea) {
		this.textArea = textArea;
	}

	public ErrorHandler(Language language, JTextArea textArea,
			int startOffset) {
		this(textArea);
		if (language.menuLabel.equals("Java") || language == Languages.fakefile)
			parser = new JavacErrorParser();
		else
			return;

		currentOffset = startOffset;

		try {
			parseErrors();
		} catch (BadLocationException e) {
			handleException(e);
		}
	}

	public int getErrorCount() {
		return list.size();
	}

	public boolean setCurrent(int index) {
		if (index < 0 || index >= list.size())
			return false;
		current = index;
		return true;
	}

	public boolean nextError(boolean forward) {
		if (forward) {
			if (current + 1 >= list.size())
				return false;
			current++;
		}
		else {
			if (current - 1 < 0)
				return false;
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
		int offset = getPosition().getOffset();
		int line = textArea.getLineOfOffset(offset);
		int start = textArea.getLineStartOffset(line);
		int end = textArea.getLineEndOffset(line);
		textArea.getHighlighter().removeAllHighlights();
		textArea.getHighlighter().addHighlight(start, end,
			DefaultHighlighter.DefaultPainter);
		scrollToVisible(start);
	}

	public void scrollToVisible(int offset) throws BadLocationException {
		textArea.scrollRectToVisible(textArea.modelToView(textArea.getDocument().getLength()));
		textArea.scrollRectToVisible(textArea.modelToView(offset));
	}

	static class Error {
		String path;
		int line;
		Position position;

		public Error(String path, int line) {
			this.path = path;
			this.line = line;
		}
	}

	public void addError(String path, int line, String text) {
		try {
			Document document = textArea.getDocument();
			int offset = document.getLength();
			if (!text.endsWith("\n"))
				text += "\n";
			textArea.insert(text, offset);
			if (path == null || line < 0)
				return;
			Error error = new Error(path, line);
			error.position = document.createPosition(offset + 1);
			list.add(error);
		} catch (BadLocationException e) {
			handleException(e);
		}
	}

	interface Parser {
		Error getError(String line);
	}

	void parseErrors() throws BadLocationException {
		int line = textArea.getLineOfOffset(currentOffset);
		int lineCount = textArea.getLineCount();
		for (;;) {
			if (++line >= lineCount)
				return;
			int start = textArea.getLineStartOffset(line);
			int end = textArea.getLineEndOffset(line);
			String text = textArea.getText(start, end - start);
			Error error = parser.getError(text);
			if (error != null) try {
				error.position = textArea.getDocument()
					.createPosition(start);
				list.add(error);
			} catch (BadLocationException e) {
				handleException(e);
			}
		}
	}

	class JavacErrorParser implements Parser {
		public Error getError(String line) {
			int colon = line.indexOf(".java:");
			if (colon <= 0)
				return null;
			colon += 5;
			int next = line.indexOf(':', colon + 1);
			if (next < colon + 2)
				return null;
			int lineNumber;
			try {
				lineNumber = Integer.parseInt(line
					.substring(colon + 1, next));
			} catch (NumberFormatException e) {
				return null;
			}
			String fileName = line.substring(0, colon);
			return new Error(fileName, lineNumber);
		}
	}

	private void handleException(final Throwable e) {
		TextEditor.handleException(e, textArea);
	}
}
