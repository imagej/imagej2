package ij.plugin.filter;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.ContrastEnhancer;
import java.awt.*;
import java.util.*;


/** This class implements the Process/FFT/Custom Filter command. */
public class FFTCustomFilter implements  IjxPlugInFilter, Measurements {

	private IjxImagePlus imp;
	private static int filterIndex = 1;
	private int slice;
	private int stackSize;	
	private boolean done;
	private ImageProcessor filter;
	private static boolean processStack;
	private boolean padded;
	private	int originalWidth;
	private int originalHeight;

	public int setup(String arg, IjxImagePlus imp) {
 		this.imp = imp;
 		if (imp==null)
 			{IJ.noImage(); return DONE;}
 		stackSize = imp.getStackSize();
		if (imp.getProperty("FHT")!=null) {
			IJ.error("FFT Custom Filter", "Ordinary (non-FFT) image required");
			return DONE;
		}
		else
			return processStack?DOES_ALL+DOES_STACKS:DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		slice++;
		if (done)
			return;
		FHT fht = newFHT(ip);
		if (slice==1) {
			filter = getFilter(fht.getWidth());
			if (filter==null) {
				done = true;
				return;
			}
		}
		((FHT)fht).transform();
		customFilter(fht);		
		doInverseTransform(fht, ip);
		if (slice==1)
			ip.resetMinAndMax();
		if (slice==stackSize)
			imp.updateAndDraw();
		IJ.showProgress(1.0);
	}
	
	void doInverseTransform(FHT fht, ImageProcessor ip) {
		showStatus("Inverse transform");
		fht.inverseTransform();
		if (fht.quadrantSwapNeeded)
			fht.swapQuadrants();
		fht.resetMinAndMax();
		ImageProcessor ip2 = fht;
		if (fht.originalWidth>0) {
			fht.setRoi(0, 0, fht.originalWidth, fht.originalHeight);
			ip2 = fht.crop();
		}
		int bitDepth = fht.originalBitDepth>0?fht.originalBitDepth:imp.getBitDepth();
		switch (bitDepth) {
			case 8: ip2 = ip2.convertToByte(true); break;
			case 16: ip2 = ip2.convertToShort(true); break;
			case 24:
				showStatus("Setting brightness");
				fht.rgb.setBrightness((FloatProcessor)ip2);
				ip2 = fht.rgb; 
				fht.rgb = null;
				break;
			case 32: break;
		}
		ip.insert(ip2, 0, 0);
	}

	FHT newFHT(ImageProcessor ip) {
		FHT fht;
		if (ip instanceof ColorProcessor) {
			showStatus("Extracting brightness");
			ImageProcessor ip2 = ((ColorProcessor)ip).getBrightness();
			fht = new FHT(pad(ip2));
			fht.rgb = (ColorProcessor)ip.duplicate(); // save so we can later update the brightness
		} else
			fht = new FHT(pad(ip));
		if (padded) {
			fht.originalWidth = originalWidth;
			fht.originalHeight = originalHeight;
		}
		fht.originalBitDepth = imp.getBitDepth();
		return fht;
	}
	
	ImageProcessor pad(ImageProcessor ip) {
		originalWidth = ip.getWidth();
		originalHeight = ip.getHeight();
		int maxN = Math.max(originalWidth, originalHeight);
		int i = 2;
		while(i<maxN) i *= 2;
		if (i==maxN && originalWidth==originalHeight) {
			padded = false;
			return ip;
		}
		maxN = i;
		showStatus("Padding to "+ maxN + "x" + maxN);
		ImageStatistics stats = ImageStatistics.getStatistics(ip, MEAN, null);
		ImageProcessor ip2 = ip.createProcessor(maxN, maxN);
		ip2.setValue(stats.mean);
		ip2.fill();
		ip2.insert(ip, 0, 0);
		padded = true;
		Undo.reset();
		//new ImagePlus("padded", ip2.duplicate()).show();
		return ip2;
	}
	
	void showStatus(String msg) {
		if (stackSize>1)
			IJ.showStatus("FFT: " + slice+"/"+stackSize);
		else
			IJ.showStatus(msg);
	}
		
	void customFilter(FHT fht) {
		int size = fht.getWidth();
		showStatus("Filtering");
		fht.swapQuadrants(filter);
		float[] fhtPixels = (float[])fht.getPixels();
		byte[] filterPixels = (byte[])filter.getPixels();
		for (int i=0; i<fhtPixels.length; i++)
			fhtPixels[i] = (float)(fhtPixels[i]*(filterPixels[i]&255)/255.0);
		fht.swapQuadrants(filter);
	}
	
	ImageProcessor getFilter(int size) {
		int[] wList = WindowManager.getIDList();
		if (wList==null || wList.length<2) {
			IJ.error("FFT", "A filter (as an open image) is required.");
			return null;
		}
		String[] titles = new String[wList.length];
		for (int i=0; i<wList.length; i++) {
			IjxImagePlus imp = WindowManager.getImage(wList[i]);
			titles[i] = imp!=null?imp.getTitle():"";
		}
		if (filterIndex<0 || filterIndex>=titles.length)
			filterIndex = 1;
		GenericDialog gd = new GenericDialog("FFT Filter");
		gd.addChoice("Filter:", titles, titles[filterIndex]);
		if (stackSize>1)
			gd.addCheckbox("Process Entire Stack", processStack);
		gd.showDialog();
		if (gd.wasCanceled())
			return null;
		filterIndex = gd.getNextChoiceIndex();
		if (stackSize>1)
			processStack = gd.getNextBoolean();
		IjxImagePlus filterImp = WindowManager.getImage(wList[filterIndex]);
		if (filterImp==imp) {
			IJ.error("FFT", "The filter cannot be the same as the image being filtered.");
			return null;
		}		
		if (filterImp.getStackSize()>1) {
			IJ.error("FFT", "The filter cannot be a stack.");
			return null;
		}		
		ImageProcessor filter = filterImp.getProcessor();		
		if (filter.getWidth()>size || filter.getHeight()>size) {
			IJ.error("FFT", "Filter cannot be larger than "+size+"x"+size);
			done = true;
			return null;
		}
		filter =  filter.convertToByte(true);		
		filter = padFilter(filter, size);
		//new ImagePlus("Padded Filter", filter.duplicate()).show();
		return filter;
	}
	
	ImageProcessor padFilter(ImageProcessor ip, int maxN) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		if (width==maxN && height==maxN)
			return ip;
		showStatus("Padding filter to "+ maxN + "x" + maxN);
		ImageStatistics stats = ImageStatistics.getStatistics(ip, MEAN, null);
		ImageProcessor ip2 = ip.createProcessor(maxN, maxN);
		ip2.setValue(stats.mean);
		ip2.fill();
		ip2.insert(ip, (maxN-width)/2, (maxN-height)/2);
		return ip2;
	}

	
}

