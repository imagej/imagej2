package ij.gui;

import ijx.gui.IjxImageCanvas;
import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.event.KeyEvent;
import ij.*;
import ij.process.*;
import ij.measure.*;
import ij.plugin.frame.Recorder;

/** A rectangular region of interest and superclass for the other ROI classes. */
public class Roi extends Object implements Cloneable, java.io.Serializable {

	public static final int CONSTRUCTING=0, MOVING=1, RESIZING=2, NORMAL=3, MOVING_HANDLE=4; // States
	public static final int RECTANGLE=0, OVAL=1, POLYGON=2, FREEROI=3, TRACED_ROI=4, LINE=5, 
		POLYLINE=6, FREELINE=7, ANGLE=8, COMPOSITE=9, POINT=10; // Types
	public static final int HANDLE_SIZE = 5; 
	public static final int NOT_PASTING = -1; 
	
	public static final int NO_MODS=0, ADD_TO_ROI=1, SUBTRACT_FROM_ROI=2; // modification states
		
	int startX, startY, x, y, width, height;
	int activeHandle;
	public int state;
	public int modState = NO_MODS;
	
	public static Roi previousRoi;
	protected static Color ROIColor = Prefs.getColor(Prefs.ROICOLOR,Color.yellow);
	protected static int pasteMode = Blitter.COPY;
	protected static int lineWidth = 1;
	
	protected int type;
	protected int xMax, yMax;
	protected IjxImagePlus imp;
	protected IjxImageCanvas ic;
	protected int oldX, oldY, oldWidth, oldHeight;
	protected int clipX, clipY, clipWidth, clipHeight;
	protected IjxImagePlus clipboard;
	protected boolean constrain; // to be square
    protected boolean center;
	protected boolean updateFullWindow;
	protected double mag = 1.0;
	protected String name;
	protected ImageProcessor cachedMask;
	protected Color handleColor = Color.white;
	protected Color instanceColor;
	protected BasicStroke stroke;



	/** Creates a new rectangular Roi. */
	public Roi(int x, int y, int width, int height) {
		setImage(null);
		if (width<1) width = 1;
		if (height<1) height = 1;
		if (width>xMax) width = xMax;
		if (height>yMax) height = yMax;
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
	}
	
	/** Creates a new rectangular Roi. */
	public Roi(Rectangle r) {
		this(r.x, r.y, r.width, r.height);
	}

	/** Starts the process of creating a user-defined rectangular Roi,
		where sx and sy are the starting screen coordinates. */
	public Roi(int sx, int sy, IjxImagePlus imp) {
		setImage(imp);
		int ox=sx, oy=sy;
		if (ic!=null) {
			ox = ic.offScreenX(sx);
			oy = ic.offScreenY(sy);
		}
		setLocation(ox, oy);
		width = 0;
		height = 0;
		state = CONSTRUCTING;
		type = RECTANGLE;
	}
	
	/** Obsolete */
	public Roi(int x, int y, int width, int height, IjxImagePlus imp) {
		this(x, y, width, height);
		setImage(imp);
	}

	public void setLocation(int x, int y) {
		//if (x<0) x = 0;
		//if (y<0) y = 0;
		//if ((x+width)>xMax) x = xMax-width;
		//if ((y+height)>yMax) y = yMax-height;
		//IJ.write(imp.getTitle() + ": Roi.setlocation(" + x + "," + y + ")");
		this.x = x;
		this.y = y;
		startX = x; startY = y;
		oldX = x; oldY = y; oldWidth=0; oldHeight=0;
	}
	
	public void setImage(IjxImagePlus imp) {
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
		double pw=1.0, ph=1.0;
		if (imp!=null) {
			Calibration cal = imp.getCalibration();
			pw = cal.pixelWidth;
			ph = cal.pixelHeight;
		}
		return Math.sqrt(width*width*pw*pw+height*height*ph*ph);
	}

	/** Return this selection's bounding rectangle. */
	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
	
	/** This obsolete method has been replaced by getBounds(). */
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

	protected void moveHandle(int sx, int sy) {
		if (clipboard!=null) return;
		int ox = ic.offScreenX(sx);
		int oy = ic.offScreenY(sy);
		if (ox<0) ox=0; if (oy<0) oy=0;
		if (ox>xMax) ox=xMax; if (oy>yMax) oy=yMax;
		//IJ.log("moveHandle: "+activeHandle+" "+ox+" "+oy);
		int x1=x, y1=y, x2=x1+width, y2=y+height;
		switch (activeHandle) {
			case 0: x=ox; y=oy; break;
			case 1: y=oy; break;
			case 2: x2=ox; y=oy; break;
			case 3: x2=ox; break;			
			case 4: x2=ox; y2=oy; break;
			case 5: y2=oy; break;
			case 6: x=ox; y2=oy; break;
			case 7: x=ox; break;
		}
		if (x<x2)
		   width=x2-x;
		else
		  {width=1; x=x2;}
		if (y<y2)
		   height = y2-y;
		else
		   {height=1; y=y2;}
		if (constrain)
			height = width;
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
		if (type==POINT) m += 4;
		if (ic!=null) {
			double mag = ic.getMagnification();
			if (mag<1.0)
				m = (int)(3/mag);
		}
		clipX-=m; clipY-=m;
		clipWidth+=m*2; clipHeight+=m*2;
	 }
		
	public void handleMouseDrag(int sx, int sy, int flags) {
		if (ic==null) return;
		constrain = (flags&Event.SHIFT_MASK)!=0;
		center = (flags&Event.CTRL_MASK)!=0 || (IJ.isMacintosh()&&(flags&Event.META_MASK)!=0);
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
		g.setColor(instanceColor!=null?instanceColor:ROIColor);
		mag = ic.getMagnification();
		int sw = (int)(width*mag);
		int sh = (int)(height*mag);
		//if (x+width==imp.getWidth()) sw -= 1;
		//if (y+height==imp.getHeight()) sh -= 1;
		int sx1 = ic.screenX(x);
		int sy1 = ic.screenY(y);
		int sx2 = sx1+sw/2;
		int sy2 = sy1+sh/2;
		int sx3 = sx1+sw;
		int sy3 = sy1+sh;
		g.drawRect(sx1, sy1, sw, sh);
		if (state!=CONSTRUCTING && clipboard==null) {
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
	
	void drawPreviousRoi(Graphics g) {
		if (previousRoi!=null && previousRoi!=this && previousRoi.modState!=NO_MODS) {
			if (type!=POINT && previousRoi.getType()==POINT && previousRoi.modState!=SUBTRACT_FROM_ROI)
				return;
			previousRoi.setImage(imp);
			previousRoi.draw(g);
		}		
	}
	
	void drawHandle(Graphics g, int x, int y) {
		double size = (width*height)*mag;
		if (type==LINE) {
			size = Math.sqrt(width*width+height*height);
			size *= size*mag;
		}
		if (size>6000.0) {
			g.setColor(Color.black);
			g.fillRect(x,y,5,5);
			g.setColor(handleColor);
			g.fillRect(x+1,y+1,3,3);
		} else if (size>1500.0) {
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

	/** Obsolete, use drawPixels(ImageProcessor) */
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
		ip.drawRect(x, y, width, height);
		if (Line.getWidth()>1)
			updateFullWindow = true;
	}

	public boolean contains(int x, int y) {
		Rectangle r = new Rectangle(this.x, this.y, width, height);
		return r.contains(x, y);
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
	
	public  void mouseDownInHandle(int handle, int sx, int sy) {
		state = MOVING_HANDLE;
		activeHandle = handle;
	}

	public void handleMouseDown(int sx, int sy) {
		if (state==NORMAL && ic!=null) {
			state = MOVING;
			startX = ic.offScreenX(sx);
			startY = ic.offScreenY(sy);
			showStatus();
		}
	}
		
	public void handleMouseUp(int screenX, int screenY) {
		state = NORMAL;
		imp.draw(clipX-5, clipY-5, clipWidth+10, clipHeight+10);
		if (Recorder.record) {
			String method;
			if (type==LINE) {
				if (imp==null) return;
				Line line = (Line)imp.getRoi();
				Recorder.record("makeLine", line.x1, line.y1, line.x2, line.y2);
			} else if (type==OVAL)
				Recorder.record("makeOval", x, y, width, height);
			else if (!(this instanceof TextRoi))
				Recorder.record("makeRectangle", x, y, width, height);
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
			roi2.setName(previousRoi.getName());
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
		IJ.showStatus(imp.getLocationAsString(x,y)+size+value);
	}
		
	/** Always returns null for rectangular Roi's */
	public ImageProcessor getMask() {
		return null;
	}
	
	public void startPaste(IjxImagePlus clipboard) {
		IJ.showStatus("Pasting...");
		this.clipboard = clipboard;
		imp.getProcessor().snapshot();
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
	}
	
	public void updatePaste() {
		if (clipboard!=null) {
			imp.getMask();
			ImageProcessor ip = imp.getProcessor();
			ip.reset();
			ip.copyBits(clipboard.getProcessor(), x, y, pasteMode);
			if (type!=RECTANGLE)
				ip.reset(ip.getMask());
			ic.setImageUpdated();
		}
	}

	public void endPaste() {
		if (clipboard!=null) {
			imp.getMask();
			ImageProcessor ip = imp.getProcessor();
			if (pasteMode!=Blitter.COPY) ip.reset();
			ip.copyBits(clipboard.getProcessor(), x, y, pasteMode);
			if (type!=RECTANGLE)
				ip.reset(ip.getMask());
			ip.snapshot();
			clipboard = null;
			imp.updateAndDraw();
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
	
	/** Returns the color used for drawing ROI outlines. */
	public static Color getColor() {
		return ROIColor;
	}

	/** Sets the color used for ROI outlines to the specified value. */
	public static void setColor(Color c) {
		ROIColor = c;
	}
	
	/** Sets the color used by this ROI to draw its outline. This color, if not null, 
		overrides the global color set by the static setColor() method. */
	public void setInstanceColor(Color c) {
		instanceColor = c;
	}

	/** Returns the the color used to draw the ROI outline or null if the default color is being used. */
	public Color getInstanceColor() {
		return instanceColor;
	}

	/** Sets the width of lines used to draw composite ROIs. */
	public void setLineWidth(int width) {
		this.stroke = new BasicStroke(width);
	}

	/** Sets the Stroke used to draw composite ROIs. */
	public void setStroke(BasicStroke stroke) {
		this.stroke = stroke;
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
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null)
			imp.updateAndDraw();
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
	
	/** Returns true if this is an area selection. */
	public boolean isArea() {
		return (type>=RECTANGLE && type<=TRACED_ROI) || type==COMPOSITE;
	}

	/** Returns true if this is a line selection. */
    public boolean isLine() {
        return type>=LINE && type<=FREELINE;
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

}
