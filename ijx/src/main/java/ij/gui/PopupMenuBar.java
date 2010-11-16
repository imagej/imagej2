package ij.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.PopupMenu;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

/*
 * This class serves as a simple container.
 */

public class PopupMenuBar extends MenuBar {
	private Vector menus = new Vector();
	Menu helpMenu;
	private int[] widths;
	private boolean calculated = false;

	public PopupMenuBar() {
		setFont(new Font("SansSerif", Font.PLAIN, 12));
	}

	public void setFont(Font f) {
		calculated = false;
		super.setFont(f);
	}

	public Menu getHelpMenu() {
		return helpMenu;
	}

	public void setHelpMenu(Menu m) {
		calculated = false;
		if (!(m instanceof PopupMenu))
			throw new RuntimeException("IJMenuBar needs PopupMenus");
		helpMenu = m;
	}

	public Menu add(Menu m) {
		calculated = false;
		if (!(m instanceof PopupMenu))
			throw new RuntimeException("IJMenuBar needs PopupMenus"
				+ "(offending menu: " + m.getLabel() + ")");
		menus.add(m);
		return m;
	}

	public void remove(int i) {
		calculated = false;
		menus.remove(i);
	}

	public Menu getMenu(int i) {
		if (i >= menus.size())
			return helpMenu;
		return (Menu)menus.get(i);
	}

	public int getMenuCount() {
		return menus.size() + (helpMenu != null ? 1 : 0);
	}

	int[] calculateWidths(Component c, Graphics g) {
		if (calculated)
			return widths;
		widths = new int[getMenuCount()];
		FontMetrics metrics = c.getFontMetrics(getFont());
		for (int i = 0; i < widths.length; i++) {
			String label = getMenu(i).getLabel();
			Rectangle2D bounds = metrics.getStringBounds(label, g);
			widths[i] = (int)Math.round(bounds.getWidth());
		}
		calculated = true;
		return widths;
	}

	int[] getWidths() {
		return widths;
	}
}

