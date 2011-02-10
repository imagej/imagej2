package ijx.roi;
import ijx.process.ImageProcessor;
import ijx.roi.Roi;
import ijx.measure.Calibration;
import ijx.WindowManager;
import ijx.IJ;

import ijx.plugin.Straightener;
import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.event.KeyEvent;


/** This class represents a straight line selection. */
public class Line extends Roi {

	public int x1, y1, x2, y2;	// the line
	public double x1d, y1d, x2d, y2d;	// the line using sub-pixel coordinates
	protected double x1R, y1R, x2R, y2R;  // the line, relative to base of bounding rect
	private double xHandleOffset, yHandleOffset;
	private double startxd, startyd;
	public static boolean widthChanged;

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
		if (!(this instanceof Arrow) && lineWidth>1)
			updateWideLine(lineWidth);
		updateClipRect();
		oldX=x; oldY=y; oldWidth=width; oldHeight=height;
		setState(NORMAL);
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
		if (!(this instanceof Arrow) && lineWidth>1)
			updateWideLine(lineWidth);
	}

	/**
	* @deprecated
	* replaced by Line(int, int, int, int)
	*/
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
		if (IJ.controlKeyDown()) {
			x1R=(int)Math.round(x1R); y1R=(int)Math.round(y1R);
			x2R=(int)Math.round(x2R); y2R=(int)Math.round(y2R);
		}
		width=(int)Math.abs(x2R-x1R); height=(int)Math.abs(y2R-y1R);
		if (width<1) width=1; if (height<1) height=1;
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
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
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX = x;
		oldY = y;
		oldWidth = width;
		oldHeight=height;
	}

	protected void moveHandle(int sx, int sy) {
		double ox = ic.offScreenXD(sx);
		double oy = ic.offScreenYD(sy);
		x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
		double length = Math.sqrt((x2d-x1d)*(x2d-x1d) + (y2d-y1d)*(y2d-y1d));
		switch (activeHandle) {
			case 0:
                double dx = ox-x1d;
                double dy = oy-y1d;
                x1d=ox;
                y1d=oy;
                if(center){
                    x2d -= dx;
                    y2d -= dy;
                }
				if(aspect){
					double ratio = length/(Math.sqrt((x2d-x1d)*(x2d-x1d) + (y2d-y1d)*(y2d-y1d)));
					double xcd = x1d+(x2d-x1d)/2;
					double ycd = y1d+(y2d-y1d)/2;
					
					if(center){
						x1d=xcd-ratio*(xcd-x1d);
						x2d=xcd+ratio*(x2d-xcd);
						y1d=ycd-ratio*(ycd-y1d);
						y2d=ycd+ratio*(y2d-ycd);
					} else {
						x1d=x2d-ratio*(x2d-x1d);
						y1d=y2d-ratio*(y2d-y1d);
					}
					
				}
                break;
			case 1:
                dx = ox-x2d;
                dy = oy-y2d;
                x2d=ox;
                y2d=oy;
                if(center){
                    x1d -= dx;
                    y1d -= dy;
                }
				if(aspect){
					double ratio = length/(Math.sqrt((x2d-x1d)*(x2d-x1d) + (y2d-y1d)*(y2d-y1d)));
					double xcd = x1d+(x2d-x1d)/2;
					double ycd = y1d+(y2d-y1d)/2;
					
					if(center){
						x1d=xcd-ratio*(xcd-x1d);
						x2d=xcd+ratio*(x2d-xcd);
						y1d=ycd-ratio*(ycd-y1d);
						y2d=ycd+ratio*(y2d-ycd);
					} else {
						x2d=x1d+ratio*(x2d-x1d);
						y2d=y1d+ratio*(y2d-y1d);
					}
										
				}
                break;
			case 2:
				dx = ox-(x1d+(x2d-x1d)/2);
				dy = oy-(y1d+(y2d-y1d)/2);
				x1d+=dx; y1d+=dy; x2d+=dx; y2d+=dy;
				if (getStrokeWidth()>1) {
					x1d+=xHandleOffset; y1d+=yHandleOffset; 
					x2d+=xHandleOffset; y2d+=yHandleOffset;
				}
				break;
		}
		if (constrain) {
			double dx = Math.abs(x1d-x2d);
			double dy = Math.abs(y1d-y2d);
			double xcd = Math.min(x1d,x2d)+dx/2;
			double ycd = Math.min(y1d,y2d)+dy/2;
			
			//double ratio = length/(Math.sqrt((x2d-x1d)*(x2d-x1d) + (y2d-y1d)*(y2d-y1d)));
			if (activeHandle==0) {
				if (dx>=dy) {
					if(aspect){
						if(x2d>x1d) x1d=x2d-length;
						else x1d=x2d+length;
					}
					y1d = y2d;
					if(center){
						y1d=y2d=ycd;
						if(aspect){
							if(xcd>x1d) {
								x1d=xcd-length/2;
								x2d=xcd+length/2;
							}
							else{
								x1d=xcd+length/2;
								x2d=xcd-length/2;
							}
						}
					}
				}else {
					if(aspect){
						if(y2d>y1d) y1d=y2d-length;
						else y1d=y2d+length;
					}
					x1d = x2d;
					if(center){
						x1d=x2d=xcd;
						if(aspect){
							if(ycd>y1d) {
								y1d=ycd-length/2;
								y2d=ycd+length/2;
							}
							else{
								y1d=ycd+length/2;
								y2d=ycd-length/2;
							}
						}
					}
				}
			} else if (activeHandle==1) {
				if (dx>=dy) {
					if(aspect){
						if(x1d>x2d) x2d=x1d-length;
						else x2d=x1d+length;
					}
					y2d= y1d;
					if(center){
						y1d=y2d=ycd;
						if(aspect){
							if(xcd>x1d) {
								x1d=xcd-length/2;
								x2d=xcd+length/2;
							}
							else{
								x1d=xcd+length/2;
								x2d=xcd-length/2;
							}
						}
					}
				} else {
					if(aspect){
						if(y1d>y2d) y2d=y1d-length;
						else y2d=y1d+length;
					}
					x2d = x1d;
					if(center){
						x1d=x2d=xcd;
						if(aspect){
							if(ycd>y1d) {
								y1d=ycd-length/2;
								y2d=ycd+length/2;
							}
							else{
								y1d=ycd+length/2;
								y2d=ycd-length/2;
							}
						}
					}
				}
			}
		}
		x=(int)Math.min(x1d,x2d); y=(int)Math.min(y1d,y2d);
		x1R=x1d-x; y1R=y1d-y;
		x2R=x2d-x; y2R=y2d-y;
		width=(int)Math.abs(x2R-x1R); height=(int)Math.abs(y2R-y1R);
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX = x;
		oldY = y;
		oldWidth = width;
		oldHeight = height;
	}

	public void mouseDownInHandle(int handle, int sx, int sy) {
		setState(MOVING_HANDLE);
		activeHandle = handle;
		if (getStrokeWidth()<=3)
			ic.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

	/** Draws this line on the image. */
	public void draw(Graphics g) {
		if (ic==null) return;
		Color color =  strokeColor!=null? strokeColor:ROIColor;
		//if (fillColor!=null) color = fillColor;
		g.setColor(color);
		x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
		x1=(int)x1d; y1=(int)y1d; x2=(int)x2d; y2=(int)y2d;
		int sx1 = ic.screenXD(x1d);
		int sy1 = ic.screenYD(y1d);
		int sx2 = ic.screenXD(x2d);
		int sy2 = ic.screenYD(y2d);
		int sx3 = sx1 + (sx2-sx1)/2;
		int sy3 = sy1 + (sy2-sy1)/2;
		Graphics2D g2d = (Graphics2D)g;
		if (stroke!=null)
			g2d.setStroke(getScaledStroke());
		g.drawLine(sx1, sy1, sx2, sy2);
		if (wideLine && !overlay) {
			g2d.setStroke(onePixelWide);
			g.setColor(getColor());
			g.drawLine(sx1, sy1, sx2, sy2);
		}
		if (getState()!=CONSTRUCTING && !overlay) {
			int size2 = HANDLE_SIZE/2;
			handleColor = strokeColor!=null?strokeColor:ROIColor;
			drawHandle(g, sx1-size2, sy1-size2);
			handleColor=Color.white;
			drawHandle(g, sx2-size2, sy2-size2);
			drawHandle(g, sx3-size2, sy3-size2);
		}
		if (getState()!=NORMAL)
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
			if (getStrokeWidth()==1) {
				ImageProcessor ip = imp.getProcessor();
				profile = ip.getLine(x1d, y1d, x2d, y2d);
			} else {
				ImageProcessor ip2 = (new Straightener()).rotateLine(imp,(int)getStrokeWidth());
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
		if (getStrokeWidth()==1) {
			p.addPoint(x1, y1);
			p.addPoint(x2, y2);
		} else {
			double angle = Math.atan2(y1-y2, x2-x1);
			double width2 = getStrokeWidth()/2.0;
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
		if (getStrokeWidth()==1) {
			ip.moveTo(x1, y1);
			ip.lineTo(x2, y2);
		} else {
			ip.drawPolygon(getPolygon());
			updateFullWindow = true;
		}
	}

public boolean contains(int x, int y) {
	if (getStrokeWidth()>1) {
		if ((x==x1&&y==y1) || (x==x2&&y==y2))
			return true;
		else
			return getPolygon().contains(x,y);
	} else
		return false;
}
		
	/** Returns a handle number if the specified screen coordinates are  
		inside or near a handle, otherwise returns -1. */
	public int isHandle(int sx, int sy) {
		int size = HANDLE_SIZE+5;
		if (getStrokeWidth()>1) size += (int)Math.log(getStrokeWidth());
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
		widthChanged = true;
	}
		
	public void setStrokeWidth(float width) {
		super.setStrokeWidth(width);
		if (getStrokeColor()==Roi.getColor())
			wideLine = true;
	}
	
	/** Return the bounding rectangle of this line. */
	public Rectangle getBounds() {
		int xmin = (int)Math.round(Math.min(x1d, x2d));
		int ymin = (int)Math.round(Math.min(y1d, y2d));
		int w = (int)Math.round(Math.abs(x2d - x1d));
		int h = (int)Math.round(Math.abs(y2d - y1d));
		return new Rectangle(xmin, ymin, w, h);
	}
	
	protected int clipRectMargin() {
		return 4;
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
