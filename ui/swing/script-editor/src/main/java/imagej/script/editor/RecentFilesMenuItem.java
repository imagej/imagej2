package fiji.scripting;

import ij.Prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class RecentFilesMenuItem extends JMenu {
	protected TextEditor editor;
	protected int maxCount = 10, maxLength = 35;
	protected LinkedList<String> list = new LinkedList<String>();
	protected final static String prefsPrefix = "script.editor.recent";

	public RecentFilesMenuItem(TextEditor editor) {
		super("Open Recent");
		this.editor = editor;

		Stack<String> prefs = new Stack<String>();
		for (int i = 1; i <= maxCount; i++) {
			String item = Prefs.get(prefsPrefix + i, null);
			if (item == null)
				break;
			prefs.push(item);
		}

		if (prefs.empty())
			setEnabled(false);
		else
			while (!prefs.empty())
				add(prefs.pop());
	}

	public JMenuItem add(final String path) {
		setEnabled(true);

		// remove identical entries, if any
		int i = 0;
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			String item = iter.next();
			if (item.equals(path)) {
				if (i == 0)
					return getItem(i);
				iter.remove();
				remove(i);
			}
			else
				i++;
		}

		// keep the maximum count
		if (list.size() + 1 >= maxCount) {
			list.removeLast();
			remove(maxCount - 2);
		}

		// add to the list
		list.add(0, path);

		// persist
		i = 1;
		for (String item : list) {
			Prefs.set(prefsPrefix + i, item);
			i++;
		}

		// add the menu item
		String label = path;
		if (path.length() > maxLength)
			label = "..." + path.substring(path.length() - maxLength + 3);
		insert(label, 0);
		JMenuItem result = getItem(0);
		result.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.open(path);
			}
		});
		return result;
	}
}