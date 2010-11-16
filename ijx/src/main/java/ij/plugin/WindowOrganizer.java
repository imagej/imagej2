package ij.plugin;
import ij.*;
import ij.gui.*;
import ijx.IjxImagePlus;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxWindow;
import java.awt.*;

public class WindowOrganizer implements PlugIn {

	private static final int XSTART=4, YSTART=80, XOFFSET=8, YOFFSET=24,MAXSTEP=200,GAP=2;
	private int titlebarHeight = IJ.isMacintosh()?40:20;

	public void run(String arg) {
		int[] wList = WindowManager.getIDList();
		if (arg.equals("show"))
			{showAll(wList); return;}
		if (wList==null) {
			IJ.noImage();
			return;
		}
		if (arg.equals("tile"))
			tileWindows(wList);
		else
			cascadeWindows(wList);
	}
	
	void tileWindows(int[] wList) {
		Dimension screen = IJ.getScreenSize();
		int minWidth = Integer.MAX_VALUE;
		int minHeight = Integer.MAX_VALUE;
		boolean allSameSize = true;
		int width=0, height=0;
		double totalWidth = 0;
		double totalHeight = 0;
		for (int i=0; i<wList.length; i++) {
			IjxImageWindow win = getWindow(wList[i]);
			if (win==null)
				continue;
			Dimension d = win.getSize();
			int w = d.width;
			int h = d.height + titlebarHeight;
			if (i==0) {
				width = w;
				height = h;
			}
			if (w!=width || h!=height)
				allSameSize = false;
			if (w<minWidth)
				minWidth = w;
			if (h<minHeight)
				minHeight = h;
			totalWidth += w;
			totalHeight += h;
		}
		int nPics = wList.length;
		double averageWidth = totalWidth/nPics;
		double averageHeight = totalHeight/nPics;
		int tileWidth = (int)averageWidth;
		int tileHeight = (int)averageHeight;
		//IJ.write("tileWidth, tileHeight: "+tileWidth+" "+tileHeight);
 		int hspace = screen.width - 2 * GAP;
		if (tileWidth>hspace)
			tileWidth = hspace;
		int vspace = screen.height - YSTART;
		if (tileHeight>vspace)
			tileHeight = vspace;
		int hloc, vloc;
		boolean theyFit;
		do {
			hloc = XSTART;
			vloc = YSTART;
			theyFit = true;
			int i = 0;
			do {
				i++;
				if (hloc+tileWidth>screen.width) {
					hloc = XSTART;
					vloc = vloc + tileHeight;
					if (vloc+tileHeight> screen.height)
						theyFit = false;
				}
				hloc = hloc + tileWidth + GAP;
			} while (theyFit && (i<nPics));
			if (!theyFit) {
				tileWidth = (int)(tileWidth*0.98 +0.5);
				tileHeight = (int)(tileHeight*0.98+0.5);
			}
		} while (!theyFit);
		int nColumns = (screen.width-XSTART)/(tileWidth+GAP);
		int nRows = nPics/nColumns;
		if ((nPics%nColumns)!=0)
			nRows++;
		hloc = XSTART;
		vloc = YSTART;
		
		for (int i=0; i<nPics; i++) {
			if (hloc+tileWidth>screen.width) {
				hloc = XSTART;
				vloc = vloc + tileHeight;
			}
			IjxImageWindow win = getWindow(wList[i]);
			if (win!=null) {
				win.setLocation(hloc, vloc);
				//IJ.write(i+" "+w+" "+tileWidth+" "+mag+" "+IJ.d2s(zoomFactor,2)+" "+zoomCount);
				IjxImageCanvas canvas = win.getCanvas();
				while (win.getSize().width*0.85>=tileWidth && canvas.getMagnification()>0.03125)
					canvas.zoomOut(0, 0);
				win.toFront();
			}
			hloc += tileWidth + GAP;
		}
	}

	IjxImageWindow getWindow(int id) {
		IjxImageWindow win = null;
		IjxImagePlus imp = WindowManager.getImage(id);
		if (imp!=null)
			win = imp.getWindow();
		return win;
	}		
			
	void cascadeWindows(int[] wList) {
		Dimension screen = IJ.getScreenSize();
		int x = XSTART;
		int y = YSTART;
		int xstep = 0;
		int xstart = XSTART;
		for (int i=0; i<wList.length; i++) {
			IjxImageWindow win = getWindow(wList[i]);
			if (win==null)
				continue;
			Dimension d = win.getSize();
			if (i==0) {
				xstep = (int)(d.width*0.8);
				if (xstep>MAXSTEP)
					xstep = MAXSTEP;
			}
			if (y+d.height*0.67>screen.height) {
				xstart += xstep;
				if (xstart+d.width*0.67>screen.width)
					xstart = XSTART+XOFFSET;
				x = xstart;
				y = YSTART;
			}
			win.setLocation(x, y);
			win.toFront();
				x += XOFFSET;
			y += YOFFSET;
		}
	}
	
	void showAll(int[] wList) {
		if (wList!=null) {
			for (int i=0; i<wList.length; i++) {
				IjxImageWindow win = getWindow(wList[i]);
				if (win!=null) win.toFront();
				
			}
		}
		IjxWindow[] frames = WindowManager.getNonImageWindows();
		if (frames!=null) {
			for (int i=0; i<frames.length; i++)
				frames[i].toFront();
		}
		IJ.getTopComponent().toFront();
	}

}


