package ij.gui;

// BDZ -- ADDITIONS BEGIN
//   NOTE - this version of the file is from 1.44m7. Wayne had fixed a bug after 1.44l9 that I had to pull into patches to get our tests passing.
// BDZ -- ADDITIONS END

import java.awt.*;
import java.awt.image.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import ij.*;
import ij.process.*;
import ij.measure.*;
import ij.plugin.frame.Recorder;
import ij.plugin.filter.Analyzer;
import ij.macro.Interpreter;

/** A rectangular region of interest and superclass for the other ROI classes. */
public class Roi extends Object implements Cloneable, java.io.Serializable {

	public static final int CONSTRUCTING=0, MOVING=1, RESIZING=2, NORMAL=3, MOVING_HANDLE=4; // States
	public static final int RECTANGLE=0, OVAL=1, POLYGON=2, FREEROI=3, TRACED_ROI=4, LINE=5, 
		POLYLINE=6, FREELINE=7, ANGLE=8, COMPOSITE=9, POINT=10; // Types
	public static final int HANDLE_SIZE = 5; 
	public static final int NOT_PASTING = -1; 
	
	static final int NO_MODS=0, ADD_TO_ROI=1, SUBTRACT_FROM_ROI=2; // modification states
		
	int startX, startY, x, y, width, height;
	int activeHandle;
	int state;
	int modState = NO_MODS;
	int arcSize;
	
	public static Roi previousRoi;
	public static final BasicStroke onePixelWide = new BasicStroke(1);
	protected static Color ROIColor = Prefs.getColor(Prefs.ROICOLOR,Color.yellow);
	protected static int pasteMode = Blitter.COPY;
	protected static int lineWidth = 1;
	protected static Color defaultFillColor;
	
	protected int type;
	protected int xMax, yMax;
	protected ImagePlus imp;
	private int imageID;
	protected ImageCanvas ic;
	protected int oldX, oldY, oldWidth, oldHeight;
	protected int clipX, clipY, clipWidth, clipHeight;
	protected ImagePlus clipboard;
	protected boolean constrain; // to be square
	protected boolean center;
	protected boolean aspect;
	protected boolean updateFullWindow;
	protected double mag = 1.0;
	protected double asp_bk; //saves aspect ratio if resizing takes roi very small
	protected String name;
	protected ImageProcessor cachedMask;
	protected Color handleColor = Color.white;
	protected Color  strokeColor;
	protected Color instanceColor; //obsolete; replaced by  strokeColor
	protected Color fillColor;
	protected BasicStroke stroke;
	protected boolean nonScalable;
	protected boolean overlay;
	protected boolean wideLine;
	protected boolean hide;



	/** Creates a new rectangular Roi. */
	public Roi(int x, int y, int width, int height) {
		this(x, y, width, height, 0);
	}

	/** Creates a new rounded rectangular Roi. */
	public Roi(int x, int y, int width, int height, int arcSize) {
		setImage(null);
		if (width<1) width = 1;
		if (height<1) height = 1;
		if (width>xMax) width = xMax;
		if (height>yMax) height = yMax;
		this.arcSize = arcSize;
		//setLocation(x, y);
		this.x = x;
		this.y = y;
		startX = x; startY = y;
		oldX = x; oldY = y; oldWidth=0; oldHeight=0;
		this.width = width;
		this.height = height;
		oldWidth=width;
		oldHeight=height;
		clipX = x;
		clipY = y;
		clipWidth = width;
		clipHeight = height;
		state = NORMAL;
		type = RECTANGLE;
		if (ic!=null) {
			Graphics g = ic.getGraphics();
			draw(g);
			g.dispose();
		}
		fillColor = defaultFillColor;
	}
	
	/** Creates a new rectangular Roi. */
	public Roi(Rectangle r) {
		this(r.x, r.y, r.width, r.height);
	}

	/** Starts the process of creating a user-defined rectangular Roi,
		where sx and sy are the starting screen coordinates. */
	public Roi(int sx, int sy, ImagePlus imp) {
		this(sx, sy, imp, 0);
	}
	
	/** Starts the process of creating a user-defined rectangular Roi,
		where sx and sy are the starting screen coordinates. */
	public Roi(int sx, int sy, ImagePlus imp, int arcSize) {
		setImage(imp);
		int ox=sx, oy=sy;
		if (ic!=null) {
			ox = ic.offScreenX(sx);
			oy = ic.offScreenY(sy);
		}
		setLocation(ox, oy);
		this.arcSize = arcSize;
		width = 0;
		height = 0;
		state = CONSTRUCTING;
		type = RECTANGLE;
		if (isDrawingTool()) {
			setStrokeColor(Toolbar.getForegroundColor());
			if (!(this instanceof TextRoi)) {
				double mag = imp!=null&&imp.getCanvas()!=null?imp.getCanvas().getMagnification():1.0; 
				if (mag>1.0) mag = 1.0;
				if (Line.getWidth()==1 && !Line.widthChanged)
					Line.setWidth((int)(2.0/mag));
				if (mag<1.0 && Line.getWidth()*mag<1.0)
						Line.setWidth((int)(1.0/mag));
				setStrokeWidth(Line.getWidth());
			}
		}
		fillColor = defaultFillColor;
	}

	/** @deprecated */
	public Roi(int x, int y, int width, int height, ImagePlus imp) {
		this(x, y, width, height);
		setImage(imp);
	}

	/** Set the location of the ROI in image coordinates. */
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
		startX = x; startY = y;
		oldX = x; oldY = y; oldWidth=0; oldHeight=0;
	}
	
	public void setImage(ImagePlus imp) {
		this.imp = imp;
		cachedMask = null;
		if (imp==null) {
			ic = null;
			clipboard = null;
			xMax = 99999;
			yMax = 99999;
		} else {
			ic = imp.getCanvas();
			xMax = imp.getWidth();
			yMax = imp.getHeight();
		}
	}
	
	ImagePlus getImage() {
		return imp;
	}
	
	/** Returns the ID of the image associated with this ROI. */
	public int getImageID() {
		return imp!=null?imp.getID():imageID;
	}

	public int getType() {
		return type;
	}
	
	public int getState() {
		return state;
	}
	
	/** Returns the perimeter length. */
	public double getLength() {
		double pw=1.0, ph=1.0;
		if (imp!=null) {
			Calibration cal = imp.getCalibration();
			pw = cal.pixelWidth;
			ph = cal.pixelHeight;
		}
		return 2.0*width*pw+2.0*height*ph;
	}
	
	/** Returns Feret's diameter, the greatest distance between 
		any two points along the ROI boundary. */
	public double getFeretsDiameter() {
		double[] a = getFeretValues();
		return a!=null?a[0]:0.0;
	}

	/** Caculates "Feret" (maximum caliper width), "FeretAngle"
		and "MinFeret" (minimum caliper width), "FeretX" and "FeretY". */	
	public double[] getFeretValues() {
		double min=Double.MAX_VALUE, diameter=0.0, angle=0.0, feretX=0.0, feretY=0.0;
		int p1=0, p2=0;
		double pw=1.0, ph=1.0;
		if (imp!=null) {
			Calibration cal = imp.getCalibration();
			pw = cal.pixelWidth;
			ph = cal.pixelHeight;
		}
		Polygon poly = getConvexHull();
		if (poly==null) {
			poly = getPolygon();
			if (poly==null) return null;
		}
		double w2=pw*pw, h2=ph*ph;
		double dx, dy, d;
		for (int i=0; i<poly.npoints; i++) {
			for (int j=i; j<poly.npoints; j++) {
				dx = poly.xpoints[i] - poly.xpoints[j];
				dy = poly.ypoints[i] - poly.ypoints[j];
				d = Math.sqrt(dx*dx*w2 + dy*dy*h2);
				if (d>diameter) {diameter=d; p1=i; p2=j;}
			}
		}
		Rectangle r = getBounds();
		double cx = r.x + r.width/2.0;
		double cy = r.y + r.height/2.0;
		int n = poly.npoints;
		double[] x = new double[n];
		double[] y = new double[n];
		for (int i=0; i<n; i++) {
			x[i] = (poly.xpoints[i]-cx)*pw;
			y[i] = (poly.ypoints[i]-cy)*ph;
		}
		double xr, yr;
		for (double a=0; a<=90; a+=0.5) { // rotate calipers in 0.5 degree increments
			double cos = Math.cos(a*Math.PI/180.0);
			double sin = Math.sin(a*Math.PI/180.0);
			double xmin=Double.MAX_VALUE, ymin=Double.MAX_VALUE;
			double xmax=-Double.MAX_VALUE, ymax=-Double.MAX_VALUE;
			for (int i=0; i<n; i++) {
				xr = cos*x[i] - sin*y[i];
				yr = sin*x[i] + cos*y[i];
				if (xr<xmin) xmin = xr;
				if (xr>xmax) xmax = xr;
				if (yr<ymin) ymin = yr;
				if (yr>ymax) ymax = yr;
			}
			double width = xmax - xmin;
			double height = ymax - ymin;
			double min2 = Math.min(width, height);
			min = Math.min(min, min2);
		}
		double x1=poly.xpoints[p1], y1=poly.ypoints[p1];
		double x2=poly.xpoints[p2], y2=poly.ypoints[p2];
		if (x1>x2) {
			double tx1=x1, ty1=y1;
			x1=x2; y1=y2; x2=tx1; y2=ty1;
		}
		feretX = x1*pw;
		feretY = y1*ph;
		dx=x2-x1; dy=y1-y2;
		angle = (180.0/Math.PI)*Math.atan2(dy*ph, dx*pw);
		if (angle<0) angle = 180.0 + angle;
		//breadth = getFeretBreadth(poly, angle, x1, y1, x2, y2);
		double[] a = new double[5];
		a[0] = diameter;
		a[1] = angle;
		a[2] = min;
		a[3] = feretX;
		a[4] = feretY;
		return a;
	}
	
	public Polygon getConvexHull() {
		return getPolygon();
	}
	
	double getFeretBreadth(Shape shape, double angle, double x1, double y1, double x2, double y2) {
		double cx = x1 + (x2-x1)/2;
		double cy = y1 + (y2-y1)/2;
		AffineTransform at = new AffineTransform();
		at.rotate(angle*Math.PI/180.0, cx, cy);
		Shape s = at.createTransformedShape(shape);
		Rectangle2D r = s.getBounds2D();
		return Math.min(r.getWidth(), r.getHeight());
		/*
		ShapeRoi roi2 = new ShapeRoi(s);
		Roi[] rois = roi2.getRois();
		if (rois!=null && rois.length>0) {
			Polygon p = rois[0].getPolygon();
			ImageProcessor ip = imp.getProcessor();
			for (int i=0; i<p.npoints-1; i++)
				ip.drawLine(p.xpoints[i], p.ypoints[i], p.xpoints[i+1], p.ypoints[i+1]);
			imp.updateAndDraw();
		}
		*/
	}

	/** Return this selection's bounding rectangle. */
	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
	
	/**
	* @deprecated
	* replaced by getBounds()
	*/
	public Rectangle getBoundingRect() {
		return getBounds();
	}

	/** Returns the outline of this selection as a Polygon, or 
		null if this is a straight line selection. 
		@see ij.process.ImageProcessor#setRoi
		@see ij.process.ImageProcessor#drawPolygon
		@see ij.process.ImageProcessor#fillPolygon
	*/
	public Polygon getPolygon() {
		int[] xpoints = new int[4];
		int[] ypoints = new int[4];
		xpoints[0] = x;
		ypoints[0] = y;
		xpoints[1] = x+width;
		ypoints[1] = y;
		xpoints[2] = x+width;
		ypoints[2] = y+height;
		xpoints[3] = x;
		ypoints[3] = y+height;
		return new Polygon(xpoints, ypoints, 4);
	}

	public FloatPolygon getFloatPolygon() {
		return null;
	}
	
	/** Returns a copy of this roi. See Thinking is Java by Bruce Eckel
		(www.eckelobjects.com) for a good description of object cloning. */
	public synchronized Object clone() {
		try { 
			Roi r = (Roi)super.clone();
			r.setImage(null);
			r.setStroke(getStroke());
			r.setFillColor(getFillColor());
			r.imageID = getImageID();
			return r;
		}
		catch (CloneNotSupportedException e) {return null;}
	}
	
	protected void grow(int sx, int sy) {
		if (clipboard!=null) return;
		int xNew = ic.offScreenX(sx);
		int yNew = ic.offScreenY(sy);
		if (type==RECTANGLE) {
			if (xNew < 0) xNew = 0;
			if (yNew < 0) yNew = 0;
		}
		if (constrain) {
			// constrain selection to be square
			if (!center)
				{growConstrained(xNew, yNew); return;}
			int dx, dy, d;
			dx = xNew - x;
			dy = yNew - y;
			if (dx<dy)
				d = dx;
			else
				d = dy;
			xNew = x + d;
			yNew = y + d;
		}
		if (center) {
			width = Math.abs(xNew - startX)*2;
			height = Math.abs(yNew - startY)*2;
			x = startX - width/2;
			y = startY - height/2;
		} else {
			width = Math.abs(xNew - startX);
			height = Math.abs(yNew - startY);
			x = (xNew>=startX)?startX:startX - width;
			y = (yNew>=startY)?startY:startY - height;
			if (type==RECTANGLE) {
				if ((x+width) > xMax) width = xMax-x;
				if ((y+height) > yMax) height = yMax-y;
			}
		}
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX = x;
		oldY = y;
		oldWidth = width;
		oldHeight = height;
	}

	private void growConstrained(int xNew, int yNew) {
		int dx = xNew - startX;
		int dy = yNew - startY;
		width = height = (int)Math.round(Math.sqrt(dx*dx + dy*dy));
		if (type==RECTANGLE) {
			x = (xNew>=startX)?startX:startX - width;
			y = (yNew>=startY)?startY:startY - height;
			if (x<0) x = 0;
			if (y<0) y = 0;
			if ((x+width) > xMax) width = xMax-x;
			if ((y+height) > yMax) height = yMax-y;
		} else {
			x = startX + dx/2 - width/2;
			y = startY + dy/2 - height/2;
		}
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX = x;
		oldY = y;
		oldWidth = width;
		oldHeight = height;
	}

	protected void moveHandle(int sx, int sy) {
		double asp;
		if (clipboard!=null) return;
		int ox = ic.offScreenX(sx);
		int oy = ic.offScreenY(sy);
		if (ox<0) ox=0; if (oy<0) oy=0;
		if (ox>xMax) ox=xMax; if (oy>yMax) oy=yMax;
		//IJ.log("moveHandle: "+activeHandle+" "+ox+" "+oy);
		int x1=x, y1=y, x2=x1+width, y2=y+height, xc=x+width/2, yc=y+height/2;
		if (width > 7 && height > 7) {
			asp = (double)width/(double)height;
			asp_bk = asp;
		} else {
			asp = asp_bk;
		}
		
		switch (activeHandle) {
			case 0:
				x=ox; y=oy;
				break;
			case 1:
				y=oy;
				break;
			case 2:
				x2=ox; y=oy;
				break;
			case 3:
				x2=ox;
				break;
			case 4:
				x2=ox; y2=oy;
				break;
			case 5:
				y2=oy;
				break;
			case 6:
				x=ox; y2=oy;
				break;
			case 7:
				x=ox;
				break;
		}
		if (x<x2)
		   width=x2-x;
		else
		  {width=1; x=x2;}
		if (y<y2)
		   height = y2-y;
		else
		   {height=1; y=y2;}
		
		if (center) {
			switch(activeHandle){
				case 0:
					width=(xc-x)*2;
					height=(yc-y)*2;
					break;
				case 1:
					height=(yc-y)*2;
					break;
				case 2:
					width=(x2-xc)*2;
					x=x2-width;
					height=(yc-y)*2;
					break;
				case 3:
					width=(x2-xc)*2;
					x=x2-width;
					break;
				case 4:
					width=(x2-xc)*2;
					x=x2-width;
					height=(y2-yc)*2;
					y=y2-height;
					break;
				case 5:
					height=(y2-yc)*2;
					y=y2-height;
					break;
				case 6:
					width=(xc-x)*2;
					height=(y2-yc)*2;
					y=y2-height;
					break;
				case 7:
					width=(xc-x)*2;
					break;
			}
			if (x>=x2) {
				width=1;
				x=x2=xc;
			}
			if (y>=y2) {
				height=1;
				y=y2=yc;
			}

		}
		
		if (constrain) {
			if (activeHandle==1 || activeHandle==5)
				width=height;
			else
				height=width;

			if(x>=x2) {
				width=1;
				x=x2=xc;
			}
			if (y>=y2) {
				height=1;
				y=y2=yc;
			}
			switch(activeHandle){
				case 0:
					x=x2-width;
					y=y2-height;
					break;
				case 1:
					x=xc-width/2;
					y=y2-height;
					break;
				case 2:
					y=y2-height;
					break;
				case 3:
					y=yc-height/2;
					break;
				case 5:
					x=xc-width/2;
					break;
				case 6:
					x=x2-width;
					break;
				case 7:
					y=yc-height/2;
					x=x2-width;
					break;
			}
			if (center){
				x=xc-width/2;
				y=yc-height/2;
			}
		}

		if (aspect && !constrain) {
			if (activeHandle==1 || activeHandle==5) width=(int)Math.rint((double)height*asp);
			else height=(int)Math.rint((double)width/asp);
			
			switch(activeHandle){
				case 0:
					x=x2-width;
					y=y2-height;
					break;
				case 1:
					x=xc-width/2;
					y=y2-height;
					break;
				case 2:
					y=y2-height;
					break;
				case 3:
					y=yc-height/2;
					break;
				case 5:
					x=xc-width/2;
					break;
				case 6:
					x=x2-width;
					break;
				case 7:
					y=yc-height/2;
					x=x2-width;
					break;
			}
			if (center){
				x=xc-width/2;
				y=yc-height/2;
			}
			
			// Attempt to preserve aspect ratio when roi very small:
			if (width<8) {
				if(width<1) width = 1;
				height=(int)Math.rint((double)width/asp_bk);
			}
			if (height<8) {
				if(height<1) height =1;
				width=(int)Math.rint((double)height*asp_bk);
			}
			
		}
		
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX=x; oldY=y;
		oldWidth=width; oldHeight=height;
	}

	void move(int sx, int sy) {
		int xNew = ic.offScreenX(sx);
		int yNew = ic.offScreenY(sy);
		x += xNew - startX;
		y += yNew - startY;
		if (clipboard==null && type==RECTANGLE) {
			if (x<0) x=0; if (y<0) y=0;
			if ((x+width)>xMax) x = xMax-width;
			if ((y+height)>yMax) y = yMax-height;
		}
		startX = xNew;
		startY = yNew;
		updateClipRect();
		if (lineWidth>1 && isLine())
			imp.draw();
		else
			imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX = x;
		oldY = y;
		oldWidth = width;
		oldHeight=height;
	}

	/** Nudge ROI one pixel on arrow key press. */
	public void nudge(int key) {
		switch(key) {
			case KeyEvent.VK_UP:
				y--;
				if (y<0 && (type!=RECTANGLE||clipboard==null))
					y = 0;
				break;
			case KeyEvent.VK_DOWN:
				y++;
				if ((y+height)>=yMax && (type!=RECTANGLE||clipboard==null))
					y = yMax-height;
				break;
			case KeyEvent.VK_LEFT:
				x--;
				if (x<0 && (type!=RECTANGLE||clipboard==null))
					x = 0;
				break;
			case KeyEvent.VK_RIGHT:
				x++;
				if ((x+width)>=xMax && (type!=RECTANGLE||clipboard==null))
					x = xMax-width;
				break;
		}
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX = x; oldY = y;
		showStatus();
	}
	
	/** Nudge lower right corner of rectangular and oval ROIs by
		one pixel based on arrow key press. */
	public void nudgeCorner(int key) {
		if (type>OVAL || clipboard!=null)
			return;
		switch(key) {
			case KeyEvent.VK_UP:
				height--;
				if (height<1) height = 1;
				break;
			case KeyEvent.VK_DOWN:
				height++;
				if ((y+height) > yMax) height = yMax-y;
				break;
			case KeyEvent.VK_LEFT:
				width--;
				if (width<1) width = 1;
				break;
			case KeyEvent.VK_RIGHT:
				width++;
				if ((x+width) > xMax) width = xMax-x;
				break;
		}
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX = x; oldY = y;
		cachedMask = null;
		showStatus();
	}
	
	protected void updateClipRect() {
	// Finds the union of current and previous roi
		clipX = (x<=oldX)?x:oldX;
		clipY = (y<=oldY)?y:oldY;
		clipWidth = ((x+width>=oldX+oldWidth)?x+width:oldX+oldWidth) - clipX + 1;
		clipHeight = ((y+height>=oldY+oldHeight)?y+height:oldY+oldHeight) - clipY + 1;
		int m = 3;
		if (ic!=null) {
			double mag = ic.getMagnification();
			if (mag<1.0)
				m = (int)(4.0/mag);
		}
		m += clipRectMargin();
		m = (int)(m+getStrokeWidth()*2);
		clipX-=m; clipY-=m;
		clipWidth+=m*2; clipHeight+=m*2;
		//if (IJ.debugMode) IJ.log("updateClipRect: "+m+"  "+clipX+" "+clipY+" "+clipWidth+" "+clipHeight);
	 }
	 
	protected int clipRectMargin() {
		return 0;
	}
		
	protected void handleMouseDrag(int sx, int sy, int flags) {
		if (ic==null) return;
		constrain = (flags&Event.SHIFT_MASK)!=0;
		center = (flags&Event.CTRL_MASK)!=0 || (IJ.isMacintosh()&&(flags&Event.META_MASK)!=0);
		aspect = (flags&Event.ALT_MASK)!=0;
		switch(state) {
			case CONSTRUCTING:
				grow(sx, sy);
				break;
			case MOVING:
				move(sx, sy);
				break;
			case MOVING_HANDLE:
				moveHandle(sx, sy);
				break;
			default:
				break;
		}
	}

	int getHandleSize() {
		double mag = ic!=null?ic.getMagnification():1.0;
		double size = HANDLE_SIZE/mag;
		return (int)(size*mag);
	}
	
	public void draw(Graphics g) {
		if (ic==null) return;
		Color color =  strokeColor!=null? strokeColor:ROIColor;
		if (fillColor!=null) color = fillColor;
		if (Interpreter.isBatchMode() && ic.getDisplayList()!=null && strokeColor==null && fillColor==null)
			return;
		g.setColor(color);
		mag = ic.getMagnification();
		int sw = (int)(width*mag);
		int sh = (int)(height*mag);
		int sx1 = ic.screenX(x);
		int sy1 = ic.screenY(y);
		int sx2 = sx1+sw/2;
		int sy2 = sy1+sh/2;
		int sx3 = sx1+sw;
		int sy3 = sy1+sh;
		Graphics2D g2d = (Graphics2D)g;
		if (stroke!=null)
			g2d.setStroke(getScaledStroke());
		if (arcSize>0) {
			int sArcSize = (int)Math.round(arcSize*mag);
			if (fillColor!=null)
				g.fillRoundRect(sx1, sy1, sw, sh, sArcSize, sArcSize);
			else
				g.drawRoundRect(sx1, sy1, sw, sh, sArcSize, sArcSize);
		} else {
			if (fillColor!=null)
				g.fillRect(sx1, sy1, sw, sh);
			else
				g.drawRect(sx1, sy1, sw, sh);
		}
		if (state!=CONSTRUCTING && clipboard==null && !overlay) {
			int size2 = HANDLE_SIZE/2;
			drawHandle(g, sx1-size2, sy1-size2);
			drawHandle(g, sx2-size2, sy1-size2);
			drawHandle(g, sx3-size2, sy1-size2);
			drawHandle(g, sx3-size2, sy2-size2);
			drawHandle(g, sx3-size2, sy3-size2);
			drawHandle(g, sx2-size2, sy3-size2);
			drawHandle(g, sx1-size2, sy3-size2);
			drawHandle(g, sx1-size2, sy2-size2);
		}
		drawPreviousRoi(g);
		if (state!=NORMAL) showStatus();
		if (updateFullWindow)
			{updateFullWindow = false; imp.draw();}
	}
	
	public void drawOverlay(Graphics g) {
		overlay = true;
		draw(g);
		overlay = false;
	}
	
	void drawPreviousRoi(Graphics g) {
		if (previousRoi!=null && previousRoi!=this && previousRoi.modState!=NO_MODS) {
			if (type!=POINT && previousRoi.getType()==POINT && previousRoi.modState!=SUBTRACT_FROM_ROI)
				return;
			previousRoi.setImage(imp);
			previousRoi.draw(g);
		}		
	}
	
	void drawHandle(Graphics g, int x, int y) {
		double size = (width*height)*mag*mag;
		if (type==LINE) {
			size = Math.sqrt(width*width+height*height);
			size *= size*mag*mag;
		}
		if (size>4000.0) {
			g.setColor(Color.black);
			g.fillRect(x,y,5,5);
			g.setColor(handleColor);
			g.fillRect(x+1,y+1,3,3);
		} else if (size>1000.0) {
			g.setColor(Color.black);
			g.fillRect(x+1,y+1,4,4);
			g.setColor(handleColor);
			g.fillRect(x+2,y+2,2,2);
		} else {			
			g.setColor(Color.black);
			g.fillRect(x+1,y+1,3,3);
			g.setColor(handleColor);
			g.fillRect(x+2,y+2,1,1);
		}
	}

	/**
	* @deprecated
	* replaced by drawPixels(ImageProcessor)
	*/
	public void drawPixels() {
		if (imp!=null)
			drawPixels(imp.getProcessor());	
	}

	/** Draws the selection outline on the specified ImageProcessor.
		@see ij.process.ImageProcessor#setColor
		@see ij.process.ImageProcessor#setLineWidth
	*/
	public void drawPixels(ImageProcessor ip) {
		endPaste();
		if (arcSize>0)
			(new ShapeRoi(new RoundRectangle2D.Float(x, y, width, height, arcSize, arcSize))).drawPixels(ip);
		else {
			int saveWidth = ip.getLineWidth();
			if (getStrokeWidth()>1f)
				ip.setLineWidth((int)Math.round(getStrokeWidth()));
			if (ip.getLineWidth()==1)
				ip.drawRect(x, y, width+1, height+1);
			else
				ip.drawRect(x, y, width, height);
			ip.setLineWidth(saveWidth);
		}
		if (Line.getWidth()>1 || getStrokeWidth()>1)
			updateFullWindow = true;
	}
	
	public boolean contains(int x, int y) {
		Rectangle r = new Rectangle(this.x, this.y, width, height);
		boolean contains = r.contains(x, y);
		if (arcSize==0 || contains==false)
			return contains;
		RoundRectangle2D rr = new RoundRectangle2D.Float(this.x, this.y, width, height, arcSize, arcSize);
		return rr.contains(x, y);
	}
		
	/** Returns a handle number if the specified screen coordinates are  
		inside or near a handle, otherwise returns -1. */
	public int isHandle(int sx, int sy) {
		if (clipboard!=null || ic==null) return -1;
		double mag = ic.getMagnification();
		int size = HANDLE_SIZE+3;
		int halfSize = size/2;
		int sx1 = ic.screenX(x) - halfSize;
		int sy1 = ic.screenY(y) - halfSize;
		int sx3 = ic.screenX(x+width) - halfSize;
		int sy3 = ic.screenY(y+height) - halfSize;
		int sx2 = sx1 + (sx3 - sx1)/2;
		int sy2 = sy1 + (sy3 - sy1)/2;
		if (sx>=sx1&&sx<=sx1+size&&sy>=sy1&&sy<=sy1+size) return 0;
		if (sx>=sx2&&sx<=sx2+size&&sy>=sy1&&sy<=sy1+size) return 1;
		if (sx>=sx3&&sx<=sx3+size&&sy>=sy1&&sy<=sy1+size) return 2;
		if (sx>=sx3&&sx<=sx3+size&&sy>=sy2&&sy<=sy2+size) return 3;
		if (sx>=sx3&&sx<=sx3+size&&sy>=sy3&&sy<=sy3+size) return 4;
		if (sx>=sx2&&sx<=sx2+size&&sy>=sy3&&sy<=sy3+size) return 5;
		if (sx>=sx1&&sx<=sx1+size&&sy>=sy3&&sy<=sy3+size) return 6;
		if (sx>=sx1&&sx<=sx1+size&&sy>=sy2&&sy<=sy2+size) return 7;
		return -1;
	}
	
	protected void mouseDownInHandle(int handle, int sx, int sy) {
		state = MOVING_HANDLE;
		activeHandle = handle;
	}

	protected void handleMouseDown(int sx, int sy) {
		if (state==NORMAL && ic!=null) {
			state = MOVING;
			startX = ic.offScreenX(sx);
			startY = ic.offScreenY(sy);
			showStatus();
		}
	}
		
	protected void handleMouseUp(int screenX, int screenY) {
		state = NORMAL;
		if (imp==null) return;
		imp.draw(clipX-5, clipY-5, clipWidth+10, clipHeight+10);
		if (Recorder.record) {
			String method;
			if (type==LINE) {
				Line line = (Line)imp.getRoi();
				Recorder.record("makeLine", line.x1, line.y1, line.x2, line.y2);
			} else if (type==OVAL)
				Recorder.record("makeOval", x, y, width, height);
			else if (!(this instanceof TextRoi)) {
				if (arcSize==0)
					Recorder.record("makeRectangle", x, y, width, height);
				else {
					if (Recorder.scriptMode())
						Recorder.recordCall("imp.setRoi(new Roi("+x+", "+y+", "+width+", "+height+", "+arcSize+"));");
					else
						Recorder.record("makeRectangle", x, y, width, height, arcSize);
				}
			}
		}
		if (Toolbar.getToolId()==Toolbar.OVAL&&Toolbar.getBrushSize()>0)  {
			int flags = ic!=null?ic.getModifiers():16;
			if ((flags&16)==0) // erase ROI Brush
				{imp.draw(); return;}
		}
		modifyRoi();
	}

    void modifyRoi() {
    	if (previousRoi==null || previousRoi.modState==NO_MODS || imp==null)
    		return;
		//IJ.log("modifyRoi: "+ type+"  "+modState+" "+previousRoi.type+"  "+previousRoi.modState);
    	if (type==POINT || previousRoi.getType()==POINT) {
    		if (type==POINT && previousRoi.getType()==POINT)
    			addPoint();
    		else if (isArea() && previousRoi.getType()==POINT && previousRoi.modState==SUBTRACT_FROM_ROI)
    			subtractPoints();
    		return;
    	}
		Roi previous = (Roi)previousRoi.clone();
		previous.modState = NO_MODS;
        ShapeRoi s1  = null;
        ShapeRoi s2 = null;
        if (previousRoi instanceof ShapeRoi)
            s1 = (ShapeRoi)previousRoi;
        else
            s1 = new ShapeRoi(previousRoi);
        if (this instanceof ShapeRoi)
            s2 = (ShapeRoi)this;
        else
            s2 = new ShapeRoi(this);
        if (previousRoi.modState==ADD_TO_ROI)
        	s1.or(s2);
        else
        	s1.not(s2);
		previousRoi.modState = NO_MODS;
		Roi[] rois = s1.getRois();
		if (rois.length==0) return;
		int type2 = rois[0].getType();
		//IJ.log(rois.length+" "+type2);
		Roi roi2 = null;
		if (rois.length==1 && (type2==POLYGON||type2==FREEROI))
			roi2 = rois[0];
		else
			roi2 = s1;
		if (roi2!=null)
			roi2.copyAttributes(previousRoi);
		imp.setRoi(roi2);
		previousRoi = previous;
    }
    
    void addPoint() {
		if (!(type==POINT && previousRoi.getType()==POINT)) {
			modState = NO_MODS;
			imp.draw();
			return;
		}
		previousRoi.modState = NO_MODS;
		PointRoi p1 = (PointRoi)previousRoi;
		Rectangle r = getBounds();
		imp.setRoi(p1.addPoint(r.x, r.y));
    }
    
    void subtractPoints() {
		previousRoi.modState = NO_MODS;
		PointRoi p1 = (PointRoi)previousRoi;
		PointRoi p2 = p1.subtractPoints(this);
		if (p2!=null)
			imp.setRoi(p1.subtractPoints(this));
		else
			imp.killRoi();
    }

    /** If 'add' is true, adds this selection to the previous one. If 'subtract' is true, subtracts 
    	it from the previous selection. Called by the IJ.doWand() method, and the makeRectangle(), 
    	makeOval(), makePolygon() and makeSelection() macro functions. */
    public void update(boolean add, boolean subtract) {
     	if (previousRoi==null) return;
    	if (add) {
			previousRoi.modState = ADD_TO_ROI;
   			modifyRoi();
		} else if (subtract) {
			previousRoi.modState = SUBTRACT_FROM_ROI;
   			modifyRoi();
		} else
			previousRoi.modState = NO_MODS;
     }

	protected void showStatus() {
		String value;
		if (state!=CONSTRUCTING && (type==RECTANGLE||type==POINT) && width<=25 && height<=25) {
			ImageProcessor ip = imp.getProcessor();
			double v = ip.getPixelValue(x,y);
			int digits = (imp.getType()==ImagePlus.GRAY8||imp.getType()==ImagePlus.GRAY16)?0:2;
			value = ", value="+IJ.d2s(v,digits);
		} else
			value = "";
		Calibration cal = imp.getCalibration();
		String size;
		if (cal.scaled() && !IJ.altKeyDown())
			size = ", w="+IJ.d2s(width*cal.pixelWidth)+", h="+IJ.d2s(height*cal.pixelHeight);
		else
			size = ", w="+width+", h="+height;
		size += ", ar="+IJ.d2s((double)width/height,2);
		IJ.showStatus(imp.getLocationAsString(x,y)+size+value);
	}
		
	/** Always returns null for rectangular Roi's */
	public ImageProcessor getMask() {
		if (arcSize>0)
			return (new ShapeRoi(new RoundRectangle2D.Float(x, y, width, height, arcSize, arcSize))).getMask();
		else
			return null;
	}
	
	public void startPaste(ImagePlus clipboard) {
		IJ.showStatus("Pasting...");
		this.clipboard = clipboard;
		imp.getProcessor().snapshot();
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
	}
	
	void updatePaste() {
		if (clipboard!=null) {
			imp.getMask();
			ImageProcessor ip = imp.getProcessor();
			ip.reset();
			ip.copyBits(clipboard.getProcessor(), x, y, pasteMode);
			if (type!=RECTANGLE)
				ip.reset(ip.getMask());
			if (ic!=null)
				ic.setImageUpdated();
		}
	}

	public void endPaste() {
		if (clipboard!=null) {
			updatePaste();
			clipboard = null;
			Undo.setup(Undo.FILTER, imp);
		}
	}
	
	public void abortPaste() {
		clipboard = null;
		imp.getProcessor().reset();
		imp.updateAndDraw();
	}

	/** Returns the angle in degrees between the specified line and a horizontal line. */
	public double getAngle(int x1, int y1, int x2, int y2) {
		double dx = x2-x1;
		double dy = y1-y2;
		if (imp!=null && !IJ.altKeyDown()) {
			Calibration cal = imp.getCalibration();
			dx *= cal.pixelWidth;
			dy *= cal.pixelHeight;
		}
		return (180.0/Math.PI)*Math.atan2(dy, dx);
	}
	
	/** Sets the default (global) color used for ROI outlines.
	 * @see #getColor()
	 * @see #setStrokeColor(Color)
	 */
	public static void setColor(Color c) {
		ROIColor = c;
	}
	
	/** Returns the default (global) color used for drawing ROI outlines.
	 * @see #setColor(Color)
	 * @see #getStrokeColor()
	 */
	public static Color getColor() {
		return ROIColor;
	}

	/** Sets the color used by this ROI to draw its outline. This color, if not null, 
	 * overrides the global color set by the static setColor() method.
	 * @see #getStrokeColor()
	 * @see #setStrokeWidth(int)
	 * @see ij.ImagePlus#setOverlay(ij.gui.Overlay)
	 */
	public void setStrokeColor(Color c) {
		 strokeColor = c;
	}

	/** Returns the the color used to draw the ROI outline or null if the default color is being used.
	 * @see #setStrokeColor(Color)
	 */
	public Color getStrokeColor() {
		return  strokeColor;
	}

	/** Sets the color used to fill ROIs when they are in an overlay.
	 * @see ij.ImagePlus#setOverlay(ij.gui.Overlay)
	 */
	public void setFillColor(Color color) {
		fillColor = color;
	}

	/** Returns the the color used to fill this ROI when it is in a display, or null.
	 * @see #getStrokeColor()
	 */
	public Color getFillColor() {
		return fillColor;
	}
	
	public static void setDefaultFillColor(Color color) {
		defaultFillColor = color;
	}
	
	public static Color getDefaultFillColor() {
		return defaultFillColor;
	}
	
	/** Copy the attributes (outline color, fill color, outline width) 
		of  'roi2' to the this selection. */
	public void copyAttributes(Roi roi2) {
		this. strokeColor = roi2. strokeColor;
		this.fillColor = roi2.fillColor;
		this.stroke = roi2.stroke;
	}

	/**
	* @deprecated
	* replaced by setStrokeColor()
	*/
	public void setInstanceColor(Color c) {
		 strokeColor = c;
	}

	/**
	* @deprecated
	* replaced by setStrokeWidth(int)
	*/
	public void setLineWidth(int width) {
		setStrokeWidth(width) ;
	}
        
	public void updateWideLine(float width) {
		//IJ.log("updateWideLine "+isLine()+"  "+isDrawingTool()+"  "+getType());
		if (isLine()) {
			wideLine = true;
			setStrokeWidth(width);
			if (getStrokeColor()==null) {
				Color c = getColor();
				setStrokeColor(new Color(c.getRed(),c.getGreen(),c.getBlue(), 77));
			}
		}
	}

    /** Set 'nonScalable' true to have TextRois in a display 
		list drawn at a fixed location  and size. */
	public void setNonScalable(boolean nonScalable) {
		this.nonScalable = nonScalable;
	}

	/** Sets the width of the lines used to draw this ROI when
	 * it is part of an Overlay list or ROI Manager "Show All" list.
	 * @see #setStrokeColor(Color)
	 * @see ij.ImagePlus#setOverlay(ij.gui.Overlay)
	 */
	public void setStrokeWidth(float width) {
		//this.stroke = new BasicStroke(width);
		if (wideLine)
			this.stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
		else
			this.stroke = new BasicStroke(width);
		if (width>1f) fillColor = null;
	}

	/** Returns the lineWidth. */
	public float getStrokeWidth() {
		return stroke!=null?stroke.getLineWidth():1;
	}

	/** Sets the Stroke used to draw this ROI. */
	public void setStroke(BasicStroke stroke) {
		this.stroke = stroke;
	}
	
	/** Returns the Stroke used to draw this ROI, or null if no Stroke is used. */
	public BasicStroke getStroke() {
		return stroke;
	}
	
	protected BasicStroke getScaledStroke() {
		if (ic==null) return stroke;
		double mag = ic.getMagnification();
		if (mag!=1.0) {
			float width = stroke.getLineWidth();
			return new BasicStroke((float)(width*mag), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
		} else
			return stroke;
	}

	/** Returns the name of this ROI, or null. */
	public String getName() {
		return name;
	}

	/** Sets the name of this ROI. */
	public void setName(String name) {
		this.name = name;
	}

	/** Sets the Paste transfer mode.
		@see ij.process.Blitter
	*/
	public static void setPasteMode(int transferMode) {
		if (transferMode==pasteMode) return;
		pasteMode = transferMode;
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null)
			imp.updateAndDraw();
	}
	
	/** Sets the rounded rectangle arc size (pixels). */
	public void setRoundRectArcSize(int size) {
		arcSize = size;
		if (arcSize<0) arcSize = 0;
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null && this==imp.getRoi())
			imp.updateAndDraw();
	}

	public int getRoundRectArcSize() {
		return arcSize;
	}

	/** Returns the current paste transfer mode, or NOT_PASTING (-1)
		if no paste operation is in progress.
		@see ij.process.Blitter
	*/
	public int getPasteMode() {
		if (clipboard==null)
			return NOT_PASTING;
		else
			return pasteMode;
	}

	/** Returns the current paste transfer mode. */
	public static int getCurrentPasteMode() {
		return pasteMode;
	}
	
	/** Returns 'true' if this is an area selection. */
	public boolean isArea() {
		return (type>=RECTANGLE && type<=TRACED_ROI) || type==COMPOSITE;
	}

	/** Returns 'true' if this is a line selection. */
    public boolean isLine() {
        return type>=LINE && type<=FREELINE;
    }
    
	/** Returns 'true' if this is an ROI primarily used from drawing
		(e.g., Rounded Rectangle, TextRoi or Arrow). */
    public boolean isDrawingTool() {
        return arcSize>0;
    }

	/** Convenience method that converts Roi type to a human-readable form. */
	public String getTypeAsString() {
		String s="";
		switch(type) {
			case POLYGON: s="Polygon"; break;
			case FREEROI: s="Freehand"; break;
			case TRACED_ROI: s="Traced"; break;
			case POLYLINE: s="Polyline"; break;
			case FREELINE: s="Freeline"; break;
			case ANGLE: s="Angle"; break;
			case LINE: s="Straight Line"; break;
			case OVAL: s="Oval"; break;
			case COMPOSITE: s = "Composite"; break;
			case POINT: s = "Point"; break;
			default: s="Rectangle"; break;
		}
		return s;
	}
	
	/** Returns true if this ROI is currently displayed on an image. */
	public boolean isVisible() {
		return ic!=null;
	}

    /** Checks whether two rectangles are equal. */
    public boolean equals(Object obj) {
		if (obj instanceof Roi) {
			Roi roi2 = (Roi)obj;
			if (type!=roi2.getType()) return false;
			if (!getBounds().equals(roi2.getBounds())) return false;
			if (getLength()!=roi2.getLength()) return false;
			return true;
		} else
			return false;
    }

	public String toString() {
		return ("Roi["+getTypeAsString()+", x="+x+", y="+y+", width="+width+", height="+height+"]");
	}

	public void temporarilyHide() {
		hide = true;
	}

}
