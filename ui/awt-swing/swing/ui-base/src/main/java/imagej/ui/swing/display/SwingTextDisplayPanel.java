/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.ui.swing.display;

import imagej.display.Display;
import imagej.display.EventDispatcher;
import imagej.display.TextDisplay;
import imagej.display.TextDisplayPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author GBH
 */
public class SwingTextDisplayPanel  extends JPanel implements TextDisplayPanel {

	TextDisplay display;
	private final JTextArea textArea = new JTextArea();
	SwingTextDisplayWindow win;
	
	public SwingTextDisplayPanel(TextDisplay display, SwingTextDisplayWindow win) {
		this.display = display;
		this.win = win;
		textArea.setEditable(false);
		textArea.setRows(30);
		textArea.setColumns(80);
		final Font font = new Font("Monospaced", java.awt.Font.PLAIN, 12);
		textArea.setFont(font);
		//textArea.setPreferredSize(new Dimension(700,300));
		add(new JScrollPane(textArea), BorderLayout.CENTER);
		win.setContent(this);
	}

		// -- OutputWindow methods --

	@Override
	public void append(final String text) {
		textArea.append(text);
		// Make sure the last line is always visible
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}

	@Override
	public void clear() {
		textArea.setText("");
	}
	@Override
	public Display getDisplay() {
		return (Display) display;
	}

	@Override
	public void addEventDispatcher(EventDispatcher dispatcher) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void close() {
		
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void makeActive() {
		win.requestFocus();
	}

	@Override
	public void redoLayout() {
	}

	@Override
	public void setLabel(String s) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setTitle(String s) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void update() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
