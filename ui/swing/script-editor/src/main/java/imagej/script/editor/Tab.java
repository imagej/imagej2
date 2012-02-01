//
// Tab.java
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class Tab extends JSplitPane {

	private final EditorFrame editorFrame;
	protected final EditorPane editorPane;
	protected final JTextArea screen;
	protected final JScrollPane scroll;
	protected boolean showingErrors;
	private Executer executer;
	private final JButton runit, killit, toggleErrors;

	public Tab(final EditorFrame editorFrame) {
		super(JSplitPane.VERTICAL_SPLIT);
		this.editorFrame = editorFrame;
		editorPane = new EditorPane(this.editorFrame);
		screen = new JTextArea();

		super.setResizeWeight(350.0 / 430.0);

		screen.setEditable(false);
		screen.setLineWrap(true);
		screen.setFont(new Font("Courier", Font.PLAIN, 12));

		final JPanel bottom = new JPanel();
		bottom.setLayout(new GridBagLayout());
		final GridBagConstraints bc = new GridBagConstraints();

		bc.gridx = 0;
		bc.gridy = 0;
		bc.weightx = 0;
		bc.weighty = 0;
		bc.anchor = GridBagConstraints.NORTHWEST;
		bc.fill = GridBagConstraints.NONE;
		runit = new JButton("Run");
		runit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent ae) {
				Tab.this.editorFrame.runText();
			}
		});
		bottom.add(runit, bc);

		bc.gridx = 1;
		killit = new JButton("Kill");
		killit.setEnabled(false);
		killit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent ae) {
				kill();
			}
		});
		bottom.add(killit, bc);

		bc.gridx = 2;
		bc.fill = GridBagConstraints.HORIZONTAL;
		bc.weightx = 1;
		bottom.add(new JPanel(), bc);

		bc.gridx = 3;
		bc.fill = GridBagConstraints.NONE;
		bc.weightx = 0;
		bc.anchor = GridBagConstraints.NORTHEAST;
		toggleErrors = new JButton("Show Errors");
		toggleErrors.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				toggleErrors();
			}
		});
		bottom.add(toggleErrors, bc);

		bc.gridx = 4;
		bc.fill = GridBagConstraints.NONE;
		bc.weightx = 0;
		bc.anchor = GridBagConstraints.NORTHEAST;
		final JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent ae) {
				if (showingErrors) Tab.this.editorFrame.errorScreen.setText("");
				else screen.setText("");
			}
		});
		bottom.add(clear, bc);

		bc.gridx = 0;
		bc.gridy = 1;
		bc.anchor = GridBagConstraints.NORTHWEST;
		bc.fill = GridBagConstraints.BOTH;
		bc.weightx = 1;
		bc.weighty = 1;
		bc.gridwidth = 5;
		screen.setEditable(false);
		screen.setLineWrap(true);
		final Font font = new Font("Courier", Font.PLAIN, 12);
		screen.setFont(font);
		scroll = new JScrollPane(screen);
		scroll.setPreferredSize(new Dimension(600, 80));
		bottom.add(scroll, bc);

		super.setTopComponent(editorPane.embedWithScrollbars());
		super.setBottomComponent(bottom);
	}

	/** Invoke in the context of the event dispatch thread. */
	private void prepare() {
		// TODO! editorPane.setEditable(false);
		runit.setEnabled(false);
		killit.setEnabled(true);
	}

	private void restore() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// TODO! editorPane.setEditable(true);
				runit.setEnabled(true);
				killit.setEnabled(false);
				executer = null;
			}
		});
	}

	public void toggleErrors() {
		showingErrors = !showingErrors;
		if (showingErrors) {
			toggleErrors.setText("Show Output");
			scroll.setViewportView(this.editorFrame.errorScreen);
		}
		else {
			toggleErrors.setText("Show Errors");
			scroll.setViewportView(screen);
		}
	}

	public void showErrors() {
		if (!showingErrors) toggleErrors();
		else if (scroll.getViewport().getView() == null) scroll
			.setViewportView(this.editorFrame.errorScreen);
	}

	public void showOutput() {
		if (showingErrors) toggleErrors();
	}

	public JTextArea getScreen() {
		return showingErrors ? this.editorFrame.errorScreen : screen;
	}

	public boolean isExecuting() {
		return null != executer;
	}

	public String getTitle() {
		return (editorPane.fileChanged() ? "*" : "") + editorPane.getFileName() +
			(isExecuting() ? " (Running)" : "");
	}

	/** Invoke in the context of the event dispatch thread. */
	public void execute(final ScriptEngineFactory language,
		final boolean selectionOnly) throws IOException
	{
		prepare();
		final JTextAreaWriter output = new JTextAreaWriter(this.screen);
		final JTextAreaWriter errors =
			new JTextAreaWriter(this.editorFrame.errorScreen);
		final ScriptEngine engine = language.getScriptEngine();
		this.editorFrame.scriptService.initialize(engine, editorPane.getFileName(),
			output, errors);
		// Pipe current text into the runScript:
		final PipedInputStream pi = new PipedInputStream();
		final PipedOutputStream po = new PipedOutputStream(pi);
		// The Executer creates a Thread that
		// does the reading from PipedInputStream
		this.executer = new Executer(this.editorFrame, output, errors) {

			@Override
			public void execute() {
				try {
					engine.eval(new InputStreamReader(pi));
					output.flush();
					errors.flush();
					Tab.this.editorFrame.markCompileEnd();
				}
				catch (final ScriptException e) {
					Tab.this.editorFrame.handleException(e);
				}
				finally {
					restore();
				}
			}
		};
		// Write into PipedOutputStream
		// from another Thread
		try {
			final String text;
			if (selectionOnly) {
				final String selected =
					this.editorFrame.getTextComponent().getSelectedText();
				if (selected == null) {
					this.editorFrame.error("Selection required!");
					text = null;
				}
				else text = selected + "\n"; // Ensure code blocks are terminated
			}
			else {
				text = this.editorFrame.getTextComponent().getText();
			}
			new Thread() {

				{
					setPriority(Thread.NORM_PRIORITY);
				}

				@Override
				public void run() {
					final PrintWriter pw = new PrintWriter(po);
					pw.write(text);
					pw.flush(); // will lock and wait in some cases
					try {
						po.close();
					}
					catch (final Throwable tt) {
						Log.error(tt);
					}
				}
			}.start();
		}
		catch (final Throwable t) {
			Log.error(t);
		}
		finally {
			// Re-enable when all text to send has been sent
			this.editorFrame.getTextComponent().setEditable(true);
		}
	}

	protected void kill() {
		if (null == executer) return;
		// Graceful attempt:
		executer.interrupt();
		// Give it 3 seconds. Then, stop it.
		final long now = System.currentTimeMillis();
		new Thread() {

			{
				setPriority(Thread.NORM_PRIORITY);
			}

			@Override
			public void run() {
				while (System.currentTimeMillis() - now < 3000)
					try {
						Thread.sleep(100);
					}
					catch (final InterruptedException e) {}
				if (null != executer) executer.obliterate();
				restore();

			}
		}.start();
	}
}
