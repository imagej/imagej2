//
// RecentFilesMenuItem.java
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

import imagej.util.Prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
			String item = Prefs.get(getClass(), prefsPrefix + i, null);
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
			Prefs.put(getClass(), prefsPrefix + i, item);
			i++;
		}

		// add the menu item
		String label = path;
		if (path.length() > maxLength)
			label = "..." + path.substring(path.length() - maxLength + 3);
		insert(label, 0);
		JMenuItem result = getItem(0);
		result.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				editor.open(new File(path));
			}
		});
		return result;
	}
}
