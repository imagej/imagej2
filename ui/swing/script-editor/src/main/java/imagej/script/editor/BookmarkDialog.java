//
// BookmarkDialog.java
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

import java.awt.Frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

public class BookmarkDialog extends JDialog implements ActionListener {
	JList list;
	JButton okay, cancel;

	public BookmarkDialog(final Frame owner, final Vector<EditorPane.Bookmark> bookmarks) {
		super(owner, "Bookmarks", true);

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		list = new JList(bookmarks);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = list.locationToIndex(e.getPoint());
					bookmarks.get(index).setCaret();
					BookmarkDialog.this.dispose();
				}
			}
		});
		getContentPane().add(list);

		okay = new JButton("OK");
		okay.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		JPanel panel = new JPanel();
		panel.add(okay);
		panel.add(cancel);
		getContentPane().add(panel);

		KeyListener keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == e.VK_ENTER) {
					jumpToSelectedBookmark();
					dispose();
				}
				else if (e.getKeyCode() == e.VK_ESCAPE)
					dispose();
			}
		};
		getContentPane().addKeyListener(keyListener);
		list.addKeyListener(keyListener);
		okay.addKeyListener(keyListener);

		pack();
		setLocationRelativeTo(owner);
	}

	public boolean jumpToSelectedBookmark() {
		EditorPane.Bookmark bookmark = (EditorPane.Bookmark)list.getSelectedValue();
		if (bookmark == null)
			return false;
		bookmark.setCaret();
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == cancel)
			dispose();
		else if (source == okay) {
			jumpToSelectedBookmark();
			dispose();
		}
	}
}
