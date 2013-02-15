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

package imagej.script.editor;


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Vector;

import javax.script.ScriptEngineFactory;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.GutterIconInfo;
import org.fife.ui.rtextarea.IconGroup;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RecordableTextAction;
import org.scijava.util.FileUtils;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
public class EditorPane extends RSyntaxTextArea implements DocumentListener {
	TextEditor frame;
	String fallBackBaseName;
	File file, gitDirectory;
	long fileLastModified;
	ScriptEngineFactory currentLanguage;
	Gutter gutter;
	IconGroup iconGroup;
	int modifyCount;
	boolean undoInProgress, redoInProgress;

	public EditorPane(TextEditor frame) {
		this.frame = frame;
		setLineWrap(false);
		setTabSize(8);
		getActionMap().put(DefaultEditorKit
				.nextWordAction, wordMovement(+1, false));
		getActionMap().put(DefaultEditorKit
				.selectionNextWordAction, wordMovement(+1, true));
		getActionMap().put(DefaultEditorKit
				.previousWordAction, wordMovement(-1, false));
		getActionMap().put(DefaultEditorKit
				.selectionPreviousWordAction, wordMovement(-1, true));
		ToolTipManager.sharedInstance().registerComponent(this);
		getDocument().addDocumentListener(this);
	}

	public void setTabSize(int width) {
		if (getTabSize() != width)
			super.setTabSize(width);
	}

	public void embedWithScrollbars(Container container) {
		container.add(embedWithScrollbars());
	}

	public RTextScrollPane embedWithScrollbars() {
		RTextScrollPane sp = new RTextScrollPane(this);
		sp.setPreferredSize(new Dimension(600, 350));
		sp.setIconRowHeaderEnabled(true);
		gutter = sp.getGutter();
		iconGroup = new IconGroup("bullets", "images/", null, "png", null);
		gutter.setBookmarkIcon(iconGroup.getIcon("var"));
		gutter.setBookmarkingEnabled(true);
		return sp;
	}

	RecordableTextAction wordMovement(final int direction,
			final boolean select) {
		final String id = "WORD_MOVEMENT_" + select + direction;
		return new RecordableTextAction(id) {
			public void actionPerformedImpl(ActionEvent e,
					RTextArea textArea) {
				int pos = textArea.getCaretPosition();
				int end = direction < 0 ? 0 :
					textArea.getDocument().getLength();
				while (pos != end && !isWordChar(textArea, pos))
					pos += direction;
				while (pos != end && isWordChar(textArea, pos))
					pos += direction;
				if (select)
					textArea.moveCaretPosition(pos);
				else
					textArea.setCaretPosition(pos);
			}

			public String getMacroID() {
				return id;
			}

			boolean isWordChar(RTextArea textArea, int pos) {
				try {
					char c = textArea.getText(pos
						+ (direction < 0 ? -1 : 0), 1)
						.charAt(0);
					return c > 0x7f ||
						(c >= 'A' && c <= 'Z') ||
						(c >= 'a' && c <= 'z') ||
						(c >= '0' && c <= '9') ||
						c == '_';
				} catch (BadLocationException e) {
					return false;
				}
			}
		};
	}

	public void undoLastAction() {
		undoInProgress = true;
		super.undoLastAction();
		undoInProgress = false;
	}

	public void redoLastAction() {
		redoInProgress = true;
		super.redoLastAction();
		redoInProgress = false;
	}

	public boolean fileChanged() {
		return modifyCount != 0;
	}

	public void insertUpdate(DocumentEvent e) {
		modified();
	}

	public void removeUpdate(DocumentEvent e) {
		modified();
	}

	// triggered only by syntax highlighting
	public void changedUpdate(DocumentEvent e) { }

	protected void modified() {
		checkForOutsideChanges();
		boolean update = modifyCount == 0;
		if (undoInProgress)
			modifyCount--;
		else if (redoInProgress || modifyCount >= 0)
			modifyCount++;
		else // not possible to get back to clean state
			modifyCount = Integer.MIN_VALUE;
		if (update || modifyCount == 0)
			setTitle();
	}

	public boolean isNew() {
		return !fileChanged() && file == null &&
			fallBackBaseName == null &&
			getDocument().getLength() == 0;
	}

	public void checkForOutsideChanges() {
		if (frame != null && wasChangedOutside() &&
				!frame.reload("The file " + file.getName()
					+ " was changed outside of the editor"))
			fileLastModified = file.lastModified();
	}

	public boolean wasChangedOutside() {
		return file != null && file.exists() &&
				file.lastModified() != fileLastModified;
	}

	public void write(File file) throws IOException {
		BufferedWriter outFile =
			new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		outFile.write(getText());
		outFile.close();
		modifyCount = 0;
		fileLastModified = file.lastModified();
	}

	public void open(final File file) throws IOException {
		final File oldFile = this.file;
		this.file = null;
		if (file == null) setText("");
		else {
			int line = 0;
			try {
				if (file.getCanonicalPath().equals(oldFile.getCanonicalPath()))
					line = getCaretLineNumber();
			} catch (Exception e) { /* ignore */ }
			if (!file.exists()) {
				modifyCount = Integer.MIN_VALUE;
				setFileName(file);
				return;
			}
			StringBuffer string = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			char[] buffer = new char[16384];
			for (;;) {
				int count = reader.read(buffer);
				if (count < 0)
					break;
				string.append(buffer, 0, count);
			}
			reader.close();
			setText(string.toString());
			this.file = file;
			if (line > getLineCount())
				line = getLineCount() - 1;
			try {
				setCaretPosition(getLineStartOffset(line));
			} catch (BadLocationException e) { /* ignore */ }
		}
		discardAllEdits();
		modifyCount = 0;
		fileLastModified = file == null || !file.exists() ? 0 :
			file.lastModified();
	}

	public void setFileName(String baseName) {
		String name = baseName;
		for (String extension : currentLanguage.getExtensions()) {
			extension = "." + extension;
			if (baseName.endsWith(extension)) {
				name = name.substring(0, name.length()
						- extension.length());
				break;
			}
		}
		fallBackBaseName = name;
		if (currentLanguage.getLanguageName().equals("Java"))
			new TokenFunctions(this).setClassName(name);
	}

	public void setFileName(final File file) {
		this.file = file;
		updateGitDirectory();
		setTitle();
		if (file != null) {
			SwingUtilities.invokeLater(new Thread() {
				public void run() {
					setLanguageByFileName(file.getName());
				}
			});
			fallBackBaseName = null;
		}
		fileLastModified = file == null || !file.exists() ? 0 :
			file.lastModified();
	}

	protected void updateGitDirectory() {
		gitDirectory = new FileFunctions(frame).getGitDirectory(file);
	}

	public File getGitDirectory() {
		return gitDirectory;
	}

	protected String getFileName() {
		if (file != null)
			return file.getName();
		String extension = "";
		if (currentLanguage != null) {
			List<String> extensions = currentLanguage.getExtensions();
			if (extensions.size() > 0) {
				extension = "." + extensions.get(0);
			}
			if (currentLanguage.getLanguageName().equals("Java")) {
				String name =
					new TokenFunctions(this).getClassName();
				if (name != null) {
					return name + extension;
				}
			}
		}
		return (fallBackBaseName == null ? "New_" : fallBackBaseName)
			+ extension;
	}

	private synchronized void setTitle() {
		if (frame != null)
			frame.setTitle();
	}

	protected void setLanguageByFileName(String name) {
		setLanguage(frame.scriptService.getByFileExtension(FileUtils.getExtension(name)));
	}

	protected void setLanguage(ScriptEngineFactory language) {
		String languageName;
		String defaultExtension;
		if (language == null) {
			languageName = "None";
			defaultExtension = ".txt";
		} else {
			languageName = language.getLanguageName();
			List<String> extensions = language.getExtensions();
			defaultExtension = extensions.size() == 0 ? "" : ("." + extensions.get(0));
		}
		if (fallBackBaseName != null && fallBackBaseName.endsWith(".txt"))
			fallBackBaseName = fallBackBaseName.substring(0,
				fallBackBaseName.length() - 4);
		if (file != null) {
			String name = file.getName();
			String ext = "." + FileUtils.getExtension(name);
			if (!defaultExtension.equals(ext)) {
				name = name.substring(0, name.length() - ext.length());
				file = new File(file.getParentFile(), name + defaultExtension);
				updateGitDirectory();
				modifyCount = Integer.MIN_VALUE;
			}
		}
		currentLanguage = language;

		final String styleName = "text/" + languageName.toLowerCase().replace(' ', '-');
		setSyntaxEditingStyle(styleName);

		frame.setTitle();
		frame.updateLanguageMenu(language);
	}

	public float getFontSize() {
		return getFont().getSize2D();
	}

	public void setFontSize(float size) {
		increaseFontSize(size / getFontSize());
	}

	public void increaseFontSize(float factor) {
		if (factor == 1)
			return;
		SyntaxScheme scheme = getSyntaxScheme();
		for (int i = 0; i < scheme.getStyleCount(); i++) {
			Style style = scheme.getStyle(i);
			if (style == null || style.font == null)
				continue;
			float size = (float)Math.max(5, style.font.getSize2D() * factor);
			style.font = style.font.deriveFont(size);
		}
		Font font = getFont();
		float size = (float)Math.max(5, font.getSize2D() * factor);
		setFont(font.deriveFont(size));
		setSyntaxScheme(scheme);
		Component parent = getParent();
		if (parent instanceof JViewport) {
			parent = parent.getParent();
			if (parent instanceof JScrollPane) {
				parent.repaint();
			}
		}
		parent.repaint();
	}

	protected RSyntaxDocument getRSyntaxDocument() {
		return (RSyntaxDocument)getDocument();
	}

	public void toggleBookmark() {
		toggleBookmark(getCaretLineNumber());
	}

	public void toggleBookmark(int line) {
		if (gutter != null) try {
			gutter.toggleBookmark(line);
		} catch (BadLocationException e) { /* ignore */ }
	}

	public class Bookmark {
		int tab;
		GutterIconInfo info;

		public Bookmark(int tab, GutterIconInfo info) {
			this.tab = tab;
			this.info = info;
		}

		public int getLineNumber() {
			try {
				return getLineOfOffset(info.getMarkedOffset());
			} catch (BadLocationException e) {
				return -1;
			}
		}

		public void setCaret() {
			frame.switchTo(tab);
			setCaretPosition(info.getMarkedOffset());
		}

		public String toString() {
			return "Line " + (getLineNumber() + 1) + " (" + getFileName() + ")";
		}
	}

	public void getBookmarks(int tab, Vector<Bookmark> result) {
		if (gutter == null)
			return;
		for (GutterIconInfo info : gutter.getBookmarks())
			result.add(new Bookmark(tab, info));
	}

	/** Adapted from ij.plugin.frame.Editor */
	public int zapGremlins() {
		final char[] chars = getText().toCharArray();
		int count=0;
		boolean inQuotes = false;
		char quoteChar = 0;
		for (int i=0; i<chars.length; i++) {
			char c = chars[i];
			if (!inQuotes && (c=='"' || c=='\'')) {
				inQuotes = true;
				quoteChar = c;
			} else  {
				if (inQuotes && (c==quoteChar || c=='\n'))
				inQuotes = false;
			}
			if (!inQuotes && c!='\n' && c!='\t' && (c<32||c>127)) {
				count++;
				chars[i] = ' ';
			}
		}
		if (count>0) {
			beginAtomicEdit();
			try {
				setText(new String(chars));
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				endAtomicEdit();
			}
		}
		return count;
	}

	public void convertTabsToSpaces() {
		beginAtomicEdit();
		try {
			super.convertTabsToSpaces();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			endAtomicEdit();
		}
	}
	public void convertSpacesToTabs() {
		beginAtomicEdit();
		try {
			super.convertSpacesToTabs();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			endAtomicEdit();
		}
	}
}
