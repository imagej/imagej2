/*
* @(#)ColorSwatch.java  1.0  2008-03-01
*
* Copyright (c) 2008 Jeremy Wood
* E-mail: mickleness@gmail.com
* All rights reserved.
*
* The copyright of this software is owned by Jeremy Wood.
* You may not use, copy or modify this software, except in
* accordance with the license agreement you entered into with
* Jeremy Wood. For details see accompanying license terms.
*/

package com.bric.swing;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import com.bric.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.awt.image.*;

/** This is a square, opaque panel used to indicate
 * a certain color.
 * <P>The color is assigned with the <code>setForeground()</code> method.
 * <P>Also the user can right-click this panel and select 'Copy' to send
 * a 100x100 image of this color to the clipboard.  (This feature was
 * added at the request of a friend who paints; she wanted to select a
 * color and then quickly print it off, and then mix her paints to match
 * that shade.)
 * 
 * @version 1.0
 * @author Jeremy Wood
 */
public class ColorSwatch extends JPanel {
	private static final long serialVersionUID = 1L;
	
	
	JPopupMenu menu;
	JMenuItem copyItem;
	MouseListener mouseListener = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			if(e.isPopupTrigger()) {
				if(menu==null) {
					menu = new JPopupMenu();
					copyItem = new JMenuItem(ColorPicker.strings.getObject("Copy").toString());
					menu.add(copyItem);
					copyItem.addActionListener(actionListener);
				}
				menu.show(ColorSwatch.this,e.getX(),e.getY());
			}
		}
	};
	ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==copyItem) {
				BufferedImage image = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
				Graphics2D g = image.createGraphics();
				g.setColor(getBackground());
				g.fillRect(0, 0, image.getWidth(), image.getHeight());
				g.dispose();
				Transferable contents = new ImageTransferable(image);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, null);
			}
		}
	};
	int w;
	public ColorSwatch(int width) {
		w = width;
		setPreferredSize(new Dimension(width,width));
		setMinimumSize(new Dimension(width,width));
		addMouseListener(mouseListener);
	}
	
	private static TexturePaint checkerPaint = null;
	private static TexturePaint getCheckerPaint() {
		if(checkerPaint==null) {
			int t = 8;
			BufferedImage bi = new BufferedImage(t*2,t*2,BufferedImage.TYPE_INT_RGB);
			Graphics g = bi.createGraphics();
			g.setColor(Color.white);
			g.fillRect(0,0,2*t,2*t);
			g.setColor(Color.lightGray);
			g.fillRect(0,0,t,t);
			g.fillRect(t,t,t,t);
			checkerPaint = new TexturePaint(bi,new Rectangle(0,0,bi.getWidth(),bi.getHeight()));
		}
		return checkerPaint;
	}
	
	public void paint(Graphics g0) {
		super.paint(g0); //may be necessary for some look-and-feels?
		
		Graphics2D g = (Graphics2D)g0;
		
		Color c = getForeground();
		int w2 = Math.min(getWidth(), w);
		int h2 = Math.min(getHeight(), w);
		Rectangle r = new Rectangle(getWidth()/2-w2/2,getHeight()/2-h2/2, w2, h2);
		
		if(c.getAlpha()<255) {
			TexturePaint checkers = getCheckerPaint();
			g.setPaint(checkers);
			g.fillRect(r.x, r.y, r.width, r.height);
		}
		g.setColor(c);
		g.fillRect(r.x, r.y, r.width, r.height);
		PaintUtils.drawBevel(g, r);
	}
}

