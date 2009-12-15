package ij.plugin.frame;
import java.awt.*;
import java.awt.event.*;

import ij.*;
import ij.plugin.*;
import ij.gui.*;

/** Displays a window that allows the user to set the font, size and style. */
public class Fonts extends PlugInFrame implements PlugIn, ItemListener {

	public static final String LOC_KEY = "fonts.loc";
	private static String[] sizes = {"8","9","10","12","14","18","24","28","36","48","60","72"};
	private static int[] isizes = {8,9,10,12,14,18,24,28,36,48,60,72};
	private Panel panel;
	private Choice font;
	private Choice size;
	private Choice style;
	private Checkbox checkbox;
	private static Frame instance;

	public Fonts() {
		super("Fonts");
		if (instance!=null) {
			instance.toFront();
			return;
		}
		WindowManager.addWindow(this);
		instance = this;
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		
		font = new Choice();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = ge.getAvailableFontFamilyNames();
		font.add("SansSerif");
		font.add("Serif");
		font.add("Monospaced");
		for (int i=0; i<fonts.length; i++) {
			String f = fonts[i];
			if (!(f.equals("SansSerif")||f.equals("Serif")||f.equals("Monospaced")))
				font.add(f);
		}
		font.select(TextRoi.getFont());
		font.addItemListener(this);
		add(font);

		size = new Choice();
		for (int i=0; i<sizes.length; i++)
			size.add(sizes[i]);
		size.select(getSizeIndex());
		size.addItemListener(this);
		add(size);
		
		style = new Choice();
		style.add("Plain");
		style.add("Bold");
		style.add("Italic");
		style.add("Bold+Italic");
		int i = TextRoi.getStyle();
		String s = "Plain";
		if (i==Font.BOLD)
			s = "Bold";
		else if (i==Font.ITALIC)
			s = "Italic";
		else if (i==(Font.BOLD+Font.ITALIC))
			s = "Bold+Italic";
		style.select(s);
		style.addItemListener(this);
		add(style);
		
		checkbox = new Checkbox("Smooth", TextRoi.isAntialiased());
		add(checkbox);
		checkbox.addItemListener(this);

		pack();
		Point loc = Prefs.getLocation(LOC_KEY);
		if (loc!=null)
			setLocation(loc);
		else
			GUI.center(this);
		show();
		IJ.register(Fonts.class);
	}
	
	int getSizeIndex() {
		int size = TextRoi.getSize();
		int index=0;
		for (int i=0; i<isizes.length; i++) {
			if (size>=isizes[i])
				index = i;
		}
		return index;
	}
	
	public void itemStateChanged(ItemEvent e) {
		String fontName = font.getSelectedItem();
		int fontSize = Integer.parseInt(size.getSelectedItem());
		String styleName = style.getSelectedItem();
		int fontStyle = Font.PLAIN;
		if (styleName.equals("Bold"))
			fontStyle = Font.BOLD;
		else if (styleName.equals("Italic"))
			fontStyle = Font.ITALIC;
		else if (styleName.equals("Bold+Italic"))
			fontStyle = Font.BOLD+Font.ITALIC;
		TextRoi.setFont(fontName, fontSize, fontStyle, checkbox.getState());
		IJ.showStatus(fontSize+" point "+fontName + " " + styleName);
	}
	
    public void windowClosing(WindowEvent e) {
	 	close();
		instance = null;
		Prefs.saveLocation(LOC_KEY, getLocation());
	}

}