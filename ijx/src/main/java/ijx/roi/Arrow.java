package ijx.roi;
import ijx.process.ImageProcessor;
import ijx.roi.Line;
import ijx.roi.ShapeRoi;
import ijx.Prefs;
import ijx.IJ;
import ij.*;

import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.geom.*;


/** This is an Roi subclass for creating and displaying arrows. */
public class Arrow extends Line {
	public static final String STYLE_KEY = "arrow.style";
	public static final String WIDTH_KEY = "arrow.width";
	public static final String SIZE_KEY = "arrow.size";
	public static final String DOUBLE_HEADED_KEY = "arrow.double";
	public static final int FILLED=0, NOTCHED=1, OPEN=2, HEADLESS=3;
	public static final String[] styles = {"Filled", "Notched", "Open", "Headless"};
	private static int defaultStyle = (int)Prefs.get(STYLE_KEY, FILLED);
	private static float defaultWidth = (float)Prefs.get(WIDTH_KEY, 2);
	private static double defaultHeadSize = (int)Prefs.get(SIZE_KEY, 10);  // 0-30;
	private static boolean defaultDoubleHeaded = Prefs.get(DOUBLE_HEADED_KEY, false);
	private int style;
	private double headSize = 10;  // 0-30
	private boolean doubleHeaded;
	
	static {
		if (defaultStyle<FILLED || defaultStyle>HEADLESS)
			defaultStyle = FILLED;
	}

	public Arrow(double ox1, double oy1, double ox2, double oy2) {
		super(ox1, oy1, ox2, oy2);
		setStrokeWidth(2);
		style = defaultStyle;
		headSize = defaultHeadSize;
		doubleHeaded = defaultDoubleHeaded;
	}

	public Arrow(int sx, int sy, IjxImagePlus imp) {
		super(sx, sy, imp);
		setStrokeWidth(defaultWidth);
		style = defaultStyle;
		headSize = defaultHeadSize;
		doubleHeaded = defaultDoubleHeaded;
	}

	/** Draws this arrow on the image. */
	public void draw(Graphics g) {
		if (ic==null) return;
		Color color =  strokeColor!=null? strokeColor:ROIColor;
		g.setColor(color);
		x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
		x1=(int)x1d; y1=(int)y1d; x2=(int)x2d; y2=(int)y2d;
		int sx1 = ic.screenXD(x1d);
		int sy1 = ic.screenYD(y1d);
		int sx2 = ic.screenXD(x2d);
		int sy2 = ic.screenYD(y2d);
		int sx3 = sx1 + (sx2-sx1)/2;
		int sy3 = sy1 + (sy2-sy1)/2;
		drawArrow((Graphics2D)g, null, sx1, sy1, sx2, sy2);
		if (doubleHeaded)
			drawArrow((Graphics2D)g, null, sx2, sy2, sx1, sy1);
		if (getState()!=CONSTRUCTING && !overlay) {
			int size2 = HANDLE_SIZE/2;
			handleColor=Color.white;
			drawHandle(g, sx1-size2, sy1-size2);
			drawHandle(g, sx2-size2, sy2-size2);
			drawHandle(g, sx3-size2, sy3-size2);
		}
		if (getState()!=NORMAL)
			IJ.showStatus(imp.getLocationAsString(x2,y2)+", angle=" + IJ.d2s(getAngle(x1,y1,x2,y2)) + ", length=" + IJ.d2s(getLength()));
		if (updateFullWindow)
			{updateFullWindow = false; imp.draw();}
	}

	void drawArrow(Graphics2D g, ImageProcessor ip, double x1, double y1, double x2, double y2) {
		double mag = ip==null?ic.getMagnification():1.0;
		double arrowWidth = getStrokeWidth();
		if (ip==null) {
			g.setStroke(new BasicStroke((float)(arrowWidth*mag)));
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		double size = 8+10*arrowWidth*mag*0.5;
		size = size*(headSize/10.0);
		double dx = x2-x1;
		double dy = y2-y1;
		double ra = Math.sqrt(dx*dx + dy*dy);
		dx = dx/ra;
		dy = dy/ra;
		double x3 = x2-dx*size;
		double y3 = y2-dy*size;
		double ratio = style==OPEN?0.45:0.35;
		double r = ratio*size;
		double x4 = Math.round(x3+dy*r);
		double y4 = Math.round(y3-dx*r);
		double x5 = Math.round(x3-dy*r);
		double y5 = Math.round(y3+dx*r);
		double x6 = x2-dx*0.85*size;
		double y6 = y2-dy*0.85*size;
		if (ra>size || style==HEADLESS) {
			double scale = style==OPEN?0.25:0.75;
			if (style==OPEN) size /= 3.0;
			double yadjust = scale*dy*size;
			if (style==HEADLESS) scale = 0.0;
			int xx1 = doubleHeaded||style==HEADLESS?(int)(x1+scale*dx*size):(int)x1;
			int yy1 = doubleHeaded||style==HEADLESS?(int)(y1+scale*dy*size):(int)y1;
			int xx2 = (int)(x2-scale*dx*size);
			int yy2 = (int)(y2-scale*dy*size);
			if (ip!=null)
				ip.drawLine(xx1, yy1, xx2, yy2);
			else
				g.drawLine(xx1, yy1, xx2, yy2);
		}
		if (style==HEADLESS)
			return;
		GeneralPath path = new GeneralPath();
		path.moveTo((float)x4, (float)y4);
		path.lineTo((float)x2, (float)y2);
		path.lineTo((float)x5, (float)y5);
		if (style!=OPEN) {
			if (style==NOTCHED)
				path.lineTo((float)x6, (float)y6);
			path.lineTo((float)x4, (float)y4);
		}
		if (ip!=null) {
			if (style==OPEN) {
				ip.moveTo((int)x4,(int)y4);
				ip.lineTo((int)x2, (int)y2);
				ip.lineTo((int)x5, (int)y5);
			} else
				ip.fill(new ShapeRoi(path));
		} else {
			if (style==OPEN) {
				if (x1!=x2 || y1!=y2) g.draw(path);
			} else
				g.fill(path);
		}
	}

	public void drawPixels(ImageProcessor ip) {
		int width = (int)Math.round(getStrokeWidth());
		ip.setLineWidth(width);
		drawArrow(null, ip, x1, y1, x2, y2);
		if (doubleHeaded)
			drawArrow(null, ip, x2, y2, x1, y1);
	}
	
	protected int clipRectMargin() {
		double mag = ic!=null?ic.getMagnification():1.0;
		double arrowWidth = getStrokeWidth();
		double size = 8+10*arrowWidth*mag*0.5;
		return (int)Math.max(size*2.0, headSize);
	}
			
	public boolean isDrawingTool() {
		return true;
	}
	
	public static void setDefaultWidth(double width) {
		defaultWidth = (float)width;
	}

	public static double getDefaultWidth() {
		return defaultWidth;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public int getStyle() {
		return style;
	}

	public static void setDefaultStyle(int style) {
		defaultStyle = style;
	}

	public static int getDefaultStyle() {
		return defaultStyle;
	}

	public void setHeadSize(double headSize) {
		this.headSize = headSize;
	}

	public double getHeadSize() {
		return headSize;
	}

	public static void setDefaultHeadSize(double size) {
		defaultHeadSize = size;
	}

	public static double getDefaultHeadSize() {
		return defaultHeadSize;
	}

	public void setDoubleHeaded(boolean b) {
		doubleHeaded = b;
	}

	public boolean getDoubleHeaded() {
		return doubleHeaded;
	}

	public static void setDefaultDoubleHeaded(boolean b) {
		defaultDoubleHeaded = b;
	}

	public static boolean getDefaultDoubleHeaded() {
		return defaultDoubleHeaded;
	}

}
