package ij;
import ijx.IjxMenus;
import ij.io.*;
import ijx.CentralLookup;
import java.awt.*;
import java.io.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/** Opens, in a separate thread, files selected from the File/Open Recent submenu.*/
public class RecentOpener implements Runnable {
	private String path;

	public RecentOpener(String path) {
		this.path = path;
		Thread thread = new Thread(this, "RecentOpener");
		thread.start();
	}

	/** Open the file and move the path to top of the submenu. */
	public void run() {
		Opener o = new Opener();
		o.open(path);
        // @todo Deal with AWT...
        IjxMenus menus = CentralLookup.getDefault().lookup(IjxMenus.class);
		JMenu menu = (JMenu)menus.getOpenRecentMenu();
		int n = menu.getItemCount();
		int index = 0;
		for (int i=0; i<n; i++) {
			if (menu.getItem(i).getLabel().equals(path)) {
				index = i;
				break;
			}
		}
		if (index>0) {
			JMenuItem item = menu.getItem(index);
			menu.remove(index);
			menu.insert(item, 0);
		}
	}

}

