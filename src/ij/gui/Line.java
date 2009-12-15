package ij.gui;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.measure.*;
import ij.plugin.Straightener;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;


/** This class represents a straight line selection. */
public class Line extends Roi {

	public int x1, y1, x2, y2;	// the line
	public double x1d, y1d, x2d, y2d;	// the line using sub-pixel coordinates
	private double x1R, y1R, x2R, y2R;  // the line, relative to base of bounding rect
	private double xHandleOffset, yHandleOffset;
	private double startxd, startyd;

	/** Creates a new straight line selection using the specified
		starting and ending offscreen integer coordinates. */
	public Line(int ox1, int oy1, int ox2, int oy2) {
		this((double)ox1, (double)oy1, (double)ox2, (double)oy2);
	}

	/** Creates a new straight line selection using the specified
		starting and ending offscreen double coordinates. */
	public Line(double ox1, double oy1, double ox2, double oy2) {
		super((int)ox1, (int)oy1, 0, 0);
		type = LINE;
		x1d=ox1; y1d=oy1; x2d=ox2; y2d=oy2; 
		x1=(int)x1d; y1=(int)y1d; x2=(int)x2d; y2=(int)y2d;
		x=(int)Math.min(x1d,x2d); y=(int)Math.min(y1d,y2d);
		x1R=x1d-x; y1R=y1d-y; x2R=x2d-x; y2R=y2d-y;
		width=(int)Math.abs(x2R-x1R); height=(int)Math.abs(y2R-y1R);
		updateClipRect();
		oldX=x; oldY=y; oldWidth=width; oldHeight=height;
		state = NORMAL;
	}

	/** Starts the process of creating a new user-generated straight line
		selection. 'sx' and 'sy' are screen coordinates that specify
		the start of the line. The user will determine the end of the line
		interactively using rubber banding. */
	public Line(int sx, int sy, IjxImagePlus imp) {
		super(sx, sy, imp);
		startxd = ic.offScreenXD(sx);
		startyd = ic.offScreenYD(sy);
		x1R = x2R = startxd - startX;
		y1R = y2R = startyd - startY;
		type = LINE;
	}

	/** Obsolete */
	public Line(int ox1, int oy1, int ox2, int oy2, IjxImagePlus imp) {
		this(ox1, oy1, ox2, oy2);
		setImage(imp);
	}

	protected void grow(int sx, int sy) {
		double xend = ic!=null?ic.offScreenXD(sx):sx;
		double yend = ic!=null?ic.offScreenYD(sy):sy;
		if (xend<0.0) xend=0.0; if (yend<0.0) yend=0.0;
		if (xend>xMax) xend=xMax; if (yend>yMax) yend=yMax;
		double xstart=x+x1R, ystart=y+y1R;
		if (constrain) {
			double dx = Math.abs(xend-xstart);
			double dy = Math.abs(yend-ystart);
			if (dx>=dy)
				yend = ystart;
			else
				xend = xstart;
		}
		x=(int)Math.min(x+x1R,xend); y=(int)Math.min(y+y1R,yend);
		x1R=xstart-x; y1R=ystart-y;
		x2R=xend-x; y2R=yend-y;
		if (IJ.altKeyDown()) {
			x1R=(int)Math.round(x1R); y1R=(int)Math.round(y1R);
			x2R=(int)Math.round(x2R); y2R=(int)Math.round(y2R);
		}
		width=(int)Math.abs(x2R-x1R); height=(int)Math.abs(y2R-y1R);
		if (width<1) width=1; if (height<1) height=1;
		updateClipRect();
		if (imp!=null) {
			if (lineWidth==1)
				imp.draw(clipX, clipY, clipWidth, clipHeight);
			else
				imp.draw();
		}
		oldX=x; oldY=y;
		oldWidth=width; oldHeight=height;
	}

	void move(int sx, int sy) {
		int xNew = ic.offScreenX(sx);
		int yNew = ic.offScreenY(sy);
		x += xNew - startxd;
		y += yNew - startyd;
		clipboard=null;
		startxd = xNew;
		startyd = yNew;
		if (lineWidth==1) {
			updateClipRect();
			imp.draw(clipX, clipY, clipWidth, clipHeight);
		} else
			imp.draw();
		oldX = x;
		oldY = y;
		oldWidth = width;
		oldHeight=height;
	}

	protected void moveHandle(int sx, int sy) {
		double ox = ic.offScreenXD(sx);
		double oy = ic.offScreenYD(sy);
		x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
		switch (activeHandle) {
			case 0: x1d=ox; y1d=oy; break;
			case 1: x2d=ox; y2d=oy; break;
			case 2:
				double dx = ox-(x1d+(x2d-x1d)/2);
				double dy = oy-(y1d+(y2d-y1d)/2);
				x1d+=dx; y1d+=dy; x2d+=dx; y2d+=dy;
				if (lineWidth>1) {
					x1d+=xHandleOffset; y1d+=yHandleOffset; 
					x2d+=xHandleOffset; y2d+=yHandleOffset;
				}
				break;
		}
		if (constrain) {
			double dx = Math.abs(x1d-x2d);
			double dy = Math.abs(y1d-y2d);
			if (activeHandle==0) {
				if (dx>=dy) y1d = y2d; else x1d = x2d;
			} else if (activeHandle==1) {
				if (dx>=dy) y2d= y1d; else x2d = x1d;
			}
		}
		x=(int)Math.min(x1d,x2d); y=(int)Math.min(y1d,y2d);
		x1R=x1d-x; y1R=y1d-y;
		x2R=x2d-x; y2R=y2d-y;
		width=(int)Math.abs(x2R-x1R); height=(int)Math.abs(y2R-y1R);
		updateClipRect();
		if (lineWidth==1)
			imp.draw(clipX, clipY, clipWidth, clipHeight);
		else
			imp.draw();
		oldX = x;
		oldY = y;
		oldWidth = width;
		oldHeight = height;
	}

	public void mouseDownInHandle(int handle, int sx, int sy) {
		state = MOVING_HANDLE;
		activeHandle = handle;
		if (lineWidth<=3)
			ic.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

	/** Draws this line on the image. */
	public void draw(Graphics g) {
		if (ic==null) return;
		x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
		x1=(int)x1d; y1=(int)y1d; x2=(int)x2d; y2=(int)y2d;
		int sx1 = ic.screenXD(x1d);
		int sy1 = ic.screenYD(y1d);
		int sx2 = ic.screenXD(x2d);
		int sy2 = ic.screenYD(y2d);
		int sx3 = sx1 + (sx2-sx1)/2;
		int sy3 = sy1 + (sy2-sy1)/2;
		g.setColor(instanceColor!=null?instanceColor:ROIColor);
		if (lineWidth>1) {
			Graphics2D g2d = (Graphics2D)g;
			GeneralPath path = new GeneralPath();
			path.moveTo(sx1, sy1);
			path.lineTo(sx2, sy2);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.3f);
			g2d.setComposite(ac);
			g2d.setStroke(new BasicStroke((float)(lineWidth*ic.getMagnification()),BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
			g2d.draw(path);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f);
			g2d.setStroke(new BasicStroke(1));
			g2d.setComposite(ac);
		}
		g.drawLine(sx1, sy1, sx2, sy2);
		if (state!=CONSTRUCTING) {
			int size2 = HANDLE_SIZE/2;
			handleColor=instanceColor!=null?instanceColor:ROIColor; drawHandle(g, sx1-size2, sy1-size2); handleColor=Color.white;
			drawHandle(g, sx2-size2, sy2-size2);
			drawHandle(g, sx3-size2, sy3-size2);
		}
		if (state!=NORMAL)
			IJ.showStatus(imp.getLocationAsString(x2,y2)+", angle=" + IJ.d2s(getAngle(x1,y1,x2,y2)) + ", length=" + IJ.d2s(getLength()));
		if (updateFullWindow)
			{updateFullWindow = false; imp.draw();}
	}

	/** Returns the length of this line. */
	public double getLength() {
		if (imp==null || IJ.altKeyDown())
			return getRawLength();
		else {
			Calibration cal = imp.getCalibration();
			return Math.sqrt((x2d-x1d)*cal.pixelWidth*(x2d-x1d)*cal.pixelWidth
				+ (y2d-y1d)*cal.pixelHeight*(y2d-y1d)*cal.pixelHeight);
		}
	}

	/** Returns the length of this line in pixels. */
	public double getRawLength() {
		return Math.sqrt((x2d-x1d)*(x2d-x1d)+(y2d-y1d)*(y2d-y1d));
	}

	/** Returns the pixel values along this line. */
	public double[] getPixels() {
			double[] profile;
			if (lineWidth==1) {
				ImageProcessor ip = imp.getProcessor();
				profile = ip.getLine(x1d, y1d, x2d, y2d);
			} else {
				ImageProcessor ip2 = (new Straightener()).rotateLine(imp,lineWidth);
				if (ip2==null) return null;
				int width = ip2.getWidth();
				int height = ip2.getHeight();
				profile = new double[width];
				double[] aLine;
				ip2.setInterpolate(false);
				for (int y=0; y<height; y++) {
					aLine = ip2.getLine(0, y, width-1, y);
					for (int i=0; i<width; i++)
						profile[i] += aLine[i];
				}
				for (int i=0; i<width; i++)
					profile[i] /= height;
			}
			return profile;
	}
	
	public Polygon getPolygon() {
		Polygon p = new Polygon();
		if (lineWidth==1) {
			p.addPoint(x1, y1);
			p.addPoint(x2, y2);
		} else {
			double angle = Math.atan2(y1-y2, x2-x1);
			double width2 = lineWidth/2.0;
			double p1x = x1 + Math.cos(angle+Math.PI/2d)*width2;
			double p1y = y1 - Math.sin(angle+Math.PI/2d)*width2;
			double p2x = x1 + Math.cos(angle-Math.PI/2d)*width2;
			double p2y = y1 - Math.sin(angle-Math.PI/2d)*width2;
			double p3x = x2 + Math.cos(angle-Math.PI/2d)*width2;
			double p3y = y2 - Math.sin(angle-Math.PI/2d)*width2;
			double p4x = x2 + Math.cos(angle+Math.PI/2d)*width2;
			double p4y = y2 - Math.sin(angle+Math.PI/2d)*width2;
			p.addPoint((int)Math.round(p1x), (int)Math.round(p1y));
			p.addPoint((int)Math.round(p2x), (int)Math.round(p2y));
			p.addPoint((int)Math.round(p3x), (int)Math.round(p3y));
			p.addPoint((int)Math.round(p4x), (int)Math.round(p4y));
		}
		return p;
	}

	public void drawPixels(ImageProcessor ip) {
		ip.setLineWidth(1);
		if (lineWidth==1) {
			ip.moveTo(x1, y1);
			ip.lineTo(x2, y2);
		} else {
			ip.drawPolygon(getPolygon());
			updateFullWindow = true;
		}
	}

	public boolean contains(int x, int y) {
		if (lineWidth>1)
			return getPolygon().contains(x, y);
		else
			return false;
	}
		
	/** Returns a handle number if the specified screen coordinates are  
		inside or near a handle, otherwise returns -1. */
	public int isHandle(int sx, int sy) {
		int size = HANDLE_SIZE+5;
		if (lineWidth>1) size += (int)Math.log(lineWidth);
		int halfSize = size/2;
		int sx1 = ic.screenXD(x+x1R) - halfSize;
		int sy1 = ic.screenYD(y+y1R) - halfSize;
		int sx2 = ic.screenXD(x+x2R) - halfSize;
		int sy2 = ic.screenYD(y+y2R) - halfSize;
		int sx3 = sx1 + (sx2-sx1)/2-1;
		int sy3 = sy1 + (sy2-sy1)/2-1;
		if (sx>=sx1&&sx<=sx1+size&&sy>=sy1&&sy<=sy1+size) return 0;
		if (sx>=sx2&&sx<=sx2+size&&sy>=sy2&&sy<=sy2+size) return 1;
		if (sx>=sx3&&sx<=sx3+size+2&&sy>=sy3&&sy<=sy3+size+2) return 2;
		return -1;
	}

	public static int getWidth() {
		return lineWidth;
	}

	public static void setWidth(int w) {
		if (w<1) w = 1;
		int max = 500;
		if (w>max) {
			IjxImagePlus imp2 = WindowManager.getCurrentImage();
			if (imp2!=null) {
				max = Math.max(max, imp2.getWidth());
				max = Math.max(max, imp2.getHeight());
			}
			if (w>max) w = max;
		}
		lineWidth = w;
	}
	
	/** Return the bounding rectangle of this line. */
	public Rectangle getBounds() {
		int xmin = (int)Math.round(Math.min(x1d, x2d));
		int ymin = (int)Math.round(Math.min(y1d, y2d));
		int w = (int)Math.round(Math.abs(x2d - x1d));
		int h = (int)Math.round(Math.abs(y2d - y1d));
		return new Rectangle(xmin, ymin, w, h);
	}
	
	/** Nudge end point of line by one pixel. */
	public void nudgeCorner(int key) {
		if (ic==null) return;
		double inc = 1.0/ic.getMagnification();
		switch(key) {
			case KeyEvent.VK_UP: y2R-=inc; break;
			case KeyEvent.VK_DOWN: y2R+=inc; break;
			case KeyEvent.VK_LEFT: x2R-=inc; break;
			case KeyEvent.VK_RIGHT: x2R+=inc; break;
		}
		grow(ic.screenXD(x+x2R), ic.screenYD(y+y2R));
	}


}
