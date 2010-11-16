package ij.plugin;
import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import ij.measure.*;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import ijx.gui.IjxImageWindow;


public class SurfacePlotter implements PlugIn {

	static final int fontSize = 14;
	static int plotWidth = 350;
	static int polygonMultiplier = 100;
	static boolean oneToOne;
	static boolean firstTime = true;
	
	static boolean showWireframe=false;
	static boolean showGrayscale=true;
	static boolean showAxis=true;
	static boolean whiteBackground=false;
	static boolean blackFill=false;
	static boolean smooth = true;

	IjxImagePlus img;
	int[] x,y;
	boolean invertedLut;
	double angleInDegrees = 35;
	double angle = (angleInDegrees/360.0)*2.0*Math.PI;
	double angle2InDegrees = 15.0;
	double angle2 = (angle2InDegrees/360.0)*2.0*Math.PI;
	double yinc2 = Math.sin(angle2);
	double p1x, p1y;  // left bottom corner
	double p2x, p2y;  // center bottom corner
	double p3x, p3y;  // right  bottom corner
	
	LookUpTable lut;
	
	public void run(String arg) {
		img = WindowManager.getCurrentImage();
		if (img==null)
			{IJ.noImage(); return;}
		if (img.getType()==IjxImagePlus.COLOR_RGB)
			{IJ.error("Surface Plotter", "Grayscale or pseudo-color image required"); return;}
		invertedLut = img.getProcessor().isInvertedLut();
		if (firstTime) {
			if (invertedLut)
				whiteBackground	= true;
			firstTime = false;			
		}
		if (!showDialog())
			return;

		int stackFlags = IJ.setupDialog(img, 0);
		if(stackFlags == PlugInFilter.DONE)
			return;
		Date start = new Date();
		lut = img.createLut();
		
		if(stackFlags == PlugInFilter.DOES_STACKS && img.getStack().getSize()>1){			
			IjxImageStack stackSource = img.getStack();
			ImageProcessor ip = stackSource.getProcessor(1);
			ImageProcessor plot = makeSurfacePlot(ip);
			IjxImageStack stack = IJ.getFactory().newImageStack(plot.getWidth(), plot.getHeight());
			stack.setColorModel(plot.getColorModel());			
			for(int i=1;i<=stackSource.getSize();i++)
				stack.addSlice(null, new byte[plot.getWidth()* plot.getHeight()]);
			stack.setPixels(plot.getPixels(), 1);
			IjxImagePlus plots = IJ.getFactory().newImagePlus("Surface Plot", stack);
			plots.show();
			for(int i=2;i<=stackSource.getSize();i++) {
				IJ.showStatus("Drawing slice " + i + "..." + " (" + (100*(i-1)/stackSource.getSize()) + "% done)");
				ip = stackSource.getProcessor(i);
				plot = makeSurfacePlot(ip);
				IjxImageWindow win = plots.getWindow();
				if (win!=null && win.isClosed()) break;
				stack.setPixels(plot.getPixels(), i);
				plots.setSlice(i);
			}
		} else {
			ImageProcessor plot = makeSurfacePlot(img.getProcessor());
			IJ.getFactory().newImagePlus("Surface Plot", plot).show();
		}
		
		Date end = new Date();
     		long lstart = start.getTime();
      		long lend = end.getTime();
      		long difference = lend - lstart;
		IJ.register(SurfacePlotter.class);
		IJ.showStatus("Done in "+difference+" msec." );
	}
	
	boolean showDialog() {
		GenericDialog gd = new GenericDialog("Surface Plotter");
		//gd.addNumericField("Plot Width (pixels):", plotWidth, 0);
		//gd.addNumericField("Angle (-90-90 degrees):", angleInDegrees, 0);
		gd.addNumericField("Polygon Multiplier (10-200%):", polygonMultiplier, 0);
		gd.addCheckbox("Draw_Wireframe", showWireframe);
		gd.addCheckbox("Shade", showGrayscale);
		gd.addCheckbox("Draw_Axis", showAxis);
		gd.addCheckbox("Source Background is Lighter", whiteBackground);
		gd.addCheckbox("Fill Plot Background with Black", blackFill);
		gd.addCheckbox("One Polygon Per Line", oneToOne);
		gd.addCheckbox("Smooth", smooth);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		//plotWidth = (int) gd.getNextNumber();
		//angleInDegrees = gd.getNextNumber();
		polygonMultiplier = (int)gd.getNextNumber();
		showWireframe = gd.getNextBoolean();
		showGrayscale = gd.getNextBoolean();
		showAxis = gd.getNextBoolean();
		whiteBackground = gd.getNextBoolean();
		blackFill = gd.getNextBoolean();
		oneToOne = gd.getNextBoolean();
		smooth = gd.getNextBoolean();
		if (showWireframe && !showGrayscale)
			blackFill = false;
		if (polygonMultiplier>400) polygonMultiplier = 400;
		if (polygonMultiplier<10) polygonMultiplier = 10;
		return true;
	}
	
	public ImageProcessor makeSurfacePlot(ImageProcessor ip) {
		ip = ip.duplicate();
		Rectangle roi = img.getProcessor().getRoi();
		ip.setRoi(roi);
		if (!(ip instanceof ByteProcessor)) {
			ip.setMinAndMax(img.getProcessor().getMin(), img.getProcessor().getMax());
			ip = ip.convertToByte(true);
			ip.setRoi(roi);
		}
		double angle = (angleInDegrees/360.0)*2.0*Math.PI;
		int polygons = (int)(plotWidth*(polygonMultiplier/100.0)/4);
		if (oneToOne)
			polygons = roi.height;
		double xinc = 0.8*plotWidth*Math.sin(angle)/polygons;
		double yinc = 0.8*plotWidth*Math.cos(angle)/polygons;
		IJ.showProgress(0.01);
		ip.setInterpolate(!oneToOne);
		ip = ip.resize(plotWidth, polygons);
		int width = ip.getWidth();
		int height = ip.getHeight();
		double min = ip.getMin();
		double max = ip.getMax();
				
		if(invertedLut) ip.invert();
		if(whiteBackground) ip.invert();
		if (smooth) ip.smooth();

		x = new int[width+2];
		y = new int[width+2];
		double xstart = 10.0;
		if (xinc<0.0)
			xstart += Math.abs(xinc)*polygons;
		ByteProcessor ipProfile =new ByteProcessor(width, (int)(256+width*yinc2));
		ipProfile.setValue(255);
		ipProfile.fill();
		double ystart =  yinc2*width;
		int ybase = (int)(ystart+0.5);
		int windowWidth =(int)(plotWidth+polygons*Math.abs(xinc) + 20.0);
		int windowHeight = (int)(ipProfile.getHeight()+polygons*yinc + 10.0);
		
		if(showAxis){
			xstart += 50+20;
			ystart += 10;
			windowWidth += 60+20;
			windowHeight += 20;
			p1x = xstart;
			p1y = ystart+255;
			p2x = xstart+xinc*height;;
			p2y = p1y+yinc*height;
			p3x =  p2x+width-1;
			p3y = p2y- yinc2*width;
		}
		
		if(showGrayscale) {
			int v;
			int[] column = new int[255];
			for(int row=0; row<255; row++) {
				if(whiteBackground)
					v = row;
				else
					v = 255-row;
				column[row] = v;
			}
			int base = ipProfile.getHeight()-255;		
			for(int col=0; col<width; col++) {
				ipProfile.putColumn(col, base-(int)(yinc2*col+0.5), column, 255);
			}
		} else {
			ipProfile.setValue(254);
			ipProfile.fill();
		}
		
		ipProfile.snapshot();
		
		ImageProcessor ip2 = new ByteProcessor(windowWidth, windowHeight);		
		if(showGrayscale) {
			ip2.setColorModel(ip.getColorModel());
			if(invertedLut)
				ip2.invertLut();	
			fixLut(ip2);
		}		
		if(!blackFill)
			ip2.setValue(255);
		else
			ip2.setValue(0);
		ip2.fill();
		
		for (int row=0; row<height; row++) {
			double[] profile = ip.getLine(0, row, width-1, row);
			clearAboveProfile(ipProfile, profile, width, yinc2);
			int ixstart = (int)(xstart+0.5);
			int iystart = (int)(ystart+0.5);
			
			ip2.copyBits(ipProfile, ixstart, iystart-ybase, Blitter.COPY_TRANSPARENT);
			ipProfile.reset();

			if (showWireframe) {
				ip2.setValue(0);
				double ydelta = 0.0;
				ip2.moveTo(ixstart, (int)(ystart+255.5 - profile[0]) );
				for(int i=1; i<width; i++) {
					ydelta += yinc2;
					ip2.lineTo( ixstart+i,  (int) (ystart+255.5-(profile[i]+ydelta)));
				}
				ip2.drawLine(ixstart, iystart+255, ixstart + width-1, (int)( ystart+255.5-ydelta) );
				ip2.drawLine( ixstart, iystart+255-(int) (profile[0]+0.5), ixstart, iystart+255 );
				ip2.drawLine( ixstart+width-1, (int) ( ystart+255.5-ydelta),  ixstart+width-1, (int) (ystart+255.5-(profile[width-1]+ydelta)) );
			}
			
			xstart += xinc;
			ystart += yinc;
			if ((row%10)==0) IJ.showProgress((double)row/height);
		}

		IJ.showProgress(1.0);		
		
		if(invertedLut) {
			ip.invert();
			ip.invertLut();
		}
		if(whiteBackground)
			ip.invert();
			
		if (showAxis) {
			if (!lut.isGrayscale() && showGrayscale)
				ip2 = ip2.convertToRGB();			
			drawAndLabelAxis(ip, ip2, roi);
		}

		if (img.getStackSize()==1)
			ip2 = trimPlot(ip2, ybase);
				
		return ip2;
	}

	void drawAndLabelAxis(ImageProcessor ip, ImageProcessor ip2, Rectangle roi) {			
		ip2.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
		if(!blackFill)	
			ip2.setColor(Color.black);
		else
			ip2.setColor(Color.white);			
		ip2.setAntialiasedText(true);
		String s;
		int w, h;
		Calibration cal = img.getCalibration();

		//z-axis & label
		s = cal.getValueUnit();
		if (s.equals("Gray Value"))
			s = "";
		w =  ip2.getFontMetrics().stringWidth(s);
		drawAxis(ip2, (int) p1x, (int) p1y-255, (int) p1x, (int) p1y , s, 10, -1, 0, 1);
		double min, max;
		if (img.getBitDepth()==8) {
			min = 0;
			max = 255;
		} else {
			min = img.getProcessor().getMin();
			max = img.getProcessor().getMax();
		}
		//IJ.log("");
		//IJ.log(min+"  "+max+"  "+cal.getCValue((int)min)+"  "+cal.getCValue((int)max));
		//ip2.putPixelValue(0,0,0);
		//boolean zeroIsBlack
		//IJ.log(ip2.getPixelValue(+"  "+max+"  "+cal.getCValue((int)min)+"  "+cal.getCValue((int)max));
		if (cal.calibrated()) {
			min = cal.getCValue((int)min);
			max = cal.getCValue((int)max);
		}
		//if (invertedPixelValues)
		//	{double t=max; max=min; min=t;}
		ip2.setAntialiasedText(true);
		s = String.valueOf( (double) Math.round(max*10)/10);
		w =  ip.getFontMetrics().stringWidth(s);
		h =  ip.getFontMetrics().getHeight();
		ip2.drawString(s, (int) p1x-18-w, (int) p1y-255 +h/2);	//ybase+5+h+(int ( yinc2/xinc *10));
		s = String.valueOf( (double) Math.round(min*10)/10);
		w =  ip2.getFontMetrics().stringWidth(s);
		ip2.drawString(s, (int) p1x-18-w, (int) p1y +h/2);
		
		//x-axis
		s = (double) Math.round(roi.height*cal.pixelHeight*10)/10+" "+cal.getUnits();
		w =  ip2.getFontMetrics().stringWidth(s);
		drawAxis(ip2, (int) p1x, (int) p1y, (int) p2x, (int) p2y, s, 10, -1, 1, 1);

		//y-axis
		s =  (double) Math.round(roi.width*cal.pixelWidth*10)/10+" "+cal.getUnits();
		w =  ip2.getFontMetrics().stringWidth(s);
		//drawAxis(ip2, (int) p2x, (int) p2y, (int) p3x, (int) p3y, s, 10, 1 , 1, -1);
		drawAxis(ip2, (int) p2x, (int) p2y, (int) p3x, (int) p3y, s, 10, 1, -1, 1);

	}

	void drawAxis(ImageProcessor ip, int x1, int y1, int x2, int y2, String label, int offset, int offsetXDirection, int offsetYDirection, int labelSide){
		if(blackFill)
			ip.setColor(Color.white);
		else
			ip.setColor(Color.black);		
		
		double m = -(double) (y2-y1)/(double) (x2-x1);
		
		if(m==0)
			m=.0001;
		double mTangent = -1/m;
		double theta = Math.atan(mTangent);
				
		int dy = -offsetXDirection * (int) ( 7*Math.sin(theta) );
		int dx = -offsetXDirection * (int) ( 7*Math.cos(theta) );
		
		x1 += offsetXDirection * (int) ( offset*Math.cos(theta) );
		x2 += offsetXDirection * (int) ( offset*Math.cos(theta) );
		
		y1 += offsetYDirection * (int) ( offset*Math.sin(theta) );
		y2 += offsetYDirection * (int) ( offset*Math.sin(theta) );		
		
		ip.drawLine(x1, y1, x2, y2);
		
		ip.drawLine(x1, y1, x1+dx, y1-dy);
		ip.drawLine(x2, y2, x2+dx, y2-dy);
		ImageProcessor ipText = drawString( ip, label, (int) (Math.atan(m)/2/Math.PI*360) );
		if(blackFill)
			ipText.invert();
				
		Blitter b;
		if (ip instanceof ByteProcessor)
			b = new ByteBlitter((ByteProcessor) ip);
		else
			b = new ColorBlitter((ColorProcessor) ip);
		Color c = blackFill?Color.black:Color.white;
		b.setTransparentColor(c);
		int xloc = (x1+x2)/2-ipText.getWidth()/2  + offsetXDirection*labelSide*(int)(15*Math.cos(theta));
		int yloc = (y1+y2)/2-ipText.getHeight()/2 + offsetYDirection*labelSide*(int)(15*Math.sin(theta));
		b.copyBits(ipText, xloc, yloc, Blitter.COPY_TRANSPARENT);
				
		return;						
	}	
	
	ImageProcessor drawString(ImageProcessor ip, String s, int a){
		int w =  ip.getFontMetrics().stringWidth(s);
		int h =  ip.getFontMetrics().getHeight();
		int ipW, ipH;
		
		double r = Math.sqrt( (w/2)*(w/2) + (h/2)*(h/2) );
		double aR = (a/360.0)*2.0*Math.PI;
		double aBaseR = Math.acos( (w/2)/r );
		
		ipW = (int) Math.abs(r*Math.cos(aBaseR+aR));
		ipH = (int) Math.abs(r*Math.sin(aBaseR+aR));
		
		if((int) Math.abs(r*Math.cos(-aBaseR+aR))>ipW)
			ipW = (int) Math.abs(r*Math.cos(-aBaseR+aR));
		if((int) Math.abs(r*Math.sin(-aBaseR+aR))>ipH)
			ipH = (int) Math.abs(r*Math.sin(-aBaseR+aR));
		
		ipW *= 2;
		ipH *= 2;
		
		int tW = w;
		if(ipW>w)
			tW = ipW;
		ImageProcessor ipText = new ByteProcessor(tW, ipH);
		ipText.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
		ipText.setColor(Color.white);
		ipText.fill();
		ipText.setColor(Color.black);
		ipText.setAntialiasedText(true);
		ipText.drawString(s, tW/2-w/2, ipH/2+h/2);
		ipText.setInterpolate(true);
		ipText.rotate(-a);
		ipText.setRoi(tW/2-ipW/2, 0, ipW, ipH);
		ipText = ipText.crop();
		
		//IJ.getFactory().newImagePlus("test", ipText).show();
		//ip.copyBits(ipText, x, y, Blitter.COPY_TRANSPARENT);

		return ipText;
	}
	
	void clearAboveProfile(ImageProcessor ipProfile, double[] profile, int width, double yinc2) {
		byte[] pixels = (byte[])ipProfile.getPixels();
		double ydelta = 0.0;
		int height = ipProfile.getHeight();
		for(int x=0; x<width; x++) {
			ydelta += yinc2;			
			int top = height - (int)(profile[x]+ydelta);
			for (int y=0,index=x; y<top; y++, index+=width)
				pixels[index] = (byte)255;
		}					
	}

	ImageProcessor trimPlot(ImageProcessor plot, int maxTrim) {
		int background = plot.getPixel(0, 0);
		int width = plot.getWidth();
		int height = plot.getHeight();
		int trim = maxTrim-5;
		a: for (int y=0; y<(maxTrim-5); y++)
			for (int x=0; x<width; x++)
				if (plot.getPixel(x,y)!=background)
					{trim = y-5; break a;}
		if (trim>10) {
			plot.setRoi(0, trim, width, height-trim);
			plot = plot.crop();
		}
		return plot;		
	}


	void fixLut(ImageProcessor ip) {
		if(!lut.isGrayscale() && lut.getMapSize() == 256){
			
			for(int y=0;y<ip.getHeight();y++){
				for(int x=0;x<ip.getWidth();x++){
					if(ip.getPixelValue(x, y)==0){
						ip.putPixelValue(x, y, 1);
					}else if(ip.getPixelValue(x, y)==255){
						ip.putPixelValue(x, y, 254);
					}
			
				}
			}
			
			byte[] rLUT = lut.getReds();			//new byte[256];
			byte[] gLUT = lut.getGreens();			//new byte[256];
			byte[] bLUT = lut.getBlues();			//new byte[256];
			
			rLUT[0] = (byte)0;
			gLUT[0] = (byte)0;
			bLUT[0] = (byte)0;
			rLUT[255] = (byte)255;
			gLUT[255] = (byte)255;
			bLUT[255] = (byte)255;
			
			ip.setColorModel(new IndexColorModel(8, 256, rLUT, gLUT, bLUT));
			
		}	
	}
	

}
