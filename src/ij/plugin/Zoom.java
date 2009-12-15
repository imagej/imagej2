package ij.plugin;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxImageCanvas;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import java.awt.*;

/** This plugin implements the commands in the Image/Zoom submenu. */
public class Zoom implements PlugIn{

	/** 'arg' must be "in", "out", "100%" or "orig". */
	public void run(String arg) {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			{IJ.noImage(); return;}
		IjxImageCanvas ic = imp.getCanvas();
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
		else if (arg.equals("max")) {
			IjxImageWindow win = imp.getWindow();
			win.setBounds(win.getMaximumBounds());
			win.maximize();
		}
	}
	
	void zoomToSelection(IjxImagePlus imp, IjxImageCanvas ic) {
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
		mag = ImageCanvasHelper.getHigherZoomLevel(mag);
		while(r.width*mag<w.width - marginw && r.height*mag<w.height - marginh) {
			ic.zoomIn(ic.screenX(x), ic.screenY(y));
			double cmag = ic.getMagnification();
			if (cmag==32.0) break;
			mag = ImageCanvasHelper.getHigherZoomLevel(cmag);
			w = imp.getWindow().getBounds();
		}
	}
	
}

