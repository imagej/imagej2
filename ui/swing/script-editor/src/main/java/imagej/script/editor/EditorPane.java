//
// EditorPane.java
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

import imagej.util.FileUtils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class EditorPane implements DocumentListener {

	protected JTextArea textArea;
	protected EditorFrame frame;
	protected String fallBackBaseName;
	protected File file, gitDirectory;
	protected long fileLastModified;
	protected ScriptEngineFactory currentLanguage;
	protected int modifyCount;
	protected boolean undoInProgress, redoInProgress;

	public EditorPane(final EditorFrame frame) {
		this.frame = frame;
		textArea = new JTextArea();
		setTabSize(8);
		textArea.getDocument().addDocumentListener(this);
		currentLanguage = null;
	}

	public void setTabSize(final int width) {
		if (textArea.getTabSize() != width) textArea.setTabSize(width);
	}

	public void embedWithScrollbars(final Container container) {
		container.add(embedWithScrollbars());
	}

	public JScrollPane embedWithScrollbars() {
		final JScrollPane sp = new JScrollPane(textArea);
		sp.setPreferredSize(new Dimension(600, 350));
		return sp;
	}

	public boolean fileChanged() {
		return modifyCount != 0;
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		modified();
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		modified();
	}

	// triggered only by syntax highlighting
	@Override
	public void changedUpdate(final DocumentEvent e) {}

	protected void modified() {
		checkForOutsideChanges();
		final boolean update = modifyCount == 0;
		if (undoInProgress) modifyCount--;
		else if (redoInProgress || modifyCount >= 0) modifyCount++;
		else // not possible to get back to clean state
		modifyCount = Integer.MIN_VALUE;
		if (update || modifyCount == 0) setTitle();
	}

	public boolean isNew() {
		return !fileChanged() && file == null && fallBackBaseName == null &&
			textArea.getDocument().getLength() == 0;
	}

	public void checkForOutsideChanges() {
		if (frame != null &&
			wasChangedOutside() &&
			!frame.reload("The file " + file.getName() +
				" was changed outside of the editor")) fileLastModified =
			file.lastModified();
	}

	public boolean wasChangedOutside() {
		return file != null && file.exists() &&
			file.lastModified() != fileLastModified;
	}

	public void write(final File file) throws IOException {
		final BufferedWriter outFile =
			new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
				"UTF-8"));
		outFile.write(textArea.getText());
		outFile.close();
		modifyCount = 0;
		fileLastModified = file.lastModified();
	}

	public void open(final File file) throws IOException {
		final File oldFile = this.file;
		this.file = null;
		if (file == null) textArea.setText("");
		else {
			int line = 0;
			try {
				if (file.getCanonicalPath().equals(oldFile.getCanonicalPath())) line =
					getCaretLineNumber();
			}
			catch (final Exception e) { /* ignore */}
			if (!file.exists()) {
				modifyCount = Integer.MIN_VALUE;
				setFileName(file);
				return;
			}
			final StringBuffer string = new StringBuffer();
			final BufferedReader reader =
				new BufferedReader(new InputStreamReader(new FileInputStream(file),
					"UTF-8"));
			final char[] buffer = new char[16384];
			for (;;) {
				final int count = reader.read(buffer);
				if (count < 0) break;
				string.append(buffer, 0, count);
			}
			reader.close();
			textArea.setText(string.toString());
			this.file = file;
			if (line > textArea.getLineCount()) line = textArea.getLineCount() - 1;
			try {
				textArea.setCaretPosition(textArea.getLineStartOffset(line));
			}
			catch (final BadLocationException e) { /* ignore */}
		}
		modifyCount = 0;
		fileLastModified = file == null || !file.exists() ? 0 : file.lastModified();
	}

	private int getCaretLineNumber() {
		// TODO Auto-generated method stub
		throw new RuntimeException("TODO");
	}

	public void setFileName(final String baseName) {
		/* TODO! move part of this to EditorFrame
		String name = baseName;
		if (baseName.endsWith(currentLanguage.extension)) name =
			name.substring(0, name.length() - currentLanguage.extension.length());
		fallBackBaseName = name;
		if (currentLanguage.extension.equals(".java")) new TokenFunctions(this)
			.setClassName(name);
		*/
	}

	public void setFileName(final File file) {
		this.file = file;
		updateLanguage();
		updateGitDirectory();
		frame.setTitle();
		if (file != null) fallBackBaseName = null;
		fileLastModified = file == null || !file.exists() ? 0 : file.lastModified();
	}

	protected void updateLanguage() {
		final String fileExtension = FileUtils.getExtension(file);
		final ScriptEngineFactory language =
			frame.scriptService.getByFileExtension(fileExtension);
		setLanguage(language);
	}

	protected void setLanguage(final ScriptEngineFactory language) {
		if (currentLanguage == language) return;
		currentLanguage = language;
		frame.updateLanguageMenu(language);
		/* TODO!
		if (fallBackBaseName != null && fallBackBaseName.endsWith(".txt")) fallBackBaseName =
			fallBackBaseName.substring(0, fallBackBaseName.length() - 4);
		if (file != null) {
			String name = file.getName();
			final String fileExtension = FileUtils.getExtension(file);
			if (!name.endsWith(fileExtension) && currentLanguage != null) {
				final String ext = currentLanguage.extension;
				if (name.endsWith(ext)) name =
					name.substring(0, name.length() - ext.length());
				else if (name.endsWith(".txt")) name =
					name.substring(0, name.length() - 4);
				file = new File(file.getParentFile(), name + language.extension);
				updateGitDirectory();
				modifyCount = Integer.MIN_VALUE;
			}
		}
		*/
	}

	protected void updateGitDirectory() {
		gitDirectory = new FileFunctions(frame).getGitDirectory(file);
	}

	public File getGitDirectory() {
		return gitDirectory;
	}

	protected String getFileName() {
		if (file != null) return file.getName();
		final List<String> extensions = currentLanguage.getExtensions();
		return (fallBackBaseName == null ? "New_" : fallBackBaseName) +
			(extensions.size() > 0 ? extensions.get(0) : "");
	}

	private synchronized void setTitle() {
		if (frame != null) frame.setTitle();
	}

	public static String getExtension(final String fileName) {
		final int dot = fileName.lastIndexOf(".");
		return dot < 0 ? "" : fileName.substring(dot);
	}

	/* TODO! move to EditorFrame
	protected void setLanguageByFileName(final String name) {
		setLanguage(Languages.get(getExtension(name)));
	}
	*/

	public float getFontSize() {
		return textArea.getFont().getSize2D();
	}

	public void setFontSize(final float size) {
		increaseFontSize(size / getFontSize());
	}

	public void increaseFontSize(final float factor) {
		if (factor == 1) return;
		final Font font = textArea.getFont();
		final float size = Math.max(5, font.getSize2D() * factor);
		textArea.setFont(font.deriveFont(size));
		Component parent = textArea.getParent();
		if (parent instanceof JViewport) {
			parent = parent.getParent();
			if (parent instanceof JScrollPane) {
				parent.repaint();
			}
		}
		parent.repaint();
	}

	public void toggleBookmark() {
		toggleBookmark(getCaretLineNumber());
	}

	public void toggleBookmark(final int line) {
		throw new UnsupportedOperationException();
	}

	// TODO: make atomic replace an API function
	/** Adapted from ij.plugin.frame.Editor */
	public int zapGremlins() {
		final char[] chars = textArea.getText().toCharArray();
		int count = 0;
		boolean inQuotes = false;
		char quoteChar = 0;
		for (int i = 0; i < chars.length; i++) {
			final char c = chars[i];
			if (!inQuotes && (c == '"' || c == '\'')) {
				inQuotes = true;
				quoteChar = c;
			}
			else {
				if (inQuotes && (c == quoteChar || c == '\n')) inQuotes = false;
			}
			if (!inQuotes && c != '\n' && c != '\t' && (c < 32 || c > 127)) {
				count++;
				chars[i] = ' ';
			}
		}
		if (count > 0) {
			beginAtomicEdit();
			try {
				textArea.setText(new String(chars));
			}
			catch (final Throwable t) {
				t.printStackTrace();
			}
			finally {
				endAtomicEdit();
			}
		}
		return count;
	}

	private void endAtomicEdit() {
		// TODO Auto-generated method stub
		throw new RuntimeException("TODO");
	}

	private void beginAtomicEdit() {
		// TODO Auto-generated method stub
		throw new RuntimeException("TODO");
	}

	public void convertTabsToSpaces() {
		beginAtomicEdit();
		try {
			// TODO! convertTabsToSpaces();
		}
		catch (final Throwable t) {
			t.printStackTrace();
		}
		finally {
			endAtomicEdit();
		}
	}

	public void convertSpacesToTabs() {
		beginAtomicEdit();
		try {
			// TODO! convertSpacesToTabs();
		}
		catch (final Throwable t) {
			t.printStackTrace();
		}
		finally {
			endAtomicEdit();
		}
	}

	public void gotoLine(final int line) {
		// TODO Auto-generated method stub
		throw new RuntimeException("TODO");
	}

	public void requestFocus() {
		textArea.requestFocus();
	}

}
