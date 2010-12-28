package ijx.roi;

import ijx.process.ImageProcessor;
import ijx.process.ByteProcessor;
import ijx.Prefs;
import ijx.IJ;
import java.awt.*;

import ijx.gui.IjxToolbar;

import ijx.plugin.filter.Analyzer;
import ijx.plugin.frame.Recorder;
import imagej.util.Java2;
import ijx.CentralLookup;
import ijx.IjxImagePlus;

/** This class represents a collection of points. */
public class PointRoi extends PolygonRoi {
	private static Font font;
	private static int fontSize = 9;
	private double saveMag;
	IjxToolbar toolbar = ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class));
	/** Creates a new PointRoi using the specified arrays of offscreen coordinates. */
	public PointRoi(int[] ox, int[] oy, int points) {
		super(ox, oy, points, POINT);
		width+=1; height+=1;
	}

	/** Creates a new PointRoi from a Polygon. */
	public PointRoi(Polygon poly) {
		this(poly.xpoints, poly.ypoints, poly.npoints);
	}

	/** Creates a new PointRoi using the specified offscreen coordinates. */
	public PointRoi(int ox, int oy) {
		super(makeXArray(ox, null), makeYArray(oy, null), 1, POINT);
		width=1; height=1;
	}

	/** Creates a new PointRoi using the specified screen coordinates. */
	public PointRoi(int sx, int sy, IjxImagePlus imp) {
		super(makeXArray(sx, imp), makeYArray(sy, imp), 1, POINT);
		setImage(imp);
		width=1; height=1;
		if (imp!=null) imp.draw(x-5, y-5, width+10, height+10);
		if (Recorder.record && !Recorder.scriptMode()) 
			Recorder.record("makePoint", x, y);

	}

	static int[] makeXArray(int value, IjxImagePlus imp) {
		int[] array = new int[1];
		array[0] = imp!=null?imp.getCanvas().offScreenX(value):value;
		return array;
	}
				
	static int[] makeYArray(int value, IjxImagePlus imp) {
		int[] array = new int[1];
		array[0] = imp!=null?imp.getCanvas().offScreenY(value):value;
		return array;
	}
				
	public void handleMouseMove(int ox, int oy) {
		//IJ.log("handleMouseMove");
	}
	
	public  void handleMouseUp(int sx, int sy) {
		super.handleMouseUp(sx, sy);
		modifyRoi(); //adds this point to previous points if shift key down
	}
	
	/** Draws the points on the image. */
	public void draw(Graphics g) {
		//IJ.log("draw: " + nPoints+"  "+width+"  "+height);
		updatePolygon();
		if (ic!=null) mag = ic.getMagnification();
		int size2 = HANDLE_SIZE/2;
		if (!Prefs.noPointLabels && (nPoints>1||toolbar.getMultiPointMode())) {
			fontSize = 9;
			if (mag>1.0)
				fontSize = (int)(((mag-1.0)/3.0+1.0)*9.0);
			if (fontSize>18) fontSize = 18;
			if (font==null || mag!=saveMag)
				font = new Font("SansSerif", Font.PLAIN, fontSize);
			g.setFont(font);
			if (fontSize>9)
				Java2.setAntialiasedText(g, true);
			saveMag = mag;
		}
		for (int i=0; i<nPoints; i++)
			drawPoint(g, xp2[i]-size2, yp2[i]-size2, i+1);
		//showStatus();
		if (updateFullWindow)
			{updateFullWindow = false; imp.draw();}
	}

	void drawPoint(Graphics g, int x, int y, int n) {
		g.setColor(fillColor!=null?fillColor:Color.white);
		g.drawLine(x-4, y+2, x+8, y+2);
		g.drawLine(x+2, y-4, x+2, y+8);
		g.setColor(strokeColor!=null?strokeColor:ROIColor);
		g.fillRect(x+1,y+1,3,3);
		if (!Prefs.noPointLabels && (nPoints>1||toolbar.getMultiPointMode()))
			g.drawString(""+n, x+6, y+fontSize+4);
		g.setColor(Color.black);
		g.drawRect(x, y, 4, 4);
	}

	public void drawPixels(ImageProcessor ip) {
		ip.setLineWidth(Analyzer.markWidth);
		for (int i=0; i<nPoints; i++) {
			ip.moveTo(x+xp[i], y+yp[i]);
			ip.lineTo(x+xp[i], y+yp[i]);
		}
	}
	
	/** Returns a copy of this PointRoi with a point at (x,y) added. */
	public PointRoi addPoint(int x, int y) {
		Polygon poly = getPolygon();
		poly.addPoint(x, y);
		PointRoi p = new PointRoi(poly.xpoints, poly.ypoints, poly.npoints);
		IJ.showStatus("count="+poly.npoints);
		return p;
	}
	
	/** Subtract the points that intersect the specified ROI and return 
		the result. Returns null if there are no resulting points. */
	public PointRoi subtractPoints(Roi roi) {
		Polygon points = getPolygon();
		Polygon poly = roi.getPolygon();
		Polygon points2 = new Polygon();
		for (int i=0; i<points.npoints; i++) {
			if (!poly.contains(points.xpoints[i], points.ypoints[i]))
				points2.addPoint(points.xpoints[i], points.ypoints[i]);
		}
		if (points2.npoints==0)
			return null;
		else
			return new PointRoi(points2.xpoints, points2.ypoints, points2.npoints);
	}

	public ImageProcessor getMask() {
		if (cachedMask!=null && cachedMask.getPixels()!=null)
			return cachedMask;
		ImageProcessor mask = new ByteProcessor(width, height);
		for (int i=0; i<nPoints; i++) {
			mask.putPixel(xp[i], yp[i], 255);
		}
		cachedMask = mask;
		return mask;
	}

	/** Returns true if (x,y) is one of the points in this collection. */
	public boolean contains(int x, int y) {
		for (int i=0; i<nPoints; i++) {
			if (x==this.x+xp[i] && y==this.y+yp[i]) return true;
		}
		return false;
	}

	public String toString() {
		if (nPoints>1)
			return ("Roi[Points, count="+nPoints+"]");
		else
			return ("Roi[Point, x="+x+", y="+y+"]");
	}

}
