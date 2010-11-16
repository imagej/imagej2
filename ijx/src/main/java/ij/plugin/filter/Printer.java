package ij.plugin.filter;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.Calibration;
import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.print.*;

/** This plugin implements the File/Page Setup and File/Print commands. */
public class Printer implements PlugInFilter, Printable {
	private IjxImagePlus imp;
	private static double scaling = 100.0;
	private static boolean drawBorder;
	private static boolean center = true;
	private static boolean label;
	private static boolean printSelection;
	private static boolean rotate;
	private static boolean actualSize;
	private static int fontSize = 12;

	public int setup(String arg, IjxImagePlus imp) {
		if (arg.equals("setup"))
			{pageSetup(); return DONE;}
		this.imp = imp;
		IJ.register(Printer.class);
		return DOES_ALL+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		print(imp);
	}
	
	void pageSetup() {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		Roi roi = imp!=null?imp.getRoi():null;
		boolean isRoi = roi!=null && roi.isArea();
		GenericDialog gd = new GenericDialog("Page Setup");
		gd.addNumericField("Scale:", scaling, 0, 3, "%");
		gd.addCheckbox("Draw border", drawBorder);
		gd.addCheckbox("Center on page", center);
		gd.addCheckbox("Print title", label);
		if (isRoi)
			gd.addCheckbox("Selection only", printSelection);
		gd.addCheckbox("Rotate 90"+IJ.degreeSymbol, rotate);
		gd.addCheckbox("Print_actual size", actualSize);
		if (imp!=null)
			gd.enableYesNoCancel(" OK ", "Print");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		scaling = gd.getNextNumber();
		if (scaling<5.0) scaling = 5;
		drawBorder = gd.getNextBoolean();
		center = gd.getNextBoolean();
		label = gd.getNextBoolean();
		if (isRoi)
			printSelection = gd.getNextBoolean();
		else
			printSelection = false;
		rotate = gd.getNextBoolean();
		actualSize = gd.getNextBoolean();
		if (!gd.wasOKed() && imp!=null) {
			this.imp = imp;
			print(imp);
		}
	}

	void print(IjxImagePlus imp) {
		PrinterJob pj = PrinterJob.getPrinterJob();
		pj.setPrintable(this);
		//pj.pageDialog(pj.defaultPage());
		if (IJ.macroRunning() || pj.printDialog()) {
			imp.startTiming();
			try {pj.print(); }
			catch (PrinterException e) {
				IJ.log(""+e);
			}
		}
	}
	
	public int print(Graphics g, PageFormat pf, int pageIndex) {
		if (pageIndex != 0) return NO_SUCH_PAGE;
		Roi roi = imp.getRoi();
		IjxImagePlus imp2 = imp;
		if (imp2.getOverlay()!=null && !imp2.getHideOverlay()) {
			imp2.killRoi();
			imp2 = imp2.flatten();
		}
		ImageProcessor ip = imp2.getProcessor();
		if (printSelection && roi!=null && roi.isArea() )
			ip.setRoi(roi);
			ip = ip.crop();
		if (rotate)
			ip = ip.rotateLeft();
		//IJ.getFactory().newImagePlus("ip", ip.duplicate()).show();
		int width = ip.getWidth();
		int height = ip.getHeight();
		int margin = 0;
		if (drawBorder) margin = 1;
		double scale = scaling/100.0;
		int dstWidth = (int)(width*scale);
		int dstHeight = (int)(height*scale);
		int pageX = (int)pf.getImageableX();
		int pageY = (int)pf.getImageableY();
		int dstX = pageX+margin;
		int dstY = pageY+margin;
		Image img = ip.createImage();
		double pageWidth = pf.getImageableWidth()-2*margin;
		double pageHeight = pf.getImageableHeight()-2*margin;
		if (label && pageWidth-dstWidth<fontSize+5) {
			dstY += fontSize+5;
			pageHeight -= fontSize+5;
		}
		if (actualSize) {
			Calibration cal = imp.getCalibration();
			int unitIndex = ImageProperties.getUnitIndex(cal.getUnit());
			if (unitIndex!=ImageProperties.OTHER_UNIT) {
				double unitsPerCm = ImageProperties.getUnitsPerCm(unitIndex);
				double widthInCm = width*cal.pixelWidth/unitsPerCm;
				double heightInCm = height*cal.pixelHeight/unitsPerCm;
				dstWidth = (int)((widthInCm*(72*0.3937))*scale);
				dstHeight = (int)((heightInCm*(72*0.3937))*scale);
			}
			if (center && dstWidth<pageWidth && dstHeight<pageHeight) {
				dstX += (pageWidth-dstWidth)/2;
				dstY += (pageHeight-dstHeight)/2;
			}
		} else if (dstWidth>pageWidth || dstHeight>pageHeight) {
			// scale to fit page
			double hscale = pageWidth/dstWidth;
			double vscale = pageHeight/dstHeight;
			double scale2 = hscale<=vscale?hscale:vscale;
			dstWidth = (int)(dstWidth*scale2);
			dstHeight = (int)(dstHeight*scale2);
		} else if (center) {
			dstX += (pageWidth-dstWidth)/2;
			dstY += (pageHeight-dstHeight)/2;
		}
		g.drawImage(img, 
			dstX, dstY, dstX+dstWidth, dstY+dstHeight,
			0, 0, width, height, 
			null);
		if (drawBorder)
			g.drawRect(dstX-1, dstY-1, dstWidth+1, dstHeight+1);
		if (label) {
			g.setFont(new Font("SanSerif", Font.PLAIN, fontSize));
			g.setColor(Color.black);
			g.drawString(imp.getTitle(), pageX+5, pageY+fontSize);
		}
		return PAGE_EXISTS;
	}

}
