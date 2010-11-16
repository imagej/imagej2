package ij.gui;
import ij.*;
import ijx.CentralLookup;
import ijx.IjxImagePlus;
import ijx.gui.IjxImageCanvas;
import java.awt.*;

/** Implements the ROI Brush tool.*/
public class RoiBrush implements Runnable {
	static int ADD=0, SUBTRACT=1;
	static int leftClick=16, alt=9, shift=1;
	private Polygon poly;
	private Point previousP;
	private int mode = ADD;
 
	public RoiBrush() {
		Thread thread = new Thread(this, "RoiBrush");
		thread.start();
	}

	public void run() {

		//int size = Toolbar.getBrushSize();
		int size = ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).getBrushSize();
		IjxImagePlus img = WindowManager.getCurrentImage();
		if (img==null) return;
		IjxImageCanvas ic = img.getCanvas();
		if (ic==null) return;
		Roi roi = img.getRoi();
		if (roi!=null && !roi.isArea())
			img.killRoi();
		Point p = ic.getCursorLoc();
		if (roi!=null && !roi.contains(p.x, p.y))
			mode = SUBTRACT;
		int flags;
		while (true) {
			p = ic.getCursorLoc();
			if (p.equals(previousP))
				{IJ.wait(1); continue;}
			previousP = p;
			flags = ic.getModifiers();
			if ((flags&leftClick)==0) return;
			if ((flags&shift)!=0)
				mode = ADD;
			else if ((flags&alt)!=0)
				mode = SUBTRACT;
			if (mode==ADD)
				addCircle(img, p.x, p.y, size);
			else
				subtractCircle(img, p.x, p.y, size);
		}
	}

	void addCircle(IjxImagePlus img, int x, int y, int width) {
		Roi roi = img.getRoi();
		Roi roi2 = roi;
		if (roi2!=null) {
			if (!(roi2 instanceof ShapeRoi))
				roi2 = new ShapeRoi(roi2);
			((ShapeRoi)roi2).or(getCircularRoi(x, y, width));
			roi2.copyAttributes(roi);
		} else
			roi2 = new OvalRoi(x-width/2, y-width/2, width, width);
		img.setRoi(roi2);
	}

	void subtractCircle(IjxImagePlus img, int x, int y, int width) {
		Roi roi = img.getRoi();
		Roi roi2 = roi;
		if (roi2!=null) {
			if (!(roi2 instanceof ShapeRoi))
			roi2 = new ShapeRoi(roi2);
			((ShapeRoi)roi2).not(getCircularRoi(x, y, width));
			roi2.copyAttributes(roi);
			img.setRoi(roi2);
		}
	}

    
	ShapeRoi getCircularRoi(int x, int y, int width) {
		if (poly==null) {
			Roi roi = new OvalRoi(x-width/2, y-width/2, width, width);
			poly = roi.getPolygon();
			for (int i=0; i<poly.npoints; i++) {
				poly.xpoints[i] -= x;
				poly.ypoints[i] -= y;
			}
		}
		return new ShapeRoi(x, y, poly);
	}

}

