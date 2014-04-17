/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.updater.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.scijava.util.LineOutputStream;
import org.scijava.util.ProcessUtils;

/**
 * A scroll pane that colorizes diff output.
 * 
 * It offers a {@link PrintStream} for use with
 * {@link ProcessUtils#exec(java.io.File, PrintStream, PrintStream, String...)}. It
 * can also show links that might update the view by calling an
 * {@link ActionListener}. Otherwise, this class is pretty dumb.
 * 
 * @author Johannes Schindelin
 */
public class DiffView extends JScrollPane {
	private static final long serialVersionUID = 1L;

	protected static final String ACTION_ATTRIBUTE = "ACTION";
	protected static final String FONT = "Courier";
	protected static final int FONT_SIZE = 12, BIG_FONT_SIZE = 15;

	protected JPanel panel;
	protected JTextPane textPane;
	protected Cursor normalCursor, handCursor;
	protected SimpleAttributeSet normal, bigBold, bold, italic, red, green;
	protected StyledDocument document;
	protected int adds, removes;
	protected boolean inHeader = true;

	/**
	 * Create a colorized diff view.
	 */
	public DiffView() {
		super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setPreferredSize(new Dimension(800, 600));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		getViewport().setView(panel);

		normal = getStyle(Color.black, false, false, FONT, FONT_SIZE);
		bigBold = getStyle(Color.blue, false, true, FONT, BIG_FONT_SIZE);
		bold = getStyle(Color.black, false, true, FONT, FONT_SIZE);
		italic = getStyle(Color.black, true, false, FONT, FONT_SIZE);
		red = getStyle(Color.red, false, false, FONT, FONT_SIZE);
		green = getStyle(new Color(0, 128, 32), false, false, FONT, FONT_SIZE);

		textPane = new JTextPane();
		normalCursor = textPane.getCursor();
		handCursor = new Cursor(Cursor.HAND_CURSOR);
		textPane.setEditable(false);
		document = textPane.getStyledDocument();
		panel.add(textPane);

		getVerticalScrollBar().setUnitIncrement(10);

		textPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				ActionListener action = getAction(event);
				if (action != null)
					action.actionPerformed(new ActionEvent(DiffView.this, 0, "action"));
			}
		});
	}

	private ActionListener getAction(final MouseEvent event) {
		Element e = document.getCharacterElement(textPane.viewToModel(event.getPoint()));
		ActionListener action = (ActionListener)e.getAttributes().getAttribute(ACTION_ATTRIBUTE);
		return action;
	}

	/**
	 * Create an attribute set for stylish text.
	 * 
	 * @param color
	 *            the color
	 * @param italic
	 *            whether the text should be slanted
	 * @param bold
	 *            whether the text should be bold
	 * @param fontName
	 *            the name of the font to use
	 * @param fontSize
	 *            the font size to use
	 * @return the attribute set
	 */
	public static SimpleAttributeSet getStyle(Color color, boolean italic,
			boolean bold, String fontName, int fontSize) {
		SimpleAttributeSet style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, color);
		StyleConstants.setItalic(style, italic);
		StyleConstants.setBold(style, bold);
		StyleConstants.setFontFamily(style, fontName);
		StyleConstants.setFontSize(style, fontSize);
		return style;
	}

	/**
	 * Add stylish text.
	 * 
	 * @param text
	 *            the text to add
	 * @param set
	 *            the formatting attributes
	 */
	public void styled(String text, AttributeSet set) {
		styled(document.getLength(), text, set);
	}

	/**
	 * Insert some stylish text.
	 * 
	 * @param position
	 *            the position where to insert the text
	 * @param text
	 *            the text to insert
	 * @param set
	 *            the formatting attributes
	 */
	public void styled(int position, String text, AttributeSet set) {
		try {
			document.insertString(position, text, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Add some plain text.
	 * 
	 * @param text
	 *            the text to add
	 */
	public void normal(String text) {
		styled(text, normal);
	}

	/**
	 * Add some warnings.
	 * 
	 * @param warning
	 *            the text to add
	 */
	public void warn(String warning) {
		red("Warning: ");
		styled(warning, italic);
	}

	/**
	 * Add some red text.
	 * 
	 * @param text
	 *            the text to add
	 */
	public void red(String text) {
		styled(text, red);
	}

	/**
	 * Add some green text.
	 * 
	 * @param text
	 *            the text to add
	 */
	public void green(String text) {
		styled(text, green);
	}

	/**
	 * Insert some red text.
	 * 
	 * @param text
	 *            the text to insert
	 */
	public void red(int position, String text) {
		styled(position, text, red);
	}

	/**
	 * Insert some green text.
	 * 
	 * @param text
	 *            the text to insert
	 */
	public void green(int position, String text) {
		styled(position, text, green);
	}

	/**
	 * Generate an attribute set for links.
	 * 
	 * @param action
	 *            the action to perform
	 * @return the attribute set
	 */
	public static SimpleAttributeSet getActionStyle(ActionListener action) {
		SimpleAttributeSet style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.blue);
		StyleConstants.setUnderline(style, true);
		StyleConstants.setFontFamily(style, FONT);
		StyleConstants.setFontSize(style, FONT_SIZE);
		style.addAttribute(ACTION_ATTRIBUTE, action);
		return style;
	}

	/**
	 * Add a link.
	 * 
	 * @param text
	 *            the label of the link
	 * @param action
	 *            the action to perform when the link is clicked
	 */
	public void link(String text, ActionListener action) {
		final JButton button = new JButton(text);
		button.addActionListener(action);
		textPane.insertComponent(button);
		button.setCursor(handCursor);
	}

	/**
	 * Get the number of added lines.
	 * 
	 * @return how many lines were added in total
	 */
	public int getAdds() {
		return adds;
	}

	/**
	 * Get the number of removed lines.
	 * 
	 * @return how many lines were removed in total
	 */
	public int getRemoves() {
		return removes;
	}

	/**
	 * Get the number of added or removed lines.
	 * 
	 * @return how many lines were added or removed in total
	 */
	public int getChanges() {
		return adds + removes;
	}

	/**
	 * Colorize one line of output.
	 * 
	 * @param line the line
	 */
	public void println(String line) {
		if (line.startsWith("diff")) {
			styled(line, bold);
			inHeader = false;
		}
		else if (line.startsWith(" "))
			styled(line, inHeader && line.startsWith("    ") ? bigBold : normal);
		else if (line.startsWith("+")) {
			adds++;
			styled(line, green);
		}
		else if (line.startsWith("-")) {
			removes++;
			styled(line, red);
		}
		else {
			if (line.startsWith("commit"))
				inHeader = true;
			styled(line, italic);
		}
		styled("\n", normal);
	}

	/**
	 * Construct a {@link PrintStream} adapter to this view.
	 * 
	 * @return the print stream
	 */
	public PrintStream getPrintStream() {
		final OutputStream out = new LineOutputStream() {
			@Override
			public void println(String line) {
				DiffView.this.println(line);
			}
		};
		return new PrintStream(out) {
			@Override
			public void println(String line) {
				DiffView.this.println(line);
			}
		};
	}

	/**
	 * Access the underlying document.
	 * 
	 * @return the document
	 */
	protected Document getDocument() {
		return document;
	}

	/**
	 * A main method for testing.
	 * 
	 * @param args the command line
	 */
	public static void main(String[] args) {
		final DiffView diff = new DiffView();
		final Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					ProcessUtils.exec(null, diff.getPrintStream(), diff.getPrintStream(), "git", "show");
				} catch (RuntimeException e) {
					if (!(e.getCause() instanceof InterruptedException))
						e.printStackTrace();
				}
			}
		};

		final JFrame frame = new JFrame("git show");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(640, 480);
		frame.getContentPane().add(diff);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thread.interrupt();
			}
		});
		frame.pack();
		frame.setVisible(true);

		thread.start();
	}
}
