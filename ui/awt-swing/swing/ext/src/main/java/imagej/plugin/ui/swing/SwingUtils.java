//
// SwingUtils.java
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

package imagej.plugin.ui.swing;

import imagej.util.awt.AWTWindows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Utility methods for working with Swing.
 * 
 * @author Curtis Rueden
 */
public final class SwingUtils {

	private SwingUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Displays a resizable dialog box containing the given component.
	 * <p>
	 * This method is very similar to
	 * {@link JOptionPane#showConfirmDialog(Component, Object, String, int, int)},
	 * except that it has a few extra features:
	 * </p>
	 * <ul>
	 * <li>It limits the size of the dialog based on the actual screen size.</li>
	 * <li>It can optionally add a scroll bar around the provided
	 * {@link Component} in case it is too large.</li>
	 * <li>It can start with a particular {@link Component} having the keyboard
	 * focus.</li>
	 * </ul>
	 * 
	 * @param parentComponent the parent {@link Component} for the dialog
	 * @param c the {@link Component} to display
	 * @param title the {@link String} to display in the dialog title bar
	 * @param messageType the type of message to be displayed:
	 *          {@link JOptionPane#ERROR_MESSAGE},
	 *          {@link JOptionPane#INFORMATION_MESSAGE},
	 *          {@link JOptionPane#WARNING_MESSAGE},
	 *          {@link JOptionPane#QUESTION_MESSAGE}, or
	 *          {@link JOptionPane#PLAIN_MESSAGE}
	 * @param doScrollPane whether to wrap the parent component in a
	 *          {@link JScrollPane} if the content is too large to fit in the
	 *          window.
	 * @param focusComponent the {@link Component} that should have the initial
	 *          keyboard focus.
	 */
	public static int showDialog(final Component parentComponent,
		final Component c, final String title, final int optionType,
		final int messageType, final boolean doScrollPane,
		final Component focusComponent)
	{
		final JOptionPane optionPane = new JOptionPane(c, messageType, optionType);

		if (doScrollPane) {
			final JPanel mainPane = (JPanel) optionPane.getComponent(0);
			final JPanel buttonPane = (JPanel) optionPane.getComponent(1);

			// wrap main pane in a scroll pane
			final JScrollPane wrappedMainPane = new JScrollPane(mainPane);

			// HACK: On Mac OS X (and maybe other platforms), setting the button
			// pane's border directly results in the right inset of the EmptyBorder
			// not being respected. Nesting the button panel in another panel avoids
			// the problem.
			final JPanel wrappedButtonPane = new JPanel();
			wrappedButtonPane.setLayout(new BorderLayout());
			wrappedButtonPane.add(buttonPane);

			// fix component borders, so that scroll pane is flush with dialog edge
			final EmptyBorder border = (EmptyBorder) optionPane.getBorder();
			final Insets insets = border.getBorderInsets();
			wrappedButtonPane.setBorder(new EmptyBorder(0, insets.left,
				insets.bottom, insets.right));
			optionPane.setBorder(null);
			optionPane.removeAll();
			optionPane.add(wrappedMainPane);
			optionPane.add(wrappedButtonPane);
		}

		// create dialog, set properties, pack and show
		final JDialog dialog = optionPane.createDialog(parentComponent, title);
		dialog.setResizable(true);
		dialog.setModal(true);
		dialog.pack();
		AWTWindows.ensureSizeReasonable(dialog);

		// HACK: When vertical scroll bar is needed, the dialog packs slightly too
		// small, resulting in an unnecessary horizontal scroll bar. Pad slightly.
		dialog.setSize(dialog.getSize().width + 20, dialog.getSize().height);

		AWTWindows.centerWindow(dialog);
		if (focusComponent != null) {
			setDefaultFocusComponent(dialog, focusComponent);
		}
		dialog.setVisible(true);
		
		// get result
		final Integer rval = (Integer) optionPane.getValue();
		
		// free resources
		dialog.dispose();

		// return result
		return rval == null ? JOptionPane.CANCEL_OPTION : rval;
	}

	/**
	 * Makes the given component grab the keyboard focus whenever the window gains
	 * the focus.
	 */
	public static void
		setDefaultFocusComponent(final Window w, final Component c)
	{
		w.addWindowFocusListener(new WindowAdapter() {

			@Override
			public void windowGainedFocus(final WindowEvent e) {
				c.requestFocusInWindow();
			}

		});
	}

}
