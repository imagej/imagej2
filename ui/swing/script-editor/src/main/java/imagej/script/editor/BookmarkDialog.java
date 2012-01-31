package fiji.scripting;

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
