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

package imagej.updater.gui;

import imagej.updater.core.FileObject;
import imagej.updater.core.FilesCollection;
import imagej.updater.util.UpdaterUserInterface;
import imagej.util.Log;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JTextPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

@SuppressWarnings("serial")
public class FileDetails extends JTextPane implements UndoableEditListener {

	private final static AttributeSet bold, italic, normal, title;
	private final static Cursor hand, defaultCursor;
	private final static String LINK_ATTRIBUTE = "URL";
	SortedMap<Position, EditableRegion> editables;
	Position dummySpace;
	UpdaterFrame updaterFrame;

	static {
		italic = getStyle(Color.black, true, false, "Verdana", 12);
		bold = getStyle(Color.black, false, true, "Verdana", 12);
		normal = getStyle(Color.black, false, false, "Verdana", 12);
		title = getStyle(Color.black, false, false, "Impact", 18);

		hand = new Cursor(Cursor.HAND_CURSOR);
		defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	}

	public FileDetails(final UpdaterFrame updaterFrame) {
		this.updaterFrame = updaterFrame;
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final String url = getLinkAt(e.getPoint());
				try {
					if (url != null) UpdaterUserInterface.get().openURL(url);
				}
				catch (final Exception exception) {
					Log.error(exception);
					UpdaterUserInterface.get().error(
						"Could not open " + url + ": " + exception.getMessage());
				}
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(final MouseEvent e) {
				setCursor(e.getPoint());
			}
		});

		reset();
		// TODO: enable depending on the update site
		if (updaterFrame.files.hasUploadableSites()) getDocument()
			.addUndoableEditListener(this);
	}

	public void reset() {
		setEditable(false);
		setText("");
		final Comparator<Position> comparator = new Comparator<Position>() {

			@Override
			public int compare(final Position p1, final Position p2) {
				return p1.getOffset() - p2.getOffset();
			}
		};

		editables = new TreeMap<Position, EditableRegion>(comparator);
		dummySpace = null;
	}

	public void setEditableForDevelopers() {
		removeDummySpace();
		setEditable(true);
	}

	private String getLinkAt(final Point p) {
		final StyledDocument document = getStyledDocument();
		final Element e = document.getCharacterElement(viewToModel(p));
		return (String) e.getAttributes().getAttribute(LINK_ATTRIBUTE);
	}

	protected void setCursor(final Point p) {
		setCursor(getLinkAt(p) == null ? defaultCursor : hand);
	}

	private AttributeSet getLinkAttribute(final String url) {
		// TODO: Verdana? Java is platform-independent, if this introduces a
		// platform dependency, it needs to be thrown out, quickly!
		final SimpleAttributeSet style =
			getStyle(Color.blue, false, false, "Verdana", 12);
		style.addAttribute(LINK_ATTRIBUTE, url);
		return style;
	}

	public static SimpleAttributeSet getStyle(final Color color,
		final boolean italic, final boolean bold, final String fontName,
		final int fontSize)
	{
		final SimpleAttributeSet style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, color);
		StyleConstants.setItalic(style, italic);
		StyleConstants.setBold(style, bold);
		StyleConstants.setFontFamily(style, fontName);
		StyleConstants.setFontSize(style, fontSize);
		return style;
	}

	public void styled(final String text, final AttributeSet set) {
		final Document document = getDocument();
		try {
			document.insertString(document.getLength(), text, set);
		}
		catch (final BadLocationException e) {
			Log.error(e);
			throw new RuntimeException(e);
		}
	}

	public void link(final String url) {
		styled(url, getLinkAttribute(url));
	}

	public void italic(final String text) {
		styled(text, italic);
	}

	public void bold(final String text) {
		styled(text, bold);
	}

	public void normal(final String text) {
		styled(text, normal);
	}

	public void title(final String text) {
		styled(text, title);
	}

	public void description(final String description, final FileObject file) {
		if (!updaterFrame.files.hasUploadableSites() &&
			(description == null || description.trim().equals(""))) return;
		blankLine();
		bold("Description:\n");
		final int offset = getCaretPosition();
		normal(description);
		addEditableRegion(offset, "Description", file);
	}

	public void executable(final FileObject file) {
		if (!updaterFrame.files.hasUploadableSites() && !file.executable) return;
		blankLine();
		bold("Executable:\n");
		final int offset = getCaretPosition();
		normal(file.executable ? "true" : "false");
		addEditableRegion(offset, "Executable", file);
	}

	public void list(String label, final boolean showLinks,
		final Iterable<?> items, final String delim, final FileObject file)
	{
		final List<Object> list = new ArrayList<Object>();
		for (final Object object : items)
			list.add(object);

		if (!updaterFrame.files.hasUploadableSites() && list.size() == 0) return;

		blankLine();
		final String tag = label;
		if (list.size() > 1 && label.endsWith("y")) label =
			label.substring(0, label.length() - 1) + "ie";
		bold(label + (list.size() > 1 ? "s" : "") + ":\n");
		final int offset = getCaretPosition();
		String delimiter = "";
		for (final Object object : list) {
			normal(delimiter);
			delimiter = delim;
			if (showLinks) link(object.toString());
			else normal(object.toString());
		}
		addEditableRegion(offset, tag, file);
	}

	public void blankLine() {
		final int offset = getCaretPosition();
		try {
			if (offset > 1 && getText(offset - 2, 2).equals("\n ")) {
				normal("\n");
				return;
			}
		}
		catch (final BadLocationException e) {
			Log.error(e);
		}
		normal("\n\n");
	}

	final String[] months = { "Zero", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
		"Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	String prettyPrintTimestamp(final long timestamp) {
		final String t = "" + timestamp;
		return t.substring(6, 8) + " " +
			months[Integer.parseInt(t.substring(4, 6))] + " " + t.substring(0, 4);
	}

	public void showFileDetails(final FileObject file) {
		if (!getText().equals("")) blankLine();
		title(file.getFilename());
		if (file.isUpdateable()) italic("\n(Update available)");
		else if (file.isLocalOnly()) italic("(Local-only)");
		if (file.isLocallyModified()) {
			blankLine();
			bold("Warning: ");
			italic("This file was locally modified.");
		}
		blankLine();
		if (file.current == null) bold("This file is no longer needed");
		else {
			bold("Release date:\n");
			normal(prettyPrintTimestamp(file.current.timestamp));
		}
		description(file.getDescription(), file);
		list("Author", false, file.getAuthors(), ", ", file);
		if (updaterFrame.files.hasUploadableSites()) list("Platform", false, file
			.getPlatforms(), ", ", file);
		list("Category", false, file.getCategories(), ", ", file);
		list("Link", true, file.getLinks(), "\n", file);
		list("Dependency", false, file.getDependencies(), ",\n", file);
		if (file.executable) executable(file);
		if (file.updateSite != null &&
			!file.updateSite.equals(FilesCollection.DEFAULT_UPDATE_SITE))
		{
			blankLine();
			bold("Update site:\n");
			normal(file.updateSite);
		}

		// scroll to top
		scrollRectToVisible(new Rectangle(0, 0, 1, 1));
	}

	class EditableRegion implements Comparable<EditableRegion> {

		FileObject file;
		String tag;
		Position start, end;

		public EditableRegion(final FileObject file, final String tag,
			final Position start, final Position end)
		{
			this.file = file;
			this.tag = tag;
			this.start = start;
			this.end = end;
		}

		@Override
		public int compareTo(final EditableRegion other) {
			return start.getOffset() - other.start.getOffset();
		}

		@Override
		public String toString() {
			return "EditableRegion(" + tag + ":" + start.getOffset() + "-" +
				(end == null ? "null" : end.getOffset()) + ")";
		}
	}

	void addEditableRegion(final int startOffset, final String tag,
		final FileObject file)
	{
		final int endOffset = getCaretPosition();
		try {
			// make sure end position does not move further
			normal(" ");
			Position start, end;
			start = getDocument().createPosition(startOffset - 1);
			end = getDocument().createPosition(endOffset);

			editables.put(start, new EditableRegion(file, tag, start, end));
			removeDummySpace();
			dummySpace = end;
		}
		catch (final BadLocationException e) {
			Log.error(e);
		}
	}

	void removeDummySpace() {
		if (dummySpace != null) try {
			getDocument().remove(dummySpace.getOffset(), 1);
			dummySpace = null;
		}
		catch (final BadLocationException e) {
			Log.error(e);
		}
	}

	boolean handleEdit() {
		EditableRegion editable;
		try {
			final int offset = getCaretPosition();
			final Position current = getDocument().createPosition(offset);
			final Position last = editables.headMap(current).lastKey();
			editable = editables.get(last);
			if (offset > editable.start.getOffset() &&
				offset > editable.end.getOffset()) return false;
		}
		catch (final NoSuchElementException e) {
			return false;
		}
		catch (final BadLocationException e) {
			return false;
		}

		final int start = editable.start.getOffset() + 1;
		final int end = editable.end.getOffset();

		String text;
		try {
			text = getDocument().getText(start, end + 1 - start);
		}
		catch (final BadLocationException e) {
			return false;
		}

		editable.file.metadataChanged = true;
		if (editable.tag.equals("Description")) {
			editable.file.description = text.trim();
			return true;
		}
		else if (editable.tag.equals("Executable")) {
			editable.file.executable = "true".equalsIgnoreCase(text.trim());
			return true;
		}
		final String[] list = text.split(editable.tag.equals("Link") ? "\n" : ",");
		editable.file.replaceList(editable.tag, list);
		return true;
	}

	// Do not process key events when on bold parts of the text
	// or when not developer
	@Override
	public void undoableEditHappened(final UndoableEditEvent e) {
		if (isEditable()) {
			if (!handleEdit()) e.getEdit().undo();
			else updaterFrame.markUploadable();
		}
	}
}
