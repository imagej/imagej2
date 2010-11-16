package ij.plugin.filter;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.ContrastEnhancer;
import ijx.IjxImagePlus;
import java.awt.*;

/** 
This class implements the Process/FFT/Bandpass Filter command. It is based on 
Joachim Walter's FFT Filter plugin at "http://rsb.info.nih.gov/ij/plugins/fft-filter.html".
2001/10/29: First Version (JW)
2003/02/06: 1st bugfix (works in macros/plugins, works on stacks, overwrites image(=>filter)) (JW)
2003/07/03: integrated into ImageJ, added "Display Filter" option (WSR)
2007/03/26: 2nd bugfix (Fixed incorrect calculation of filter from structure sizes, which caused
            the real structure sizes to be wrong by a factor of 0.75 to 1.5 depending on the image size.)
*/
public class FFTFilter implements  PlugInFilter, Measurements {

	private IjxImagePlus imp;
	private String arg;
	private static int filterIndex = 1;
	private FHT fht;
	private int slice;
	private int stackSize = 1;	
	
	private static double filterLargeDia = 40.0;
	private static double  filterSmallDia = 3.0;
	private static int choiceIndex = 0;
	private static String[] choices = {"None","Horizontal","Vertical"};
	private static String choiceDia = choices[0];
	private static double toleranceDia = 5.0;
	private static boolean doScalingDia = true;
	private static boolean saturateDia = true;
	private static boolean displayFilter;
	private static boolean processStack;

	public int setup(String arg, IjxImagePlus imp) {
		this.arg = arg;
 		this.imp = imp;
 		if (imp==null)
 			{IJ.noImage(); return DONE;}
 		stackSize = imp.getStackSize();
		fht  = (FHT)imp.getProperty("FHT");
		if (fht!=null) {
			IJ.error("FFT Filter", "Spatial domain image required");
			return DONE;
		}
		if (!showBandpassDialog(imp))
			return DONE;
		else
			return processStack?DOES_ALL+DOES_STACKS:DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		slice++;
		filter(ip);
	}
	
	void filter(ImageProcessor ip) {
		ImageProcessor ip2 = ip;
		if (ip2 instanceof ColorProcessor) {
			showStatus("Extracting brightness");
			ip2 = ((ColorProcessor)ip2).getBrightness();
		} 
		Rectangle roiRect = ip2.getRoi();		
		int maxN = Math.max(roiRect.width, roiRect.height);
		double sharpness = (100.0 - toleranceDia) / 100.0;
		boolean doScaling = doScalingDia;
		boolean saturate = saturateDia;
		
		IJ.showProgress(1,20);

		/* 	tile mirrored image to power of 2 size		
			first determine smallest power 2 >= 1.5 * image width/height
		  	factor of 1.5 to avoid wrap-around effects of Fourier Trafo */

		int i=2;
		while(i<1.5 * maxN) i *= 2;		
        
        // Calculate the inverse of the 1/e frequencies for large and small structures.
        double filterLarge = 2.0*filterLargeDia / (double)i;
        double filterSmall = 2.0*filterSmallDia / (double)i;
        
		// fit image into power of 2 size 
		Rectangle fitRect = new Rectangle();
		fitRect.x = (int) Math.round( (i - roiRect.width) / 2.0 );
		fitRect.y = (int) Math.round( (i - roiRect.height) / 2.0 );
		fitRect.width = roiRect.width;
		fitRect.height = roiRect.height;
		
		// put image (ROI) into power 2 size image
		// mirroring to avoid wrap around effects
		showStatus("Pad to "+i+"x"+i);
		ip2 = tileMirror(ip2, i, i, fitRect.x, fitRect.y);
		IJ.showProgress(2,20);
		
		// transform forward
		showStatus(i+"x"+i+" forward transform");
		FHT fht = new FHT(ip2);
		fht.setShowProgress(false);
		fht.transform();
		IJ.showProgress(9,20);
		//IJ.getFactory().newImagePlus("after fht",ip2.crop()).show();	

		// filter out large and small structures
		showStatus("Filter in frequency domain");
		filterLargeSmall(fht, filterLarge, filterSmall, choiceIndex, sharpness);
		//IJ.getFactory().newImagePlus("filter",ip2.crop()).show();
		IJ.showProgress(11,20);

		// transform backward
		showStatus("Inverse transform");
		fht.inverseTransform();
		IJ.showProgress(19,20);
		//IJ.getFactory().newImagePlus("after inverse",ip2).show();	
		
		// crop to original size and do scaling if selected
		showStatus("Crop and convert to original type");
		fht.setRoi(fitRect);
		ip2 = fht.crop();
		if (doScaling) {
			IjxImagePlus imp2 = IJ.getFactory().newImagePlus(imp.getTitle()+"-filtered", ip2);
			new ContrastEnhancer().stretchHistogram(imp2, saturate?1.0:0.0);
			ip2 = imp2.getProcessor();
		}

		// convert back to original data type
		int bitDepth = imp.getBitDepth(); 
		switch (bitDepth) {
			case 8: ip2 = ip2.convertToByte(doScaling); break;
			case 16: ip2 = ip2.convertToShort(doScaling); break;
			case 24:
				ip.snapshot();
				showStatus("Setting brightness");
				((ColorProcessor)ip).setBrightness((FloatProcessor)ip2);
				break;
			case 32: break;
		}

		// copy filtered image back into original image
		if (bitDepth!=24) {
			ip.snapshot();
			ip.copyBits(ip2, roiRect.x, roiRect.y, Blitter.COPY);
		}
		ip.resetMinAndMax();
		IJ.showProgress(20,20);
	}
	
	void showStatus(String msg) {
		if (stackSize>1 && processStack)
			IJ.showStatus("FFT Filter: "+slice+"/"+stackSize);
		else
			IJ.showStatus(msg);
	}

	/** Puts ImageProcessor (ROI) into a new ImageProcessor of size width x height y at position (x,y).
	The image is mirrored around its edges to avoid wrap around effects of the FFT. */
	public ImageProcessor tileMirror(ImageProcessor ip, int width, int height, int x, int y) {
			
		if (x < 0 || x > (width -1) || y < 0 || y > (height -1)) {
			IJ.error("Image to be tiled is out of bounds.");
			return null;
		}
		
		ImageProcessor ipout = ip.createProcessor(width, height);
		
		ImageProcessor ip2 = ip.crop();
		int w2 = ip2.getWidth();
		int h2 = ip2.getHeight();
				
		//how many times does ip2 fit into ipout?
		int i1 = (int) Math.ceil(x / (double) w2);
		int i2 = (int) Math.ceil( (width - x) / (double) w2);
		int j1 = (int) Math.ceil(y / (double) h2);
		int j2 = (int) Math.ceil( (height - y) / (double) h2);		

		//tile		
		if ( (i1%2) > 0.5)
			ip2.flipHorizontal();
		if ( (j1%2) > 0.5)
			ip2.flipVertical();
					
		for (int i=-i1; i<i2; i += 2) {
			for (int j=-j1; j<j2; j += 2) {
				ipout.insert(ip2, x-i*w2, y-j*h2);
			}
		}
		
		ip2.flipHorizontal();
		for (int i=-i1+1; i<i2; i += 2) {
			for (int j=-j1; j<j2; j += 2) {
				ipout.insert(ip2, x-i*w2, y-j*h2);
			}
		}
		
		ip2.flipVertical();
		for (int i=-i1+1; i<i2; i += 2) {
			for (int j=-j1+1; j<j2; j += 2) {
				ipout.insert(ip2, x-i*w2, y-j*h2);
			}
		}
		
		ip2.flipHorizontal();
		for (int i=-i1; i<i2; i += 2) {
			for (int j=-j1+1; j<j2; j += 2) {
				ipout.insert(ip2, x-i*w2, y-j*h2);
			}
		}
		
		return ipout;
	}		
	

	/*
	filterLarge: down to which size are large structures suppressed?
	filterSmall: up to which size are small structures suppressed?
	filterLarge and filterSmall are given as fraction of the image size 
				in the original (untransformed) image.
	stripesHorVert: filter out: 0) nothing more  1) horizontal  2) vertical stripes
				(i.e. frequencies with x=0 / y=0)
	scaleStripes: width of the stripe filter, same unit as filterLarge
	*/
	void filterLargeSmall(ImageProcessor ip, double filterLarge, double filterSmall, int stripesHorVert, double scaleStripes) {
		
		int maxN = ip.getWidth();
			
		float[] fht = (float[])ip.getPixels();
		float[] filter = new float[maxN*maxN];
		for (int i=0; i<maxN*maxN; i++)
			filter[i]=1f;		

		int row;
		int backrow;
		float rowFactLarge;
		float rowFactSmall;
		
		int col;
		int backcol;
		float factor;
		float colFactLarge;
		float colFactSmall;
		
		float factStripes;
		
		// calculate factor in exponent of Gaussian from filterLarge / filterSmall

		double scaleLarge = filterLarge*filterLarge;
		double scaleSmall = filterSmall*filterSmall;
		scaleStripes = scaleStripes*scaleStripes;
		//float FactStripes;

		// loop over rows
		for (int j=1; j<maxN/2; j++) {
			row = j * maxN;
			backrow = (maxN-j)*maxN;
			rowFactLarge = (float) Math.exp(-(j*j) * scaleLarge);
			rowFactSmall = (float) Math.exp(-(j*j) * scaleSmall);
			

			// loop over columns
			for (col=1; col<maxN/2; col++){
				backcol = maxN-col;
				colFactLarge = (float) Math.exp(- (col*col) * scaleLarge);
				colFactSmall = (float) Math.exp(- (col*col) * scaleSmall);
				factor = (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall;
				switch (stripesHorVert) {
					case 1: factor *= (1 - (float) Math.exp(- (col*col) * scaleStripes)); break;// hor stripes
					case 2: factor *= (1 - (float) Math.exp(- (j*j) * scaleStripes)); // vert stripes
				}
				
				fht[col+row] *= factor;
				fht[col+backrow] *= factor;
				fht[backcol+row] *= factor;
				fht[backcol+backrow] *= factor;
				filter[col+row] *= factor;
				filter[col+backrow] *= factor;
				filter[backcol+row] *= factor;
				filter[backcol+backrow] *= factor;
			}
		}

		//process meeting points (maxN/2,0) , (0,maxN/2), and (maxN/2,maxN/2)
		int rowmid = maxN * (maxN/2);
		rowFactLarge = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleLarge);
		rowFactSmall = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleSmall);	
		factStripes = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleStripes);
		
		fht[maxN/2] *= (1 - rowFactLarge) * rowFactSmall; // (maxN/2,0)
		fht[rowmid] *= (1 - rowFactLarge) * rowFactSmall; // (0,maxN/2)
		fht[maxN/2 + rowmid] *= (1 - rowFactLarge*rowFactLarge) * rowFactSmall*rowFactSmall; // (maxN/2,maxN/2)
		filter[maxN/2] *= (1 - rowFactLarge) * rowFactSmall; // (maxN/2,0)
		filter[rowmid] *= (1 - rowFactLarge) * rowFactSmall; // (0,maxN/2)
		filter[maxN/2 + rowmid] *= (1 - rowFactLarge*rowFactLarge) * rowFactSmall*rowFactSmall; // (maxN/2,maxN/2)

		switch (stripesHorVert) {
			case 1: fht[maxN/2] *= (1 - factStripes);
					fht[rowmid] = 0;
					fht[maxN/2 + rowmid] *= (1 - factStripes);
					filter[maxN/2] *= (1 - factStripes);
					filter[rowmid] = 0;
					filter[maxN/2 + rowmid] *= (1 - factStripes);
					break; // hor stripes
			case 2: fht[maxN/2] = 0;
					fht[rowmid] *=  (1 - factStripes);
					fht[maxN/2 + rowmid] *= (1 - factStripes);
					filter[maxN/2] = 0;
					filter[rowmid] *=  (1 - factStripes);
					filter[maxN/2 + rowmid] *= (1 - factStripes);
					break; // vert stripes
		}		
		
		//loop along row 0 and maxN/2	
		rowFactLarge = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleLarge);
		rowFactSmall = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleSmall);			
		for (col=1; col<maxN/2; col++){
			backcol = maxN-col;
			colFactLarge = (float) Math.exp(- (col*col) * scaleLarge);
			colFactSmall = (float) Math.exp(- (col*col) * scaleSmall);
			
			switch (stripesHorVert) {
				case 0:
					fht[col] *= (1 - colFactLarge) * colFactSmall;
					fht[backcol] *= (1 - colFactLarge) * colFactSmall;
					fht[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall;
					fht[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall;
					filter[col] *= (1 - colFactLarge) * colFactSmall;
					filter[backcol] *= (1 - colFactLarge) * colFactSmall;
					filter[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall;
					filter[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall;	
					break;			
				case 1:
					factStripes = (float) Math.exp(- (col*col) * scaleStripes);
					fht[col] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes);
					fht[backcol] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes);
					fht[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
					fht[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
					filter[col] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes);
					filter[backcol] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes);
					filter[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
					filter[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
					break;
				case 2:
					factStripes = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleStripes); 
					fht[col] = 0;
					fht[backcol] = 0;
					fht[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
					fht[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
					filter[col] = 0;
					filter[backcol] = 0;
					filter[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
					filter[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
			}
		}
		
		// loop along column 0 and maxN/2
		colFactLarge = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleLarge);
		colFactSmall = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleSmall);
		for (int j=1; j<maxN/2; j++) {
			row = j * maxN;
			backrow = (maxN-j)*maxN;
			rowFactLarge = (float) Math.exp(- (j*j) * scaleLarge);
			rowFactSmall = (float) Math.exp(- (j*j) * scaleSmall);

			switch (stripesHorVert) {
				case 0:
					fht[row] *= (1 - rowFactLarge) * rowFactSmall;
					fht[backrow] *= (1 - rowFactLarge) * rowFactSmall;
					fht[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall;
					fht[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall;
					filter[row] *= (1 - rowFactLarge) * rowFactSmall;
					filter[backrow] *= (1 - rowFactLarge) * rowFactSmall;
					filter[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall;
					filter[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall;
					break;
				case 1:
					factStripes = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleStripes);
					fht[row] = 0;
					fht[backrow] = 0;
					fht[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
					fht[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
					filter[row] = 0;
					filter[backrow] = 0;
					filter[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
					filter[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
					break;
				case 2:
					factStripes = (float) Math.exp(- (j*j) * scaleStripes);
					fht[row] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes);
					fht[backrow] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes);
					fht[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
					fht[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
					filter[row] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes);
					filter[backrow] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes);
					filter[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
					filter[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);	
			}
		}
		if (displayFilter && slice==1) {
			FHT f = new FHT(new FloatProcessor(maxN, maxN, filter, null));
			f.swapQuadrants();
			IJ.getFactory().newImagePlus("Filter", f).show();
		}
	}	

	boolean showBandpassDialog(IjxImagePlus imp) {
		GenericDialog gd = new GenericDialog("FFT Bandpass Filter");
		gd.addNumericField("Filter_Large Structures Down to", filterLargeDia, 0, 4, "pixels");
		gd.addNumericField("Filter_Small Structures Up to", filterSmallDia, 0, 4, "pixels");
		gd.addChoice("Suppress Stripes:", choices, choiceDia);
		gd.addNumericField("Tolerance of Direction:", toleranceDia, 0, 2, "%");
		gd.addCheckbox("Autoscale After Filtering", doScalingDia);
		gd.addCheckbox("Saturate Image when Autoscaling", saturateDia);
		gd.addCheckbox("Display Filter", displayFilter);
		if (stackSize>1)
			gd.addCheckbox("Process Entire Stack", processStack);	
		gd.showDialog();
		if(gd.wasCanceled())
			return false;
		if(gd.invalidNumber()) {
			IJ.error("Error", "Invalid input number");
			return false;
		}				
		filterLargeDia = gd.getNextNumber();
		filterSmallDia = gd.getNextNumber();	
		choiceIndex = gd.getNextChoiceIndex();
		choiceDia = choices[choiceIndex];
		toleranceDia = gd.getNextNumber();
		doScalingDia = gd.getNextBoolean();
		saturateDia = gd.getNextBoolean();
		displayFilter = gd.getNextBoolean();
		if (stackSize>1)
			processStack = gd.getNextBoolean();
		return true;
	}

}

