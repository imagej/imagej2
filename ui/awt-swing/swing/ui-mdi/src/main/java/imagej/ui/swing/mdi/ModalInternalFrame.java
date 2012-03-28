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

package imagej.ui.swing.mdi;

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MenuComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

/**
 * A modal internal frame, based on the <a
 * href="http://java.sun.com/jdc/JDCTechTips/2001/tt1220.html">Creating Modal
 * Internal Frames</a> JDC Tech Tip article.
 * 
 * @author Grant Harris
 */
public class ModalInternalFrame extends JInternalFrame {

	public ModalInternalFrame(final String title, final JRootPane rootPane,
		final Component desktop, final JOptionPane pane)
	{
		super(title);

		// create opaque glass pane
		final JPanel glass = new JPanel();
		glass.setOpaque(false);

		// Attach mouse listeners
		final MouseInputAdapter adapter = new MouseInputAdapter() {
			// no changes needed
		};
		glass.addMouseListener(adapter);
		glass.addMouseMotionListener(adapter);

		// Add in option pane
		getContentPane().add(pane, BorderLayout.CENTER);

		// Define close behavior
		final PropertyChangeListener pcl = new PropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if (isVisible() &&
					(event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY) || event
						.getPropertyName().equals(JOptionPane.INPUT_VALUE_PROPERTY)))
				{
					try {
						setClosed(true);
					}
					catch (final PropertyVetoException ignored) {
						// ignore
					}
					ModalInternalFrame.this.setVisible(false);
					glass.setVisible(false);
				}
			}

		};
		pane.addPropertyChangeListener(pcl);

		// Change frame border
		putClientProperty("JInternalFrame.frameType", "optionDialog");

		// Size frame
		final Dimension size = getPreferredSize();
		final Dimension rootSize = desktop.getSize();

		setBounds((rootSize.width - size.width) / 2,
			(rootSize.height - size.height) / 2, size.width, size.height);
		desktop.validate();
		try {
			setSelected(true);
		}
		catch (final PropertyVetoException ignored) {
			// ignore
		}

		// Add modal internal frame to glass pane
		glass.add(this);

		// Change glass pane to our panel
		rootPane.setGlassPane(glass);

		// Show glass pane, then modal dialog
		glass.setVisible(true);
	}

	@Override
	public void setVisible(final boolean value) {
		super.setVisible(value);
		if (value) {
			startModal();
		}
		else {
			stopModal();
		}
	}

	private synchronized void startModal() {
		try {
			if (SwingUtilities.isEventDispatchThread()) {
				final EventQueue theQueue = getToolkit().getSystemEventQueue();
				while (isVisible()) {
					final AWTEvent event = theQueue.getNextEvent();
					final Object source = event.getSource();
					if (event instanceof ActiveEvent) {
						((ActiveEvent) event).dispatch();
					}
					else if (source instanceof Component) {
						((Component) source).dispatchEvent(event);
					}
					else if (source instanceof MenuComponent) {
						((MenuComponent) source).dispatchEvent(event);
					}
					else {
						System.err.println("Unable to dispatch: " + event);
					}
				}
			}
			else {
				while (isVisible()) {
					wait();
				}
			}
		}
		catch (final InterruptedException ignored) {
			// ignore
		}
	}

	private synchronized void stopModal() {
		notifyAll();
	}

	public static void main(final String args[]) {
		final JFrame frame = new JFrame("Modal Internal Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JDesktopPane desktop = new JDesktopPane();

		final ActionListener showModal = new ActionListener() {

			Integer ZERO = new Integer(0);
			Integer ONE = new Integer(1);

			@Override
			public void actionPerformed(final ActionEvent e) {

				// Manually construct an input popup
				final JOptionPane optionPane =
					new JOptionPane("Print?", JOptionPane.QUESTION_MESSAGE,
						JOptionPane.YES_NO_OPTION);

				// Construct a message internal frame popup
				final JInternalFrame modal =
					new ModalInternalFrame("Really Modal", frame.getRootPane(), desktop,
						optionPane);

				modal.setVisible(true);

				final Object value = optionPane.getValue();
				if (value.equals(ZERO)) {
					System.out.println("Selected Yes");
				}
				else if (value.equals(ONE)) {
					System.out.println("Selected No");
				}
				else {
					System.err.println("Input Error");
				}
			}

		};

		final JInternalFrame internal = new JInternalFrame("Opener");
		desktop.add(internal);

		final JButton button = new JButton("Open");
		button.addActionListener(showModal);

		final Container iContent = internal.getContentPane();
		iContent.add(button, BorderLayout.CENTER);
		internal.setBounds(25, 25, 200, 100);
		internal.setVisible(true);

		final Container content = frame.getContentPane();
		content.add(desktop, BorderLayout.CENTER);
		frame.setSize(500, 300);
		frame.setVisible(true);
	}

}
