package ijx.roi;
import ijx.process.ImageProcessor;
import ijx.util.Java2;
import ijx.util.Tools;
import ijx.WindowManager;
import ijx.IJ;
import java.awt.*;
import ij.*;

import ijx.macro.Interpreter;
import ijx.IjxImagePlus;
import ijx.gui.IjxImageCanvas;


/** This class is a rectangular ROI containing text. */
public class TextRoi extends Roi {

	static final int MAX_LINES = 50;

	private String[] theText = new String[MAX_LINES];
	private static String name = "SansSerif";
	private static int style = Font.PLAIN;
	private static int size = 18;
	private Font instanceFont;
	private static boolean newFont = true;
	private static boolean antialiasedText = true;
	private static boolean recordSetFont = true;
	private double previousMag;
	private boolean firstChar = true;
	private boolean firstMouseUp = true;
	private int cline = 0;

	/** Creates a new TextRoi.*/
	public TextRoi(int x, int y, String text) {
		this(x, y, text, null);
	}

	/** Creates a new TextRoi with the specified location and Font.
	 * @see ij.gui.Roi#setStrokeColor
	 * @see ij.gui.Roi#setNonScalable
	 * @see ij.IjxImagePlus#setOverlay(ij.gui.Overlay)
	 */
	public TextRoi(int x, int y, String text, Font font) {
		super(x, y, 1, 1);
		String[] lines = Tools.split(text, "\n");
		int count = Math.min(lines.length, MAX_LINES);
		for (int i=0; i<count; i++)
			theText[i] = lines[i];
		if (font==null) font = new Font(name, style, size);
		instanceFont = font;
		firstChar = false;
		if (IJ.debugMode) IJ.log("TextRoi: "+theText[0]+"  "+width+","+height);
	}

	/** @deprecated */
	public TextRoi(int x, int y, String text, Font font, Color color) {
		super(x, y, 1, 1);
		if (font==null) font = new Font(name, style, size);
		instanceFont = font;
		IJ.error("TextRoi", "API has changed. See updated example at\nhttp://rsb.info.nih.gov/ij/macros/js/TextOverlay.js");
	}

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
		instanceFont = new Font(name, style, size);
	}

	/** This method is used by the text tool to add typed
		characters to displayed text selections. */
	public void addChar(char c) {
		if (imp==null) return;
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
			updateBounds(null);
			updateText();
		} else {
			char[] chr = {c};
			theText[cline] += new String(chr);
			updateBounds(null);
			updateText();
			firstChar = false;
			return;
		}
	}

	Font getScaledFont() {
		if (nonScalable)
			return instanceFont;
		else {
			if (instanceFont==null)
				instanceFont = new Font(name, style, size);
			double mag = (ic!=null)?ic.getMagnification():1.0;
			return instanceFont.deriveFont((float)(instanceFont.getSize()*mag));
		}
	}
	
	/** Renders the text on the image. */
	public void drawPixels(ImageProcessor ip) {
		ip.setFont(instanceFont);
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
		if (IJ.debugMode) IJ.log("draw: "+theText[0]+"  "+width+","+height);
		if (ic==null) return;
		if (Interpreter.isBatchMode() && ic.getDisplayList()!=null) return;
		if (newFont || width==1)
			updateBounds(g);
		super.draw(g); // draw the rectangle
		double mag = ic.getMagnification();
		int sx = ic.screenX(x);
		int sy = ic.screenY(y);
		int swidth = (int)(width*mag);
		int sheight = (int)(height*mag);
		Rectangle r = null;
		r = g.getClipBounds();
		g.setClip(sx, sy, swidth, sheight);
		drawText(g);
		if (r!=null) g.setClip(r.x, r.y, r.width, r.height);
	}
	
	public void drawText(Graphics g) {
		if (hide) {
			hide = false;
			return;
		}
		g.setColor( strokeColor!=null? strokeColor:ROIColor);
		Java2.setAntialiasedText(g, antialiasedText);
		if (newFont || width==1)
			updateBounds(g);
		double mag = ic.getMagnification();
		int sx = nonScalable?x:ic.screenX(x);
		int sy = nonScalable?y:ic.screenY(y);
		Font font = getScaledFont();
		FontMetrics metrics = g.getFontMetrics(font);
		int fontHeight = metrics.getHeight();
		int descent = metrics.getDescent();
		g.setFont(font);
		int i = 0;
		if (fillColor!=null) {
			if (getStrokeWidth()<10) {
				Color saveFillColor = fillColor;
				setStrokeWidth(10);
				fillColor = saveFillColor;
			}
			updateBounds(g);
			Color c = g.getColor();
			int alpha = fillColor.getAlpha();
 			g.setColor(fillColor);
 			Graphics2D g2d = (Graphics2D)g;
			int sw = nonScalable?width:(int)(ic.getMagnification()*width);
			int sh = nonScalable?height:(int)(ic.getMagnification()*height);
			g.fillRect(sx-5, sy-5, sw+10, sh+10);
			g.setColor(c);
		}
		while (i<MAX_LINES && theText[i]!=null) {
			g.drawString(theText[i], sx, sy+fontHeight-descent);
			i++;
			sy += fontHeight;
		}
	}

	/*
	void handleMouseUp(int screenX, int screenY) {
		if (width<size || height<size)
			grow(x+Math.max(size*5,width), y+Math.max((int)(size*1.5),height));
		super.handleMouseUp(screenX, screenY);
	}
	*/

	/** Returns the name of the global font. */
	public static String getFont() {
		return name;
	}

	/** Returns the global font size. */
	public static int getSize() {
		return size;
	}

	/** Returns the global font style. */
	public static int getStyle() {
		return style;
	}
	
	/** Set the current (instance) font. */
	public void setCurrentFont(Font font) {
		instanceFont = font;
		updateBounds(null);
	}
	
	/** Returns the current (instance) font. */
	public Font getCurrentFont() {
		return instanceFont;
	}
	
	public static boolean isAntialiased() {
		return antialiasedText;
	}

	/** Sets the global font face, size and style that will be used by
		TextROIs interactively created using the text tool. */
	public static void setFont(String fontName, int fontSize, int fontStyle) {
		recordSetFont = true;
		setFont(fontName, fontSize, fontStyle, true);
	}
	
	/** Sets the font face, size, style and antialiasing mode that will 
		be used by TextROIs interactively created using the text tool. */
	public static void setFont(String fontName, int fontSize, int fontStyle, boolean antialiased) {
		recordSetFont = true;
		name = fontName;
		size = fontSize;
		style = fontStyle;
		antialiasedText = antialiased;
		newFont = true;
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null) {
			Roi roi = imp.getRoi();
			if (roi instanceof TextRoi) {
				((TextRoi)roi).setCurrentFont(new Font(name, style, size));
				imp.draw();
			}
		}
	}
	
	public void handleMouseUp(int screenX, int screenY) {
		super.handleMouseUp(screenX, screenY);
		if (firstMouseUp) {
			updateBounds(null);
			updateText();
			firstMouseUp = false;
		} else {
			if (width<5 || height<5)
				imp.killRoi();
		}
	}
	
	/** Increases the size of bounding rectangle so it's large enough to hold the text. */ 
	void updateBounds(Graphics g) {
		//IJ.log("adjustSize1: "+theText[0]+"  "+width+","+height);
		if (ic==null) return;
		double mag = ic.getMagnification();
		if (nonScalable) mag = 1.0;
		Font font = getScaledFont();
		newFont = false;
		boolean nullg = g==null;
		if (nullg) g = ic.getCanvas().getGraphics();
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
		if (nullg) g.dispose();
		width += 2;
		if (xMax!=0 && x+width>xMax)
			x = xMax-width;
		height = nLines*fontHeight+2;
		if (yMax!=0) {
			if (height>yMax)
				height = yMax;
			if (y+height>yMax)
				y = yMax-height;
		}
		//IJ.log("adjustSize2: "+theText[0]+"  "+width+","+height);
	}
	
	void updateText() {
		if (imp!=null) {
			updateClipRect();
			imp.draw(clipX, clipY, clipWidth, clipHeight);
		}
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
		code += "makeText(\""+text+"\", "+x+", "+(y+fontHeight)+");\n";
		code += "//drawString(\""+text+"\", "+x+", "+(y+fontHeight)+");\n";
		return (code);
	}
	
	public String getText() {
		String text = "";
		for (int i=0; i<MAX_LINES; i++) {
			if (theText[i]==null) break;
			text += theText[i]+"\n";
		}
		return text;
	}
	
	public static void recordSetFont() {
		recordSetFont = true;
	}
	
	public boolean isDrawingTool() {
		return true;
	}
	
	/** Returns a copy of this TextRoi. */
	public synchronized Object clone() {
		TextRoi tr = (TextRoi)super.clone();
		tr.theText = new String[MAX_LINES];
		for (int i=0; i<MAX_LINES; i++)
			tr.theText[i] = theText[i];
		return tr;
	}
        
}
