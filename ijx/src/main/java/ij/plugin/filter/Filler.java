package ij.plugin.filter;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ijx.CentralLookup;
import ijx.IjxImagePlus;
import ijx.gui.IjxImageCanvas;
import java.awt.*;

/** This plugin implements ImageJ's Fill, Clear, Clear Outside and Draw commands. */
public class Filler implements PlugInFilter, Measurements {
	
	String arg;
	Roi roi;
	IjxImagePlus imp;
	int sliceCount;
	ImageProcessor mask;
	boolean isTextRoi;
    IjxToolbar toolbar = ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class));

	public int setup(String arg, IjxImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		if (imp!=null)
			roi = imp.getRoi();			
		isTextRoi = roi!=null && (roi instanceof TextRoi);
		IJ.register(Filler.class);
		int baseCapabilities = DOES_ALL+ROI_REQUIRED;
	 	if (arg.equals("clear")) {
	 		if (roi!=null && roi.getType()==Roi.POINT) {
	 			IJ.error("Clear", "Area selection required");
	 			return DONE;
	 		}
	 		if (isTextRoi || isLineSelection())
				return baseCapabilities;
			else
				return IJ.setupDialog(imp,baseCapabilities+SUPPORTS_MASKING);
		} else if (arg.equals("draw"))
				return IJ.setupDialog(imp,baseCapabilities);
		else if (arg.equals("label")) {
				if (Analyzer.firstParticle<Analyzer.lastParticle)
					return baseCapabilities-ROI_REQUIRED;
				else
					return baseCapabilities;
		} else if (arg.equals("outside"))
				return IJ.setupDialog(imp,baseCapabilities);
		else if (roi!=null && roi.getType()==Roi.POINT && arg.equals("fill")) {
	 			IJ.error("Fill", "Area selection required");
	 			return DONE;
	 	} else
			return IJ.setupDialog(imp,baseCapabilities+SUPPORTS_MASKING);
	}

	public void run(ImageProcessor ip) {
	 	if (arg.equals("clear"))
	 		clear(ip);
	 	else if (isTextRoi && (arg.equals("draw") || arg.equals("fill")))
	 		draw(ip);
	 	else if (arg.equals("fill"))
	 		fill(ip);
	 	else if (arg.equals("draw"))
			draw(ip);
	 	else if (arg.equals("label"))
			label(ip);
	 	else if (arg.equals("outside"))
	 		clearOutside(ip);
	}

	boolean isLineSelection() {
		return roi!=null && roi.isLine();
	}
	
	boolean isStraightLine() {
		return roi!=null && roi.getType()==Roi.LINE;
	}
	
	public void clear(ImageProcessor ip) {
	 	ip.setColor(toolbar.getBackgroundColor());
		if (isLineSelection()) {
			if (isStraightLine() && roi.getStrokeWidth()>1)
				ip.fillPolygon(roi.getPolygon());
			else
				roi.drawPixels();
		} else
	 		ip.fill(); // fill with background color
		ip.setColor(toolbar.getForegroundColor());
	}
		
	/**
	* @deprecated
	* replaced by ImageProcessor.fill(Roi)
	*/
	public void fill(ImageProcessor ip) {
		ip.setColor(toolbar.getForegroundColor());
		if (isLineSelection()) {
			if (isStraightLine() && roi.getStrokeWidth()>1 && !(roi instanceof Arrow))
				ip.fillPolygon(roi.getPolygon());
			else
				roi.drawPixels(ip);
		} else
	 		ip.fill(); // fill with foreground color
	}
	 			 		
	/**
	* @deprecated
	* replaced by ImageProcessor.draw(Roi)
	*/
	public void draw(ImageProcessor ip) {
		ip.setColor(toolbar.getForegroundColor());
		roi.drawPixels(ip);
		if (IJ.altKeyDown())
			drawLabel(ip);
 	}

	public void label(ImageProcessor ip) {
		if (Analyzer.getCounter()==0) {
			IJ.error("Label", "Measurement counter is zero");
			return;
		}
		if (Analyzer.firstParticle<Analyzer.lastParticle)
			drawParticleLabels(ip);
		else {
			ip.setColor(toolbar.getForegroundColor());
			IjxImageCanvas ic = imp.getCanvas();
			if (ic!=null) {
				double mag = ic.getMagnification();
				if (mag<1.0) {
					int lineWidth = 1;
					lineWidth = (int)(lineWidth/mag);
					ip.setLineWidth(lineWidth);
				}
			}
			roi.drawPixels(ip);
			ip.setLineWidth(1);
			drawLabel(ip);
		}
	}

	void drawParticleLabels(ImageProcessor ip) {
		ResultsTable rt = ResultsTable.getResultsTable();
		int count = rt.getCounter();
		int first = Analyzer.firstParticle;
		int last = Analyzer.lastParticle;
		if (count==0 || first>=count || last>=count)
			return;
		if (!rt.columnExists(ResultsTable.X_CENTROID)) {
			IJ.error("Label", "\"Centroids\" required to label particles");
			return;
		}
		for (int i=first; i<=last; i++) {
			int x = (int)rt.getValueAsDouble(ResultsTable.X_CENTROID, i);		
			int y = (int)rt.getValueAsDouble(ResultsTable.Y_CENTROID, i);		
			drawLabel(imp, ip, i+1, new Rectangle(x,y,0,0));
		}
	}

	void drawLabel(ImageProcessor ip) {
		int count = Analyzer.getCounter();
		if (count>0 && roi!=null)
			drawLabel(imp, ip, count, roi.getBounds());
	}

	public void drawLabel(IjxImagePlus imp, ImageProcessor ip, int count, Rectangle r) {
		Color foreground = toolbar.getForegroundColor();
		Color background = toolbar.getBackgroundColor();
		if (foreground.equals(background)) {
			foreground = Color.black;
			background = Color.white;
		}
		int size = 9;
		IjxImageCanvas ic = imp.getCanvas();
		if (ic!=null) {
			double mag = ic.getMagnification();
			if (mag<1.0)
				size /= mag;
		}
		if (size==9 && r.width>50 && r.height>50)
			size = 12;
		ip.setFont(new Font("SansSerif", Font.PLAIN, size));
		String label = "" + count;
		int w =  ip.getStringWidth(label);
		int x = r.x + r.width/2 - w/2;
		int y = r.y + r.height/2 + Math.max(size/2,6);
		FontMetrics metrics = ip.getFontMetrics();
		int h =  metrics.getHeight();
		ip.setColor(background);
		ip.setRoi(x-1, y-h+2, w+1, h-3);
		ip.fill();
		ip.resetRoi();
		ip.setColor(foreground);
		ip.drawString(label, x, y);
	} 

	/**
	* @deprecated
	* replaced by ImageProcessor.fillOutside(Roi)
	*/
	public synchronized void clearOutside(ImageProcessor ip) {
		if (isLineSelection()) {
			IJ.error("\"Clear Outside\" does not work with line selections.");
			return;
		}
 		sliceCount++;
 		Rectangle r = ip.getRoi();
 		if (mask==null)
 			makeMask(ip, r);
  		ip.setColor(toolbar.getBackgroundColor());
 		int stackSize = imp.getStackSize();
 		if (stackSize>1)
 			ip.snapshot();
		ip.fill();
 		ip.reset(mask);
		int width = ip.getWidth();
		int height = ip.getHeight();
 		ip.setRoi(0, 0, r.x, height);
 		ip.fill();
 		ip.setRoi(r.x, 0, r.width, r.y);
 		ip.fill();
 		ip.setRoi(r.x, r.y+r.height, r.width, height-(r.y+r.height));
 		ip.fill();
 		ip.setRoi(r.x+r.width, 0, width-(r.x+r.width), height);
 		ip.fill();
 		ip.setRoi(r); // restore original ROI
 		if (sliceCount==stackSize) {
			ip.setColor(toolbar.getForegroundColor());
			Roi roi = imp.getRoi();
			imp.killRoi();
			imp.updateAndDraw();
			imp.setRoi(roi);
		}
	}

	public void makeMask(ImageProcessor ip, Rectangle r) {
 		mask = ip.getMask();
 		if (mask==null) {
 			mask = new ByteProcessor(r.width, r.height);
 			mask.invert();
 		} else {
 			// duplicate mask (needed because getMask caches masks)
 			mask = mask.duplicate();
 		}
 		mask.invert();
 	}

}
