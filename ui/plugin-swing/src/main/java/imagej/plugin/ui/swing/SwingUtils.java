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
	 * Displays a resizable dialog box containing the given component, with scroll
	 * bars as needed.
	 * <p>
	 * This method is very similar to
	 * {@link JOptionPane#showConfirmDialog(Component, Object, String, int, int)},
	 * except that it also limits the size of the dialog based on the actual
	 * screen size, and adds a scroll bar around the provided {@link Component} in
	 * case it is too large.
	 * </p>
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
	 */
	public static int showDialog(final Component parentComponent,
		final Component c, final String title, final int optionType,
		final int messageType)
	{
		final JOptionPane optionPane = new JOptionPane(parentComponent);
		optionPane.setOptionType(optionType);
		optionPane.setMessageType(messageType);
		final JPanel mainPane = (JPanel) optionPane.getComponent(0);
		final JPanel buttonPane = (JPanel) optionPane.getComponent(1);

		// add component to main pane
		final JPanel cPanel = new JPanel();
		cPanel.setLayout(new BorderLayout());
		cPanel.add(c);
		mainPane.removeAll();
		final JScrollPane scrollPane = new JScrollPane(cPanel);
		mainPane.add(scrollPane);

		// fix component borders, so that scroll pane is flush with dialog edge
		final EmptyBorder border = (EmptyBorder) optionPane.getBorder();
		optionPane.setBorder(null);
		// HACK: On Mac OS X (and maybe other platforms), setting the button pane's
		// border directly results in the right inset of the EmptyBorder not being
		// respected. Nesting the button panel in another panel avoids the problem.
		final JPanel wrappedButtonPane = new JPanel();
		wrappedButtonPane.setLayout(new BorderLayout());
		wrappedButtonPane.add(buttonPane);
		wrappedButtonPane.setBorder(border);
		optionPane.remove(buttonPane);
		optionPane.add(wrappedButtonPane);

		// create dialog, set properties, pack and show
		final JDialog dialog = optionPane.createDialog(title);
		dialog.setResizable(true);
		dialog.setModal(true);
		dialog.pack();
		AWTWindows.ensureSizeReasonable(dialog);
		AWTWindows.centerWindow(dialog);
		dialog.setVisible(true);

		// return result
		final Integer rval = (Integer) optionPane.getValue();
		return rval == null ? JOptionPane.CANCEL_OPTION : rval;
	}

}
