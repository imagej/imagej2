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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;

// Helper functions to instantiated Swing components
@SuppressWarnings("serial")
public class SwingTools {

	public static JTabbedPane tab(final Component component, final String title,
		final String tooltip, final int width, final int height,
		final Container addTo)
	{
		final JPanel tab = new JPanel();
		tab.setLayout(new BorderLayout());
		tab.add(scrollPane(component, width, height, null), BorderLayout.CENTER);

		final JTabbedPane tabbed = new JTabbedPane();
		tabbed.addTab(title, null, tab, tooltip);
		tabbed.setPreferredSize(new Dimension(width, height));
		if (addTo != null) addTo.add(tabbed);
		return tabbed;
	}

	public static JScrollPane scrollPane(final Component component,
		final int width, final int height, final Container addTo)
	{
		final JScrollPane scroll = new JScrollPane(component);
		scroll.getViewport().setBackground(component.getBackground());
		scroll.setPreferredSize(new Dimension(width, height));
		if (addTo != null) addTo.add(scroll);
		return scroll;
	}

	public static JPanel label(final String text, final Container addTo) {
		final JLabel label = new JLabel(text, SwingConstants.LEFT);
		final JPanel panel = horizontalPanel();
		panel.add(label);
		panel.add(Box.createHorizontalGlue());
		if (addTo != null) addTo.add(panel);
		return panel;
	}

	public static JPanel boxLayoutPanel(final int alignment) {
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, alignment));
		return panel;
	}

	public static JPanel horizontalPanel() {
		return boxLayoutPanel(BoxLayout.X_AXIS);
	}

	public static JPanel verticalPanel() {
		return boxLayoutPanel(BoxLayout.Y_AXIS);
	}

	public static JButton button(final String title, final String toolTip,
		final ActionListener listener, final Container addTo)
	{
		final JButton button = new JButton(title);
		button.setToolTipText(toolTip);
		button.addActionListener(listener);
		if (addTo != null) addTo.add(button);
		return button;
	}

	public static JPanel labelComponent(final String text,
		final JComponent component, final Container addTo)
	{
		final JPanel panel = horizontalPanel();
		final JLabel label = new JLabel(text, SwingConstants.LEFT);
		panel.add(label);
		panel.add(Box.createRigidArea(new Dimension(10, 0)));
		panel.add(component);
		if (addTo != null) addTo.add(panel);
		return panel;
	}

	/** Uses a GridBagLayout to prevent odd resizing behaviours. */
	public static JPanel labelComponentRigid(final String text,
		final JComponent component)
	{
		final JPanel panel = new JPanel();
		final GridBagLayout gb = new GridBagLayout();
		panel.setLayout(gb);
		final GridBagConstraints c = new GridBagConstraints(0, 0, // x, y
			1, 3, // rows, cols
			0, 0, // weightx, weighty
			GridBagConstraints.WEST, // anchor
			GridBagConstraints.NONE, // fill
			new Insets(0, 0, 0, 0), 0, 0); // ipadx, ipady
		final JLabel label = new JLabel(text, SwingConstants.LEFT);
		gb.setConstraints(label, c);
		panel.add(label);

		final Component box = Box.createRigidArea(new Dimension(10, 0));
		c.gridx = 1;
		gb.setConstraints(box, c);
		panel.add(box);

		c.gridx = 2;
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		gb.setConstraints(component, c);
		panel.add(component);

		return panel;
	}

	public static JTextPane scrolledText(final int width, final int height,
		final String text, final DocumentListener listener, final Container addTo)
	{
		final JTextPane component = new JTextPane();
		component.getDocument().addDocumentListener(listener);
		if (text != null) component.setText(text);
		component.setSelectionStart(0);
		component.setSelectionEnd(0);
		scrollPane(component, width, height, addTo);
		return component;
	}

	public static JTextPane scrolledText(final int width, final int height,
		final Iterable<String> list, final DocumentListener listener,
		final Container addTo)
	{
		final StringBuilder builder = new StringBuilder();
		for (final String text : list)
			builder.append(text + "\n");
		return scrolledText(width, height, builder.toString(), listener, addTo);
	}

	/**
	 * Add a keyboard accelerator to a container. This method adds a keystroke to
	 * the input map of a container that sends an action event with the given
	 * source to the given listener.
	 */
	public static void addAccelerator(final Component source,
		final JComponent container, final ActionListener listener, final int key,
		final int modifiers)
	{
		container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
			KeyStroke.getKeyStroke(key, modifiers), source);
		if (container.getActionMap().get(source) != null) return;
		container.getActionMap().put(source, new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (!source.isEnabled()) return;
				final ActionEvent event = new ActionEvent(source, 0, "Accelerator");
				listener.actionPerformed(event);
			}
		});
	}

	public static boolean showQuestion(final Component owner, final String title,
		final String question)
	{
		return JOptionPane.showConfirmDialog(owner, question, title,
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
	}

	public static boolean showYesNoQuestion(final Component owner,
		final String title, final String question)
	{
		return JOptionPane.showConfirmDialog(owner, question, title,
			JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
	}

	public static void showMessageBox(final Component owner,
		final String message, final int type)
	{
		final String title =
			type == JOptionPane.ERROR_MESSAGE ? "Error"
				: type == JOptionPane.WARNING_MESSAGE ? "Warning" : "Information";
		JOptionPane.showMessageDialog(owner, message, title, type);
	}

	public static String getChoice(final Component owner,
		final List<String> list, final String question, final String title)
	{
		final String[] array = list.toArray(new String[list.size()]);
		final JOptionPane pane =
			new JOptionPane(question, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null, array);
		pane.createDialog(owner, title).setVisible(true);
		return (String) pane.getValue();
	}

	public static void waitForFakeDialog(final String title,
		final Component component)
	{
		// Do not show, but wait for dispose() to be called
		final JDialog fakeDialog = new JDialog((Dialog) null, title) {

			@Override
			public void dispose() {
				synchronized (this) {
					notifyAll();
				}
				super.dispose();
			}
		};
		fakeDialog.getContentPane().add(component);
		fakeDialog.pack();
		try {
			synchronized (fakeDialog) {
				fakeDialog.wait();
			}
		}
		catch (final InterruptedException e) { /* ignore */}
	}
}
