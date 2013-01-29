/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.util.swing.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * A {@link TreeCellRenderer} for check box tree nodes.
 * <p>
 * Thanks to John Zukowski for the <a
 * href="http://www.java2s.com/Code/Java/Swing-JFC/CheckBoxNodeTreeSample.htm"
 * >sample code</a> upon which this is based.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class CheckBoxNodeRenderer implements TreeCellRenderer {

	private final CheckBoxNodePanel panel = new CheckBoxNodePanel();

	private final DefaultTreeCellRenderer defaultRenderer =
		new DefaultTreeCellRenderer();

	private final Color selectionForeground, selectionBackground;
	private final Color textForeground, textBackground;

	protected CheckBoxNodePanel getPanel() {
		return panel;
	}

	public CheckBoxNodeRenderer() {
		final Font fontValue = UIManager.getFont("Tree.font");
		if (fontValue != null) panel.label.setFont(fontValue);

		final Boolean focusPainted =
			(Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
		panel.check.setFocusPainted(focusPainted != null && focusPainted);

		selectionForeground = UIManager.getColor("Tree.selectionForeground");
		selectionBackground = UIManager.getColor("Tree.selectionBackground");
		textForeground = UIManager.getColor("Tree.textForeground");
		textBackground = UIManager.getColor("Tree.textBackground");
	}

	// -- TreeCellRenderer methods --

	@Override
	public Component getTreeCellRendererComponent(final JTree tree,
		final Object value, final boolean selected, final boolean expanded,
		final boolean leaf, final int row, final boolean hasFocus)
	{
		CheckBoxNodeData data = null;
		if (value instanceof DefaultMutableTreeNode) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			final Object userObject = node.getUserObject();
			if (userObject instanceof CheckBoxNodeData) {
				data = (CheckBoxNodeData) userObject;
			}
		}

		final String stringValue =
			tree.convertValueToText(value, selected, expanded, leaf, row, false);
		panel.label.setText(stringValue);
		panel.check.setSelected(false);

		panel.setEnabled(tree.isEnabled());

		if (selected) {
			panel.setForeground(selectionForeground);
			panel.setBackground(selectionBackground);
			panel.label.setForeground(selectionForeground);
			panel.label.setBackground(selectionBackground);
		}
		else {
			panel.setForeground(textForeground);
			panel.setBackground(textBackground);
			panel.label.setForeground(textForeground);
			panel.label.setBackground(textBackground);
		}

		if (data == null) {
			// not a check box node; return default cell renderer
			return defaultRenderer.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, hasFocus);
		}

		panel.label.setText(data.getText());
		panel.check.setSelected(data.isChecked());

		return panel;
	}

}
