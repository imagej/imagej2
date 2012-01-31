package fiji.scripting;

import fiji.SimpleExecuter;

import fiji.SimpleExecuter.LineHandler;

import ij.IJ;

import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class DiffView extends JScrollPane implements LineHandler {
	protected JPanel panel;
	protected SimpleAttributeSet normal, bigBold, bold, italic, red, green;
	protected Document document;
	protected int adds, removes;
	protected boolean inHeader = true;
	protected final static String ACTION_ATTRIBUTE = "ACTION";
	protected final static String font = "Courier";
	protected final static int fontSize = 12, bigFontSize = 15;

	public DiffView() {
		super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		getViewport().setView(panel);

		normal = getStyle(Color.black, false, false, font, fontSize);
		bigBold = getStyle(Color.blue, false, true, font, bigFontSize);
		bold = getStyle(Color.black, false, true, font, fontSize);
		italic = getStyle(Color.black, true, false, font, fontSize);
		red = getStyle(Color.red, false, false, font, fontSize);
		green = getStyle(new Color(0, 128, 32), false, false, font, fontSize);

		final JTextPane current = new JTextPane();
		current.setEditable(false);
		document = current.getDocument();
		panel.add(current);

		getVerticalScrollBar().setUnitIncrement(10);

		current.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				StyledDocument document = current.getStyledDocument();
				Element e = document.getCharacterElement(current.viewToModel(event.getPoint()));
				ActionListener action = (ActionListener)e.getAttributes().getAttribute(ACTION_ATTRIBUTE);
				if (action != null)
					action.actionPerformed(new ActionEvent(DiffView.this, 0, "action"));
			}
		});
	}

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

	public void styled(String text, AttributeSet set) {
		styled(document.getLength(), text, set);
	}

	public void styled(int position, String text, AttributeSet set) {
		try {
			document.insertString(position, text, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void normal(String text) {
		styled(text, normal);
	}

	public void red(String text) {
		styled(text, red);
	}

	public void green(String text) {
		styled(text, green);
	}

	public void red(int position, String text) {
		styled(position, text, red);
	}

	public void green(int position, String text) {
		styled(position, text, green);
	}

	public static SimpleAttributeSet getActionStyle(ActionListener action) {
		SimpleAttributeSet style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.blue);
		StyleConstants.setUnderline(style, true);
		StyleConstants.setFontFamily(style, font);
		StyleConstants.setFontSize(style, fontSize);
		style.addAttribute(ACTION_ATTRIBUTE, action);
		return style;
	}

	public void link(String text, ActionListener action) {
		styled(text, getActionStyle(action));
	}

	public void handleLine(String line) {
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

	public int getAdds() {
		return adds;
	}

	public int getRemoves() {
		return removes;
	}

	public int getChanges() {
		return adds + removes;
	}

	public static class IJLog implements LineHandler {
		public void handleLine(String line) {
			IJ.log(line);
		}
	}

	public class DiffOutputStream extends OutputStream {
		@Override
		public final void write(int i) {
			write(Character.toString((char)i));
		}

		@Override
		public final void write(byte[] buffer) {
			write(new String(buffer));
		}

		@Override
		public final void write(byte[] buffer, int off, int len) {
			write(new String(buffer, off, len));
		}

		public final void write(final String string) {
			normal(string);
		}
	}

	public OutputStream getOutputStream() {
		return new DiffOutputStream();
	}

	public static void main(String[] args) {
		DiffView diff = new DiffView();
		try {
			SimpleExecuter e = new SimpleExecuter(new String[] {
					"git", "show"
				}, diff, new IJLog());
		} catch (IOException e) {
			IJ.handleException(e);
		}

		JFrame frame = new JFrame("git show");
		frame.setSize(640, 480);
		frame.getContentPane().add(diff);
		frame.pack();
		frame.setVisible(true);
	}
}
