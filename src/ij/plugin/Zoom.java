package ij.plugin;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import java.awt.*;

/** This plugin implements the commands in the Image/Zoom submenu. */
public class Zoom implements PlugIn{

	/** 'arg' must be "in", "out", "100%" or "orig". */
	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			{IJ.noImage(); return;}
		ImageCanvas ic = imp.getCanvas();
		if (ic==null) return;
		Point loc = ic.getCursorLoc();
		int x = ic.screenX(loc.x);
		int y = ic.screenY(loc.y);
    	if (arg.equals("in")) {
 			ic.zoomIn(x, y);
			if (ic.getMagnification()<=1.0) imp.repaintWindow();
    	} else if (arg.equals("out")) {
			ic.zoomOut(x, y);
			if (ic.getMagnification()<1.0) imp.repaintWindow();
    	} else if (arg.equals("orig"))
			ic.unzoom();
    	else if (arg.equals("100%"))
    		ic.zoom100Percent();
		else if (arg.equals("to"))
			zoomToSelection(imp, ic);
		else if (arg.equals("set"))
			setZoom(imp, ic);
		else if (arg.equals("max")) {
			ImageWindow win = imp.getWindow();
			win.setBounds(win.getMaximumBounds());
			win.maximize();
		}
	}
	
	void zoomToSelection(ImagePlus imp, ImageCanvas ic) {
		Roi roi = imp.getRoi();
		ic.unzoom();
		if (roi==null) return;
		Rectangle w = imp.getWindow().getBounds();
		Rectangle r = roi.getBounds();
		double mag = ic.getMagnification();
		int marginw = (int)((w.width - mag * imp.getWidth()));
		int marginh = (int)((w.height - mag * imp.getHeight()));
		int x = r.x+r.width/2;
		int y = r.y+r.height/2;
		mag = ic.getHigherZoomLevel(mag);
		while(r.width*mag<w.width - marginw && r.height*mag<w.height - marginh) {
			ic.zoomIn(ic.screenX(x), ic.screenY(y));
			double cmag = ic.getMagnification();
			if (cmag==32.0) break;
			mag = ic.getHigherZoomLevel(cmag);
			w = imp.getWindow().getBounds();
		}
	}
	
	/** Based on Albert Cardona's ZoomExact plugin:
		http://albert.rierol.net/software.html */
	void setZoom(ImagePlus imp, ImageCanvas ic) {
		ImageWindow win = imp.getWindow();
		GenericDialog gd = new GenericDialog("Set Zoom");
		gd.addNumericField("Zoom (%): ", ic.getMagnification() * 200, 0);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		double mag = gd.getNextNumber()/100.0;
		if (mag<=0.0) mag = 1.0;
		win.getCanvas().setMagnification(mag);
		double w = imp.getWidth()*mag;
		double h = imp.getHeight()*mag;
		Dimension screen = IJ.getScreenSize();
		if (w>screen.width-20) w = screen.width - 20;  // does it fit?
		if (h>screen.height-50) h = screen.height - 50;
		ic.setSourceRect(new Rectangle(0, 0, (int)(w/mag), (int)(h/mag)));
		ic.setDrawingSize((int)w, (int)h);
		win.pack();
		ic.repaint();
	}
	
}

