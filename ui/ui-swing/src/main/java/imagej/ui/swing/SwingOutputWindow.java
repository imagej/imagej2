/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.ui.swing;

import imagej.event.EventSubscriber;
import imagej.event.OutputEvent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *  Generalized textual output window.
 * 
 * 	Can be subscribed to OutputEvents for global output, e.g. logging.
 * 
 * @author GBH
 */
public class SwingOutputWindow extends JFrame implements EventSubscriber<OutputEvent> {
	
	JTextArea textArea = new JTextArea();
	
	// TODO: add tabular functionality
	
	
	public SwingOutputWindow(String title) {
		// Add a scrolling text area
		this.setTitle(title);
		textArea.setEditable(false);
		textArea.setRows(20);
		textArea.setColumns(50);
		java.awt.Font font = new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12);
		textArea.setFont(font);
		getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
		pack();
		setVisible(true);
		this.setBounds(new Rectangle(400, 400, 700, 300));
		this.setVisible(true);
	}

	public void append(String text) {
		textArea.append(text);
		// Make sure the last line is always visible
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}
	
	public void clear() {
		textArea.setText("");
	}

	@Override
	public void onEvent(OutputEvent event) {
		String output = event.getOutput();
		OutputEvent.TYPE type = event.getType();
		// 	LOG, INFO, RESULT, ERROR, DIAGNOSTIC
		if (type == OutputEvent.TYPE.ERROR) {
			textArea.setForeground(Color.RED);
		} else if (type == OutputEvent.TYPE.RESULT) {
			textArea.setForeground(Color.GREEN);
		} else if (type == OutputEvent.TYPE.INFO) {
			textArea.setForeground(Color.BLACK);
		} else if (type == OutputEvent.TYPE.LOG) {
			textArea.setForeground(Color.GRAY);
		} else if (type == OutputEvent.TYPE.DIAGNOSTIC) {
			textArea.setForeground(Color.MAGENTA);
		} else {
			textArea.setForeground(Color.BLACK);
		}
		append(output);
		textArea.setForeground(Color.BLACK);
	}

}