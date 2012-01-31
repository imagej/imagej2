//
// FindAndReplaceDialog.java
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

@SuppressWarnings("serial")
public class FindAndReplaceDialog extends JDialog implements ActionListener {

	protected JTextComponent textComponent;
	protected JTextField searchField, replaceField;
	protected JLabel replaceLabel;
	protected JCheckBox matchCase, wholeWord, markAll, regex, forward;
	protected JButton findNext, replace, replaceAll, cancel;

	public FindAndReplaceDialog(final Frame owner,
		final JTextComponent textComponent)
	{
		super(owner);
		this.textComponent = textComponent;

		final Container root = getContentPane();
		root.setLayout(new GridBagLayout());

		final JPanel text = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();

		c.gridx = c.gridy = 0;
		c.gridwidth = c.gridheight = 1;
		c.weightx = c.weighty = 1;
		c.ipadx = c.ipady = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		searchField = createField("Find Next", text, c, null);
		replaceField = createField("Replace with", text, c, this);

		c.gridwidth = 4;
		c.gridheight = c.gridy;
		c.gridx = c.gridy = 0;
		c.weightx = c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		root.add(text, c);

		c.gridy = c.gridheight;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.001;
		matchCase = createCheckBox("Match Case", root, c);
		regex = createCheckBox("Regex", root, c);
		forward = createCheckBox("Search forward", root, c);
		forward.setSelected(true);
		c.gridx = 0;
		c.gridy++;
		markAll = createCheckBox("Mark All", root, c);
		wholeWord = createCheckBox("Whole Word", root, c);

		c.gridx = 4;
		c.gridy = 0;
		findNext = createButton("Find Next", root, c);
		replace = createButton("Replace", root, c);
		replaceAll = createButton("Replace All", root, c);
		cancel = createButton("Cancel", root, c);
		setResizable(true);
		pack();

		getRootPane().setDefaultButton(findNext);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		final KeyAdapter listener = new KeyAdapter() {

			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
			}
		};
		// TODO: handle via actionmap
		for (final Component component : getContentPane().getComponents())
			component.addKeyListener(listener);
		searchField.addKeyListener(listener);
		replaceField.addKeyListener(listener);
	}

	@Override
	public void show(final boolean replace) {
		setTitle(replace ? "Replace" : "Find");
		replaceLabel.setEnabled(replace);
		replaceField.setEnabled(replace);
		replaceField.setBackground(replace ? searchField.getBackground()
			: getRootPane().getBackground());
		this.replace.setEnabled(replace);
		replaceAll.setEnabled(replace);

		searchField.selectAll();
		replaceField.selectAll();
		getRootPane().setDefaultButton(findNext);
		setVisible(true);
	}

	private JTextField createField(final String name, final Container container,
		final GridBagConstraints c, final FindAndReplaceDialog replaceDialog)
	{
		c.weightx = 0.001;
		final JLabel label = new JLabel(name);
		if (replaceDialog != null) replaceDialog.replaceLabel = label;
		container.add(label, c);
		c.gridx++;
		c.weightx = 1;
		final JTextField field = new JTextField();
		container.add(field, c);
		c.gridx--;
		c.gridy++;
		return field;
	}

	private JCheckBox createCheckBox(final String name, final Container panel,
		final GridBagConstraints c)
	{
		final JCheckBox checkBox = new JCheckBox(name);
		checkBox.addActionListener(this);
		panel.add(checkBox, c);
		c.gridx++;
		return checkBox;
	}

	private JButton createButton(final String name, final Container panel,
		final GridBagConstraints c)
	{
		final JButton button = new JButton(name);
		button.addActionListener(this);
		panel.add(button, c);
		c.gridy++;
		return button;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object source = e.getSource();
		if (source == cancel) {
			dispose();
			return;
		}

		final String text = searchField.getText();
		if (text.length() == 0) return;

		final Pattern pattern =
			preparePattern(searchField.getText(), matchCase.isSelected(), wholeWord
				.isSelected(), regex.isSelected());
		try {
			if (source == findNext) find(pattern);
			else if (source == replace) replace(pattern, replaceField.getText());
			else if (source == replaceAll) {
				final int replace = replaceAll(pattern, replaceField.getText());
				JOptionPane.showMessageDialog(this, replace + " replacements made!");
			}
		}
		catch (final BadLocationException exception) {
			Log.error(exception);
		}
	}

	private Pattern preparePattern(final String search, final boolean matchCase,
		final boolean wholeWord, final boolean regex)
	{
		final String boundary = wholeWord ? "\\b" : "";
		return Pattern.compile(".*?(" + boundary +
			(regex ? search : Pattern.quote(search)) + boundary + ").*",
			Pattern.DOTALL | Pattern.MULTILINE |
				(matchCase ? 0 : Pattern.CASE_INSENSITIVE));
	}

	private int replaceAll(final Pattern pattern, final String replacement) {
		final int caret = textComponent.getCaretPosition();
		final StringBuilder builder = new StringBuilder();
		String text = textComponent.getText();
		int count = 0;
		for (;;) {
			final Matcher matcher = pattern.matcher(text);
			if (!matcher.matches()) break;
			builder.append(text.substring(0, matcher.start(1)));
			builder.append(replacement);
			text = text.substring(matcher.end(1));
			count++;
		}
		builder.append(text);
		textComponent.setText(builder.toString());
		textComponent.setCaretPosition(caret);
		return count;
	}

	private boolean replace(final Pattern pattern, final String replacement)
		throws BadLocationException
	{
		final int start = textComponent.getCaretPosition();
		final int length = textComponent.getDocument().getLength();
		final String text = textComponent.getText(start, length - start);
		final Matcher matcher = pattern.matcher(text);
		if (!matcher.matches()) return false;

		final StringBuilder builder = new StringBuilder();
		builder.append(textComponent.getText(0, start + matcher.start(1)));
		builder.append(replacement);
		builder.append(text.substring(matcher.end(1)));
		textComponent.setText(builder.toString());
		textComponent.setCaretPosition(start + matcher.start(1) +
			replacement.length());
		return true;
	}

	private boolean find(final Pattern pattern) throws BadLocationException {
		final int start = textComponent.getCaretPosition();
		final int length = textComponent.getDocument().getLength();
		final String text = textComponent.getText(start, length - start);
		final Matcher matcher = pattern.matcher(text);
		if (!matcher.matches()) return false;
		textComponent.setCaretPosition(start + matcher.start(1));
		return true;
	}

	public boolean isReplace() {
		return replace.isEnabled();
	}

	/**
	 * Sets the content of the search field.
	 * 
	 * @param pattern The new content of the search field.
	 */
	public void setSearchPattern(final String pattern) {
		searchField.setText(pattern);
	}
}
