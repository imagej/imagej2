package ij.gui;

/*
 * This implements a canvas which draws a menu
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class MenuCanvas extends Panel {
	final private int pad = 2, padx = 8;
	private Color toolColor = new Color(0, 25, 45);

	PopupMenuBar menuBar;

	public MenuCanvas() {
		menuBar = new PopupMenuBar();
		setLayout(new FlowLayout());
		enableEvents(AWTEvent.MOUSE_EVENT_MASK |
				AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		g.setFont(menuBar.getFont());
		int size = menuBar.getFont().getSize();
		int[] widths = menuBar.calculateWidths(this, g);
		int w = pad + padx;
		for (int i = 0; i < widths.length; i++) {
			if (i == widths.length - 1 && menuBar.helpMenu != null)
				w = getWidth() - pad - widths[i];
			g.drawString(menuBar.getMenu(i).getLabel(),
					w, size - 1);
			w += widths[i] + padx * 2;
		}
	}

	public Dimension getPreferredSize() {
		int w = 2 * pad, h = getFont().getSize() + 2 * pad;
		int[] widths = menuBar.calculateWidths(this, getGraphics());
		for (int i = 0; i < widths.length; i++)
			w += widths[i] + padx * 2;
		return new Dimension(w, h);
	}

	int menuX;

	int getMenuIndex(int x) {
		int[] widths = menuBar.getWidths();
		int w = getWidth();
		Menu helpMenu = menuBar.getHelpMenu();
		int count = menuBar.getMenuCount();
		if (helpMenu != null) {
			count--;
			menuX = w - widths[widths.length - 1] - pad - padx;
			if (x < w - pad && x >= menuX)
				return count;
		}
		menuX = w = pad;
		for (int i = 0; i < count; i++) {
			w += widths[i] + 2 * padx;
			if (x < w)
				return i;
			menuX = w;
		}
		return -1;
	}

	protected void processMouseEvent(MouseEvent e) {
		int i = getMenuIndex(e.getX());
		int id = e.getID();

		if (id == MouseEvent.MOUSE_PRESSED) {
			if (i < 0)
				return;
			Dimension size = getSize();
			PopupMenu menu = (PopupMenu)menuBar.getMenu(i);
			add(menu);
			menu.show(this, menuX, size.height);
		}
		else if (id == MouseEvent.MOUSE_EXITED) {
			// reset
		}
		else if (id == MouseEvent.MOUSE_MOVED ||
				id == MouseEvent.MOUSE_ENTERED) {
			/*if (item != highlightedItem) {
				highlightedItem = item;
				if (highlightColor != null) repaint();
			}*/
		}
	}
}

