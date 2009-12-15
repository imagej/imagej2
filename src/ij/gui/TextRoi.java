package ij.gui;
import ijx.gui.IjxImageCanvas;
import ijx.IjxImagePlus;
import java.awt.*;
import ij.*;
import ij.process.*;
import ij.util.Java2;


/** This class is a rectangular ROI containing text. */
public class TextRoi extends Roi {

	static final int MAX_LINES = 50;

	private String[] theText = new String[MAX_LINES];
	private static String name = "SansSerif";
	private static int style = Font.PLAIN;
	private static int size = 18;
	private static Font font;
	private static boolean antialiasedText = true;
	private static boolean recordSetFont = true;
	private double previousMag;
	private boolean firstChar = true;
	private boolean firstMouseUp = true;
	private int cline = 0;

	public TextRoi(int x, int y, IjxImagePlus imp) {
		super(x, y, imp);
        IjxImageCanvas ic = imp.getCanvas();
        double mag = (ic!=null)?ic.getMagnification():1.0;
        if (mag>1.0)
            mag = 1.0;
        if (size<(12/mag))
        	size = (int)(12/mag);
		theText[0] = "Type, then";
		theText[1] = "Ctl+D";
		if (previousRoi!=null && (previousRoi instanceof TextRoi)) {
			firstMouseUp = false;
			//IJ.write(""+previousRoi.getBounds());
			previousRoi = null;
		}
	}

	/** Adds the specified character to the end of the text string. */
	public void addChar(char c) {
		if (!(c>=' ' || c=='\b' || c=='\n')) return;
		if (firstChar) {
			cline = 0;
			theText[cline] = new String("");
			for (int i=1; i<MAX_LINES; i++)
				theText[i] = null;
		}
		if ((int)c=='\b') {
			// backspace
			if (theText[cline].length()>0)
				theText[cline] = theText[cline].substring(0, theText[cline].length()-1);
			else if (cline>0) {
				theText[cline] = null;
				cline--;
						}
			imp.draw(clipX, clipY, clipWidth, clipHeight);
			firstChar = false;
			return;
		} else if ((int)c=='\n') {
			// newline
			if (cline<(MAX_LINES-1)) cline++;
			theText[cline] = "";
			adjustSize();
		} else {
			char[] chr = {c};
			theText[cline] += new String(chr);
			adjustSize();
			updateClipRect();
			imp.draw(clipX, clipY, clipWidth, clipHeight);
			firstChar = false;
			return;
		}
	}

	Font getCurrentFont() {
		double mag = ic.getMagnification();
		if (font==null || mag!=previousMag) {
			font = new Font(name, style, (int)(size*mag));
			previousMag = mag;
		}
		return font;
	}
	
	/** Renders the text on the image. */
	public void drawPixels(ImageProcessor ip) {
		Font font = new Font(name, style, size);
		ip.setFont(font);
		ip.setAntialiasedText(antialiasedText);
		FontMetrics metrics = ip.getFontMetrics();
		int fontHeight = metrics.getHeight();
		int descent = metrics.getDescent();
		int i = 0;
		int yy = 0;
		while (i<MAX_LINES && theText[i]!=null) {
			ip.drawString(theText[i], x, y+yy+fontHeight);
			i++;
			yy += fontHeight;
		}
	}

	/** Draws the text on the screen, clipped to the ROI. */
	public void draw(Graphics g) {
		super.draw(g); // draw the rectangle
		g.setColor(instanceColor!=null?instanceColor:ROIColor);
		double mag = ic.getMagnification();
		int sx = ic.screenX(x);
		int sy = ic.screenY(y);
		int swidth = (int)(width*mag);
		int sheight = (int)(height*mag);
		Java2.setAntialiasedText(g, antialiasedText);
		if (font==null)
			adjustSize();
		Font font = getCurrentFont();
		FontMetrics metrics = g.getFontMetrics(font);
		int fontHeight = metrics.getHeight();
		int descent = metrics.getDescent();
		g.setFont(font);
		Rectangle r = g.getClipBounds();
		g.setClip(sx, sy, swidth, sheight);
		int i = 0;
		while (i<MAX_LINES && theText[i]!=null) {
			g.drawString(theText[i], sx, sy+fontHeight-descent);
			i++;
			sy += fontHeight;
		}
		if (r!=null) g.setClip(r.x, r.y, r.width, r.height);
	}

	/*
	void handleMouseUp(int screenX, int screenY) {
		if (width<size || height<size)
			grow(x+Math.max(size*5,width), y+Math.max((int)(size*1.5),height));
		super.handleMouseUp(screenX, screenY);
	}
	*/

	/** Returns the name of the current font. */
	public static String getFont() {
		return name;
	}

	/** Returns the current font size. */
	public static int getSize() {
		return size;
	}

	/** Returns the current font style. */
	public static int getStyle() {
		return style;
	}
	
	public static boolean isAntialiased() {
		return antialiasedText;
	}

	/** Sets the font face, size and style. */
	public static void setFont(String fontName, int fontSize, int fontStyle) {
		recordSetFont = true;
		setFont(fontName, fontSize, fontStyle, true);
	}
	
	/** Sets the font face, size, style and antialiasing mode. */
	public static void setFont(String fontName, int fontSize, int fontStyle, boolean antialiased) {
		recordSetFont = true;
		name = fontName;
		size = fontSize;
		style = fontStyle;
		antialiasedText = antialiased;
		font = null;
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null) {
			Roi roi = imp.getRoi();
			if (roi instanceof TextRoi)
				imp.draw();
		}
	}

	public void handleMouseUp(int screenX, int screenY) {
		super.handleMouseUp(screenX, screenY);
		if (firstMouseUp) {
			adjustSize();
			firstMouseUp = false;
		} else {
			if (width<5 || height<5)
				imp.killRoi();
		}
	}
	
	/** Increases the size of the rectangle so it's large
		enough to hold the text. */ 
	void adjustSize() {
		if (ic==null)
			return;
		double mag = ic.getMagnification();
		Font font = getCurrentFont();
		Graphics g = ic.getGraphics();
		Java2.setAntialiasedText(g, true);
		FontMetrics metrics = g.getFontMetrics(font);
		int fontHeight = (int)(metrics.getHeight()/mag);
		int descent = metrics.getDescent();
		int i=0, nLines=0;
		oldX = x;
		oldY = y;
		oldWidth = width;
		oldHeight = height;
		width = 10;
		while (i<MAX_LINES && theText[i]!=null) {
			nLines++;
			int w = (int)(stringWidth(theText[i],metrics,g)/mag);
			if (w>width)
				width = w;
			i++;
		}
		g.dispose();
		width += 2;
		if (x+width>xMax)
			x = xMax-width;
		height = nLines*fontHeight+2;
		if (height>yMax)
			height = yMax;
		if (y+height>yMax)
			y = yMax-height;
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
	}

	int stringWidth(String s, FontMetrics metrics, Graphics g) {
		return Java2.getStringWidth(s, metrics, g);
	}
	
	public String getMacroCode(ImageProcessor ip) {
		String code = "";
		if (recordSetFont) {
			String options = "";
			if (style==Font.BOLD)
				options += "bold";
			if (style==Font.ITALIC)
				options += " italic";
			if (antialiasedText)
				options += " antialiased";
			if (options.equals(""))
				options = "plain";
			code += "setFont(\""+name+"\", "+size+", \""+options+"\");\n";
			recordSetFont = false;
		}
		FontMetrics metrics = ip.getFontMetrics();
		int fontHeight = metrics.getHeight();
		String text = "";
		for (int i=0; i<MAX_LINES; i++) {
			if (theText[i]==null) break;
			text += theText[i];
			if (theText[i+1]!=null) text += "\\n";
		}
		code += "drawString(\""+text+"\", "+x+", "+(y+fontHeight)+");\n";
		return (code);
	}
	
	public static void recordSetFont() {
		recordSetFont = true;
	}

}