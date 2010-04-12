package plugin;


import ij.*;

import ij.plugin.*;
import ij.process.*;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;

import ij.process.ImageProcessor;



/*3D Image deconvolution.  Bob Dougherty.

Uses code from ImageJ and several plugins.

The following notice is from the FHT source code in ImageJ:

		This class contains a Java implementation of the Fast Hartley Transform.
		It is based on Pascal code in NIH Image contributed by Arlo Reeves
		(http://rsb.info.nih.gov/ij/docs/ImageFFT/). The Fast Hartley Transform was
		restricted by U.S. Patent No. 4,646,256, but was placed in the public domain
		by Stanford University in 1995 and is now freely available.


Version 0 5/1/2005.
Version 1 5/2/2005  Improved performce for 2D. Option to not show iterations.
Version 2 5/29/2005 Added Gaussian filter like DAMAS2/Iterative Deconvolution.
Version 3 5/30/2005 Improved user interface and recommended settings.  Corrected filters.
Version 4 6/3/2005  Option to skip Wiener filter.  Corrected normalization in iteration and overall.
					Changed convergence criteria.  Changed low pass filter definition.
Version 5 6/3/2005  Correced error in z filter.
Version 5.1 6/3/2005 Improved test for turning off Wiener filter
Version 5.2 6/4/2005 Changed recommended filter settings.

*/
/*	License:
	Copyright (c) 2005, OptiNav, Inc.
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

		Redistributions of source code must retain the above copyright
	notice, this list of conditions and the following disclaimer.
		Redistributions in binary form must reproduce the above copyright
	notice, this list of conditions and the following disclaimer in the
	documentation and/or other materials provided with the distribution.
		Neither the name of OptiNav, Inc. nor the names of its contributors
	may be used to endorse or promote products derived from this software
	without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
	"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
	A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
	CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
	EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
	PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
	PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
	LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
public class Iterative_Deconvolve_3D implements PlugIn{
 
	private FHT3DopenCL fht = new FHT3DopenCL();
    
    public void run(String arg)
	{
        if (arg.equalsIgnoreCase("about"))
		{
            showAbout();
            return;
        }
		
		if (IJ.versionLessThan("1.32c"))
			return;
		
 		int[] wList = WindowManager.getIDList();
        if (wList == null)
		{
            IJ.noImage();
            return;
        }
		
        String[] titles = new String[wList.length];
        for (int i = 0; i < wList.length; i++)
		{
            ImagePlus imp = WindowManager.getImage(wList[i]);
            if (imp != null)
                titles[i] = imp.getTitle();
            else
                titles[i] = "";
        }
		
		String titleImage = Prefs.get("iterativedeconvolve3d.titleImage", titles[0]);
		int imageChoice = 0;
		
        for (int i = 0; i < wList.length; i++)
		{
			if(titleImage.equals(titles[i]))
			{
				imageChoice = i;
				break;
			}
		}
		String titlePSF = Prefs.get("iterativedeconvolve3d.titlePSF", titles[0]);
		int psfChoice = 0;
        for (int i = 0; i < wList.length; i++)
		{
			if(titlePSF.equals(titles[i]))
			{
				psfChoice = i;
				break;
			}
		}
		
		int nIter = (int)Prefs.get("iterativedeconvolve3d.nIter", 100);
		//double filterSmallDia = Prefs.get("iterativedeconvolve3d.filterSmallDia", 2);
		float gamma = (float)Prefs.get("iterativedeconvolve3d.gamma", 0);
		double filterX = (double)Prefs.get("iterativedeconvolve3d.filterx", 1);
		double filterY = (double)Prefs.get("iterativedeconvolve3d.filtery", 1);
		double filterZ = (double)Prefs.get("iterativedeconvolve3d.filterz", 1);
		boolean normalize = Prefs.get("iterativedeconvolve3d.normalize", false);
		boolean logMean = Prefs.get("iterativedeconvolve3d.logmean", true);
		boolean showIteration = Prefs.get("iterativedeconvolve3d.showiteration", true);
		boolean antiRing = Prefs.get("iterativedeconvolve3d.antiring", true);
		double changeThreshPercent = Prefs.get("iterativedeconvolve3d.changethreshpercent", .01);
		boolean dB = Prefs.get("iterativedeconvolve3d.dB", false);
		boolean detectDivergence = Prefs.get("iterativedeconvolve3d.detectdivergence", true);

		GenericDialog gd = new GenericDialog("Deconvlove 3D (DAMAS3)", IJ.getInstance());
        gd.addChoice("Image",titles,titles[imageChoice]);
        gd.addChoice("Point Spread Function (Kernel)",titles,titles[psfChoice]);
       	gd.addStringField("Output title","Deconvolved",20);
       	String[] cbgl = new String[]{"Normalize PSF","Data (image, psf and result) in dB",
       		"Show iteration","Log mean pixel value to track convergence","Perform anti-ringing step",
       		"Detect divergence"};
       	
		boolean[] cbgd = new boolean[]{normalize,dB,showIteration,logMean,antiRing,detectDivergence};
	
		gd.addCheckboxGroup(3,2,cbgl,cbgd);
        gd.addNumericField("Wiener filter gamma (suggest 0 [< .0001] to turn off, 0.0001-0.1 as tests)", gamma, 3);
      	gd.addNumericField("Low pass filter x and y, pixels (suggest 1, 0 to turn off)", filterX, 1);
      	gd.addNumericField("Z_direction low pass filter z, pixels (suggest 1, 0 to turn off).", filterZ, 1);
		gd.addNumericField("Maximum number of iterations (suggest 100, 0 for filters only)", nIter,0);
		gd.addNumericField("Terminate iteration if mean delta < x% (suggest 0.01, 0 to turn off)", changeThreshPercent,3);
		gd.showDialog();

        if (gd.wasCanceled())
            return;
		
        ImagePlus impY = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
        ImagePlus impA = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
        String titleOut = gd.getNextString();
        normalize = gd.getNextBoolean();
       	dB = gd.getNextBoolean();
       	showIteration = gd.getNextBoolean();
       	logMean = gd.getNextBoolean();
       	antiRing = gd.getNextBoolean();
       	detectDivergence = gd.getNextBoolean();
 		gamma = (float)gd.getNextNumber();
      	boolean wiener = (gamma > 0.0001);
		filterX = gd.getNextNumber();
		//filterY = gd.getNextNumber();
		filterY = filterX;
		filterZ = gd.getNextNumber();
        nIter = (int)gd.getNextNumber();
		changeThreshPercent = gd.getNextNumber();

		Prefs.set("iterativedeconvolve3d.titleImage", impY.getTitle());
		Prefs.set("iterativedeconvolve3d.titlePSF", impA.getTitle());
		Prefs.set("iterativedeconvolve3d.gamma", gamma);
		Prefs.set("iterativedeconvolve3d.filterx", filterX);
		Prefs.set("iterativedeconvolve3d.filtery", filterY);
		Prefs.set("iterativedeconvolve3d.filterz", filterZ);
		Prefs.set("iterativedeconvolve3d.nIter", nIter);
		Prefs.set("iterativedeconvolve3d.normalize", normalize);
		Prefs.set("iterativedeconvolve3d.showiteration", showIteration);
		Prefs.set("iterativedeconvolve3d.logmean", logMean);
		Prefs.set("iterativedeconvolve3d.changethreshpercent", changeThreshPercent);
		Prefs.set("iterativedeconvolve3d.dB", dB);
		Prefs.set("iterativedeconvolve3d.antiring", antiRing);
		Prefs.set("iterativedeconvolve3d.detectdivergence", detectDivergence);

		ImageProcessor ipY = impY.getProcessor();
		ImageProcessor ipA = impA.getProcessor();
		
 		if(((ipY instanceof ColorProcessor)||(ipA instanceof ColorProcessor)))
		{
			IJ.showMessage("RGB images are not currently supported.");
			return;
		}

		ImageStack stackY = impY.getStack();
		int bw = stackY.getWidth();
		int bh = stackY.getHeight();
		int bd = impY.getStackSize();
		float[][] dataYin = new float[bd][];
		
		if(ipY instanceof FloatProcessor)
		{
			for (int i = 0; i < bd; i++)
			{
 				dataYin[i] = (float[])stackY.getProcessor(i+1).getPixels();
			}
		}
		else
		{
			for (int i = 0; i < bd; i++)
			{
 				dataYin[i] = (float[])stackY.getProcessor(i+1).convertToFloat().getPixels();
			}
		}

		ImageStack stackA = impA.getStack();
		int kw = stackA.getWidth();
		int kh = stackA.getHeight();
		int kd = impA.getStackSize();
		float[][] dataAin = new float[kd][];
		
		if(ipA instanceof FloatProcessor)
		{
			for (int i = 0; i < kd; i++){
 				dataAin[i] = (float[])stackA.getProcessor(i+1).getPixels();
			}
		}
		else
		{
			for (int i = 0; i < kd; i++)
			{
 				dataAin[i] = (float[])stackA.getProcessor(i+1).convertToFloat().getPixels();
			}
		}

		double minA = 0;
		double minY = 0;
		if(dB){
			minA = unDB(dataAin);
			minY = unDB(dataYin);
		}

		float[] aw = null,ah = null,ad = null;

		float scalePSF = 1;
		//if(normalize){
			float sum = 0;
			for (int k = 0; k < kd; k++)
			{
				for (int ind = 0; ind < kh*kw; ind++)
				{
					sum += dataAin[k][ind];
				}
			}
		
			if((sum != 0)&&normalize)scalePSF /= sum;
		//}
		int bwE = expandedSize(bw);
		int bhE = expandedSize(bh);
		int bdE = (bd == 1) ? 1 : expandedSize(bd);
		int kwE = expandedSize(kw);
		int khE = expandedSize(kh);
		int kdE = (kd == 1) ? 1 : expandedSize(kd);
		//w and h will always be at least 4.  d can be 1 as a special case.
		int w = (int)Math.max(bwE,kwE);
		int h = (int)Math.max(bhE,khE);
		int d = (int)Math.max(bdE,kdE);

		//Compute Gaussian filter weights
		float[] gi = new float[w];
		double ic = w/(filterX + 0.000001);
		for(int i = 0; i < w; i++)
		{
			int iShifted = i;
			if(iShifted > w/2) iShifted = w - iShifted;
			gi[i] = (float)Math.exp(-(iShifted/ic)*(iShifted/ic));
		}
		
		float[] gj = new float[h];
		double jc = h/(filterY + 0.000001);
		for(int j = 0; j < h; j++)
		{
			int jShifted = j;
			if(jShifted > h/2)jShifted = h - jShifted;
			gj[j] = (float)Math.exp(-(jShifted/jc)*(jShifted/jc));
		}
		
		float[] gk = new float[d];
		double kc = d/(filterZ + 0.000001);
		for(int k = 0; k < d; k++)
		{
			int kShifted = k;
			if(kShifted > d/2) kShifted = d - kShifted;
			gk[k] = (float)Math.exp(-(kShifted/kc)*(kShifted/kc));
		}
		
		IJ.showStatus("Creating expanded arrays");
		float[][] dataY = new float[d][w*h];
		copyDataMirror(bw,bh,bd,dataYin,w,h,d,dataY);
		float[][] dataA = new float[d][w*h];
		copyDataMask(kw,kh,kd,dataAin,w,h,d,dataA);

		IJ.showStatus("Swapping quadrants of the PSF");
		swapQuadrants(w,h,d,dataA);

		java.awt.image.ColorModel cmY = stackY.getProcessor(1).getColorModel();

		IJ.showStatus("Transforming PSF");
		FHT3D(dataA,w,h,d,false);
		float[][] dataX = new float[d][w*h];
		float[][] AX = new float[d][w*h];

		if(antiRing){
			//Anti-ringing step.
			IJ.showStatus("Performing anti-ringing step.");
			copyData(w,h,d,dataY,dataX);
			FHT3D(dataX,w,h,d,false);
			convolveFD(w,h,d,dataA,dataX,AX);
			FHT3D(AX,w,h,d,true);
			copyDataAverage(bw,bh,bd,w,h,d,sum,dataY,AX,dataY);
		}

		//Optional premultiplication step
		if(wiener){
			IJ.showStatus("Finding largest spectral element");
			double magMax = findMagMax(w,h,d,dataA);
			IJ.showStatus("Transforming blurred image");
			FHT3D(dataY,w,h,d,false);
			IJ.showStatus("Premultiplying PSF and blured image");
			
			//Use dataX storage temporarily for FD PSF (could be more efficient)
			//Use AX storage temporarily to store FD Y (could be more efficient)
			copyData(w,h,d,dataA,dataX);
			deconvolveFD(gamma,magMax,w,h,d,dataX,dataX,dataA);
			
			copyData(w,h,d,dataY,AX);
			deconvolveFD(gamma,magMax,w,h,d,AX,dataX,dataY);
			IJ.showStatus("Inverse transforming blurred image");
			FHT3D(dataY,w,h,d,true);
		}

		//Finished with optional premultiplication step

		int wh = w*h;
		int kOff = (d - bd + 1)/2;
		int jOff = (h - bh + 1)/2;
		int iOff = (w - bw + 1)/2;

		//Convert PSF back to the spatial domain in order to
		//compute aSum after the premultiplication step
		FHT3D(dataA,w,h,d,true);
		float aSum = 0;
		
		for(int k = 0; k < d; k++)
		{
			for (int ind = 0; ind < wh; ind++)
			{
				aSum += (float)Math.abs(dataA[k][ind]);
			}
		}
		
		//Apply scale factors
		if(scalePSF != 1)
		{
			IJ.showStatus("Normalizing");
			for (int k = 0; k < d; k++)
			{
				for (int ind = 0; ind < h*w; ind++)
				{
					dataY[k][ind] /= scalePSF;
				}
			}
		}
		
		FHT3D(dataA,w,h,d,false);
		copyData(w,h,d,dataY,dataX);
		ImageStack stackOutTemp = null;
		ImagePlus impOutTemp = null;
		
		if(showIteration)
		{
			stackOutTemp = new ImageStack(w,h);
			
			for (int k = 0; k < d; k++)
			{
				ImageProcessor ip = new FloatProcessor(w,h);
				float[] px = (float[])ip.getPixels();
				
				for (int j = 0; j < h; j++)
				{
					for (int i = 0; i < w; i++){
						px[i + w*j] = dataX[k][i + w*j];
					}
				}
				ip.setMinAndMax(0,0);
				ip.setColorModel(cmY);
				ip.setRoi(iOff,jOff,bw,bh);
				stackOutTemp.addSlice(null,ip);
			}
			
			impOutTemp = new ImagePlus(titleOut+"Temp",stackOutTemp);
			impOutTemp.setSlice(d/2 + 1);
			impOutTemp.show();
			ImageProcessor ip2 = impOutTemp.getProcessor();
			ip2.setMinAndMax(0,0);
			ip2.setColorModel(cmY);
			impOutTemp.updateAndDraw();
			impOutTemp.setRoi(iOff,jOff,bw,bh);
		}//if(showIteration)

		int iter;
		float oldPercentChange = Float.MAX_VALUE;
		
		for (iter = 0; iter < nIter; iter++)
		{
			IJ.showProgress((float)iter/nIter);
			IJ.showStatus("Starting iteration "+(iter+1)+" of "+nIter);
			
			
			FHT3D(dataX,w,h,d,false);
			
			//Gaussian filter
			for(int k = 0; k < d; k++)
			{
				for (int j = 0; j < h; j++)
				{
					for (int i = 0; i < w; i++)
					{
						dataX[k][i + w*j] *= gk[k]*gj[j]*gi[i];
					}
				}
			}
			
			convolveFD(w,h,d,dataA,dataX,AX);
			FHT3D(AX,w,h,d,true);
			FHT3D(dataX,w,h,d,true);
			float meanDelta = 0;
			float delta;
			for(int k = 0; k < d; k++)
			{
				for (int ind = 0; ind < wh; ind++)
				{
					delta = (dataY[k][ind] - AX[k][ind]/aSum);
					dataX[k][ind] += delta;
					if(dataX[k][ind] < 0)
					{
						dataX[k][ind] = 0;
					} else
					
					{
						meanDelta += Math.abs(delta);
					}
				}
			}
			
			if(showIteration)
			{
				for (int k = 0; k < d; k++)
				{
					ImageProcessor ip = stackOutTemp.getProcessor(k+1);
					float[] px = (float[])ip.getPixels();
					for (int j = 0; j < h; j++){
						for (int i = 0; i < w; i++){
							px[i + w*j] = dataX[k][i + w*j];
						}
					}
				}
				ImageProcessor ip1 = impOutTemp.getProcessor();
				ip1.setMinAndMax(0,0);
				ip1.setColorModel(cmY);
				impOutTemp.updateAndDraw();
				impOutTemp.setRoi(iOff,jOff,bw,bh);
			}
			
			//Energy sum to track convergence
			float sumPixels = 0;
			for (int k = 0; k < bd; k++)
			{
				for (int j = 0; j < bh; j++)
				{
					for (int i = 0; i < bw; i++)
					{
						sumPixels += dataX[k+kOff][i + iOff + w*(j+jOff)];
					}
				}
			}
			
			float percentChange = 100*meanDelta/sumPixels;
			if(logMean)IJ.write(Float.toString(percentChange));
			if((oldPercentChange - percentChange) < changeThreshPercent)
			{
				if(logMean)IJ.write("Automatically terminated after "+iter+" iterations.");
				break;
			}
			
			if((oldPercentChange < percentChange)&&detectDivergence)
			{
				if(logMean)IJ.write("Automatically terminated due to divergence "+iter+" iterations.");
				break;
			}
			
			oldPercentChange = percentChange;
			IJ.showStatus(iter+" iterations complete.");
		}
		
		IJ.showStatus("Final filter.");
		FHT3D(dataX,w,h,d,false);
		
		//Gaussian filter and scaling
		for(int k = 0; k < d; k++)
		{
			for (int j = 0; j < h; j++)
			{
				for (int i = 0; i < w; i++)
				{
					dataX[k][i + w*j] *= gk[k]*gj[j]*gi[i]/aSum;
				}
			}
		}
		
		FHT3D(dataX,w,h,d,true);
		IJ.showStatus("Done");
		
		if(dB)
		{
			toDB(dataAin, minA);
			toDB(dataYin, minY);
			toDB(dataX, -90);
		}

		//Crop the output to the size of Yin
		if(showIteration)impOutTemp.hide();
		ImageStack stackOut = new ImageStack(bw,bh);
		for (int k = 0; k < bd; k++)
		{
			ImageProcessor ip = new FloatProcessor(bw,bh);
			float[] px = (float[])ip.getPixels();
			for (int j = 0; j < bh; j++)
			{
				for (int i = 0; i < bw; i++)
				{
					px[i + bw*j] = dataX[k+kOff][i + iOff + w*(j+jOff)];
				}
			}
			stackOut.addSlice(null,ip);
		}
		
		ImagePlus impOut = new ImagePlus(titleOut+"_"+iter,stackOut);
		impOut.setSlice(bd/2+1);
		ImageProcessor ip = impOut.getProcessor();
		ip.setMinAndMax(0,0);
		ip.setColorModel(cmY);
		impOut.show();
	}//run
	void copyDataMask(int w, int h, int d,float[][] data,int wE,int hE,int dE,float[][] dataE)
	{
		int kOff = (dE - d + 1)/2;
		int jOff = (hE - h + 1)/2;
		int iOff = (wE - w + 1)/2;
		for(int k = 0; k < d; k++)
		{
			for (int j = 0; j < h; j++)
			{
				for (int i = 0; i < w; i++)
				{
					dataE[k+kOff][i+iOff + wE*(j+jOff)] = data[k][i + w*j];
				}
			}
		}
	}
	
	void copyDataAverage(int w, int h, int d,int wE,int hE,int dE,float sum, float[][] dataIn,float[][] dataOut,float[][] result)
	{
		int kOff = (dE - d + 1)/2;
		int jOff = (hE - h + 1)/2;
		int iOff = (wE - w + 1)/2;
		int iIn,jIn,kIn,iOut,jOut,kOut;
		float alphaI,alphaJ,alphaK;
		float a;
		for(int k = -kOff; k < dE-kOff; k++)
		{
			kOut = k + kOff;
			if(k < 0)
			{
				alphaK = -k/((float)kOff);
			}
			else if(k > (d-1))
			{
				alphaK = (k - d)/((float)kOff);
			}
			else
			{
				alphaK = 0;
			}
			
			for (int j = -jOff; j < hE-jOff; j++)
			{
				jOut = j + jOff;
				if(j < 0){
					alphaJ = -j/((float)jOff);
				}
				else if(j > (h-1))
				{
					alphaJ = (j - h)/((float)jOff);
				}
				else
				{
					alphaJ = 0;
				}
				for (int i = -iOff; i < wE-iOff; i++)
				{
					iOut = i + iOff;
					if(i < 0)
					{
						alphaI = -i/((float)iOff);
					}
					else if(i > (w-1))
					{
						alphaI = (i - w)/((float)iOff);
					}
					else
					{
						alphaI = 0;
					}
					a = alphaK;
					if(alphaJ > a) a = alphaJ;
					if(alphaI > a) a = alphaI;
					result[kOut][iOut + wE*jOut] = (1-a)*dataIn[kOut][iOut + wE*jOut] +
								a*dataOut[kOut][iOut + wE*jOut]/sum;
				}
			}
		}
	}
	
	void copyData(int w, int h, int d,float[][] data, float[][] data2)
	{
		int wh = w*h;
		for(int k = 0; k < d; k++)
		{
			for (int ind = 0; ind < wh; ind++)
			{
				data2[k][ind] = data[k][ind];
			}
		}
	}
	
	void copyDataMirror(int w, int h, int d,float[][] data,int wE,int hE,int dE,float[][] dataE)
	{
		int kOff = (dE - d + 1)/2;
		int jOff = (hE - h + 1)/2;
		int iOff = (wE - w + 1)/2;
		int iIn,jIn,kIn,iOut,jOut,kOut;
		for(int k = -kOff; k < dE-kOff; k++)
		{
			kOut = k + kOff;
			kIn = mirror(k,d);
			for (int j = -jOff; j < hE-jOff; j++)
			{
				jOut = j + jOff;
				jIn = mirror(j,h);
				for (int i = -iOff; i < wE-iOff; i++)
				{
					iOut = i + iOff;
					iIn = mirror(i,w);
					dataE[kOut][iOut + wE*jOut] = data[kIn][iIn + w*jIn];
				}
			}
		}
	}
	
	int mirror(int i, int n)
	{
		int ip = mod(i,2*n);
		if(ip < n){
			return ip;
		}
		else
		{
			return n - (ip % n) - 1;
		}
	}
	
	//A version of mod that is periodic for postive and negative i
	int mod(int i, int n)
	{
		return ((i % n) + n) % n;
	}
	
	int expandedSize(int maxN)
	{
		//Expand this to a power of 2 that is at least 1.5* as large, to avoid wrap effects
		//Start with 4 to avoid apparent normalization problems with n = 2
		int iN=4;
		if(maxN > 1)
		{
			while(iN<1.5 * maxN) iN *= 2;
		}
		return iN;
	}
	
	double unDB(float[][] x)
	{
		double result = Float.MAX_VALUE;
		int n = x.length;
		for (int i = 0; i < n; i++)
		{
			double ri = unDB(x[i]);
			if(ri < result) result = ri;
		}
		
		return result;
	}
	
	double unDB(float[] x)
	{
		double SCALE = 10/Math.log(10);
		int n = x.length;
		double result = Float.MAX_VALUE;
		for (int i = 0; i < n; i++)
		{
			if(x[i] < result) result = x[i];
			x[i] = (float)Math.exp(x[i]/SCALE);
		}
		return result;
	}
	
	void toDB(float[][] x, double minDB)
	{
		double SCALE = 10/Math.log(10);
		int n = x.length;
		for (int i = 0; i < n; i++)
		{
			toDB(x[i], minDB);
		}
	}
	
	void toDB(float[] x, double minDB)
	{
		double SCALE = 10/Math.log(10);
		double minVal = Math.exp(minDB/SCALE);
		int n = x.length;
		for (int i = 0; i < n; i++)
		{
			if(x[i] > minVal)
				x[i] = (float)(SCALE*Math.log(x[i]));
			else
				x[i] = (float)minDB;
		}
	}
	
	void swapQuadrants(int w,int h,int d,float[][] x)
	{
		int k1P,k2P,k3P;
		float temp;
		int wHalf = w/2;
		int hHalf = h/2;
		int dHalf = d/2;
		//Shift by half of the grid, less one pixel, in each direction
		for(int k3 = 0; k3 < dHalf; k3++)
		{
			k3P = k3 + dHalf;
			for (int k2 = 0; k2 < h; k2++)
			{
				for (int k1 = 0; k1 < w; k1++)
				{
					temp = x[k3][k1 + w*k2];
					x[k3][k1 + w*k2] = x[k3P][k1 + w*k2];
					x[k3P][k1 + w*k2] = temp;
				}
			}
		}
		
		for(int k2 = 0; k2 < hHalf; k2++)
		{
			k2P = k2 + hHalf;
			for (int k3 = 0; k3 < d; k3++)
			{
				for (int k1 = 0; k1 < w; k1++)
				{
					temp = x[k3][k1 + w*k2];
					x[k3][k1 + w*k2] = x[k3][k1 + w*k2P];
					x[k3][k1 + w*k2P] = temp;
				}
			}
		}
		
		for(int k1 = 0; k1 < wHalf; k1++)
		{
			k1P = k1 + wHalf;
			for (int k2 = 0; k2 < h; k2++)
			{
				for (int k3 = 0; k3 < d; k3++)
				{
					temp = x[k3][k1 + w*k2];
					x[k3][k1 + w*k2] = x[k3][k1P + w*k2];
					x[k3][k1P + w*k2] = temp;
				}
			}
		}
	}
	
	void convolveFD(int w,int h,int d,float[][] h1,float[][] h2, float[][] result)
	{
		int k1C,k2C,k3C;
		double h2e,h2o;
		for(int k3 = 0; k3 < d; k3++)
		{
			k3C = (d - k3) % d;
			for (int k2 = 0; k2 < h; k2++)
			{
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 < w; k1++)
				{
					k1C = (w - k1) % w;
					h2e = (h2[k3][k1 + w*k2] + h2[k3C][k1C + w*k2C])/2;
					h2o = (h2[k3][k1 + w*k2] - h2[k3C][k1C + w*k2C])/2;
					result[k3][k1 + w*k2] = (float)(h1[k3][k1 + w*k2]*h2e + h1[k3C][k1C + w*k2C]*h2o);
				}
			}
		}
	}
	
	void deconvolveFD(float gamma, double magMax, int w,int h,int d,float[][] h1,float[][] h2, float[][] result)
	{
		int k1C,k2C,k3C;
		double mag,h2e,h2o;
		double gammaScaled = gamma*magMax;
		for(int k3 = 0; k3 < d; k3++)
		{
			k3C = (d - k3) % d;
			for (int k2 = 0; k2 < h; k2++)
			{
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 < w; k1++)
				{
					k1C = (w - k1) % w;
					h2e = (h2[k3][k1 + w*k2] + h2[k3C][k1C + w*k2C])/2;
					h2o = (h2[k3][k1 + w*k2] - h2[k3C][k1C + w*k2C])/2;
					mag =h2[k3][k1 + w*k2]*h2[k3][k1 + w*k2] + h2[k3C][k1C + w*k2C]*h2[k3C][k1C + w*k2C];
					double tmp = h1[k3][k1 + w*k2]*h2e - h1[k3C][k1C + w*k2C]*h2o;
					result[k3][k1 + w*k2] = (float)(tmp/(mag+gammaScaled));
				}
			}
		}
	}
	
	double findMagMax(int w,int h,int d,float[][] h2)
	{
		int k1C,k2C,k3C;
		double magMax = 0;
		double mag;
		for(int k3 = 0; k3 < d; k3++)
		{
			k3C = (d - k3) % d;
			for (int k2 = 0; k2 < h; k2++)
			{
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 < w; k1++)
				{
					k1C = (w - k1) % w;
					mag =h2[k3][k1 + w*k2]*h2[k3][k1 + w*k2] + h2[k3C][k1C + w*k2C]*h2[k3C][k1C + w*k2C];
					if(mag > magMax) magMax = mag;
				}
			}
		}
		return magMax;
	}
	
    boolean powerOf2Size(int w) 
	{
        int i=2;
        while(i<w) i *= 2;
        return i==w;
    }
	
	public void FHT3D(float[][] data,int w, int h, int d, boolean inverse) 
	{
		//if using OpenCL
		if( fht.isUseOpenCL() )
		{
			FHT3DopenCL.FHT3DopenCL(data, w, h, d, inverse);
		}
		else
		{
			FHT3Djava(data, w, h, d, inverse);
		}
		
	}
	
	
	public void FHT3Djava(float[][] data,int w, int h, int d, boolean inverse)
	{
	
		float[] sw = new float[w/4];
		float[] cw = new float[w/4];
		float[] sh = new float[h/4];
		float[] ch = new float[h/4];
		makeSinCosTables(w,sw,cw);
		makeSinCosTables(h,sh,ch);
		
		for (int i = 0; i < d; i++)
		{
	 		rc2DFHT(data[i], w, h, sw, cw, sh, ch);
		}
		
		float[] u = new float[d];
		if(powerOf2Size(d))
		{
			float[] s = new float[d/4];
			float[] c = new float[d/4];
			
			makeSinCosTables(d,s,c);
			for(int k2 = 0; k2 < h; k2++)
			{
				for(int k1 = 0; k1 < w; k1++)
				{
					int ind = k1 + k2*w;
					for(int k3 = 0; k3 < d; k3++)
					{
						u[k3] = data[k3][ind];
					}
					
					dfht3(u, 0, d, s, c);
					for(int k3 = 0; k3 < d; k3++)
					{
						data[k3][ind] = u[k3];
					}
				}
			}
		}
		else
		{
			float[] cas = hartleyCoefs(d);
			float[] work = new float[d];
			for(int k2 = 0; k2 < h; k2++)
			{
				for(int k1 = 0; k1 < w; k1++)
				{
					int ind = k1 + k2*w;
					for(int k3 = 0; k3 < d; k3++)
					{
						u[k3] = data[k3][ind];
					}
					
					slowHT(u,cas,d,work);
					
					for(int k3 = 0; k3 < d; k3++)
					{
						data[k3][ind] = u[k3];
					}
				}
			}
		}
		
		//Convert to actual Hartley transform
		float A,B,C,D,E,F,G,H;
		int k1C,k2C,k3C;
		for(int k3 = 0; k3 <= d/2; k3++)
		{
			k3C = (d - k3) % d;
			for(int k2 = 0; k2 <= h/2; k2++)
			{
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 <= w/2; k1++)
				{
					k1C = (w - k1) % w;
					A = data[k3][k1 + w*k2C];
					B = data[k3][k1C + w*k2];
					C = data[k3C][k1 + w*k2];
					D = data[k3C][k1C + w*k2C];
					E = data[k3C][k1 + w*k2C];
					F = data[k3C][k1C + w*k2];
					G = data[k3][k1 + w*k2];
					H = data[k3][k1C + w*k2C];
					data[k3][k1 + w*k2] = (A+B+C-D)/2;
					data[k3C][k1 + w*k2] = (E+F+G-H)/2;
					data[k3][k1 + w*k2C] = (G+H+E-F)/2;
					data[k3C][k1 + w*k2C] = (C+D+A-B)/2;
					data[k3][k1C + w*k2] = (H+G+F-E)/2;
					data[k3C][k1C + w*k2] = (D+C+B-A)/2;
					data[k3][k1C + w*k2C] = (B+A+D-C)/2;
					data[k3C][k1C + w*k2C] = (F+E+H-G)/2;
				}
			}
		}
		if(inverse)
		{
			//float norm = (float)Math.sqrt(d*h*w);
			float norm = d*h*w;
			for(int k3 = 0; k3 < d; k3++)
			{
				for(int k2 = 0; k2 < h; k2++)
				{
					for (int k1 = 0; k1 < w; k1++)
					{
						data[k3][k1 + w*k2] /= norm;
					}
				}
			}
		}
	}
	
	 float[] hartleyCoefs(int max)
	{
		float[] cas = new float[max*max];
		int ind = 0;
		for(int n = 0; n < max; n++)
		{
			for (int k = 0; k < max; k++)
			{
				double arg = (2*Math.PI*k*n)/max;
				cas[ind++] = (float)(Math.cos(arg) + Math.sin(arg));
			}
		}
		return cas;
	}
	
	 void slowHT(float[] u, float[] cas, int max, float[] work)
	{
		int ind = 0;
		for(int k = 0; k < max; k++)
		{
			float sum = 0;
			for(int n = 0; n < max; n++)
			{
				sum += u[n]*cas[ind++];
			}
			work[k] = sum;
		}
		for (int k = 0; k < max; k++)
		{
			u[k] = work[k];
		}
	}
	
	 void makeSinCosTables(int maxN, float[] s, float[] c) 
	{
		int n = maxN/4;
		double theta = 0.0;
		double dTheta = 2.0 * Math.PI/maxN;
		for (int i=0; i<n; i++) 
		{
			c[i] = (float)Math.cos(theta);
			s[i] = (float)Math.sin(theta);
			theta += dTheta;
		}
	}
	
	/** Row-column Fast Hartley Transform */
 void rc2DFHT(float[] x, int w, int h, float[] sw, float[] cw, float[] sh, float[] ch) 
	{
		for (int row=0; row<h; row++)
			dfht3(x, row*w, w, sw, cw);
		float[] temp = new float[h];
		for(int col = 0; col < w; col++)
		{
			for (int row = 0; row < h; row++)
			{
				temp[row] = x[col + w*row];
			}
			
			dfht3(temp, 0, h, sh, ch);
			for (int row = 0; row < h; row++)
			{
				x[col + w*row] = temp[row];
			}
		}
	}
	
	/* An optimized real FHT */
	public void dfht3 (float[] x, int base, int maxN, float[] s, float[] c) 
	{
		int i, stage, gpNum, gpIndex, gpSize, numGps, Nlog2;
		int bfNum, numBfs;
		int Ad0, Ad1, Ad2, Ad3, Ad4, CSAd;
		float rt1, rt2, rt3, rt4;

		Nlog2 = log2(maxN);
		BitRevRArr(x, base, Nlog2, maxN);	//bitReverse the input array
		gpSize = 2;     //first & second stages - do radix 4 butterflies once thru
		numGps = maxN / 4;
		for (gpNum=0; gpNum<numGps; gpNum++)  
		{
			Ad1 = gpNum * 4;
			Ad2 = Ad1 + 1;
			Ad3 = Ad1 + gpSize;
			Ad4 = Ad2 + gpSize;
			rt1 = x[base+Ad1] + x[base+Ad2];   // a + b
			rt2 = x[base+Ad1] - x[base+Ad2];   // a - b
			rt3 = x[base+Ad3] + x[base+Ad4];   // c + d
			rt4 = x[base+Ad3] - x[base+Ad4];   // c - d
			x[base+Ad1] = rt1 + rt3;      // a + b + (c + d)
			x[base+Ad2] = rt2 + rt4;      // a - b + (c - d)
			x[base+Ad3] = rt1 - rt3;      // a + b - (c + d)
			x[base+Ad4] = rt2 - rt4;      // a - b - (c - d)
		}
		
		if (Nlog2 > 2) 
		{
			 // third + stages computed here
			gpSize = 4;
			numBfs = 2;
			numGps = numGps / 2;
			//IJ.write("FFT: dfht3 "+Nlog2+" "+numGps+" "+numBfs);
			for (stage=2; stage<Nlog2; stage++) 
			{
				for (gpNum=0; gpNum<numGps; gpNum++) 
				{
					Ad0 = gpNum * gpSize * 2;
					Ad1 = Ad0;     // 1st butterfly is different from others - no mults needed
					Ad2 = Ad1 + gpSize;
					Ad3 = Ad1 + gpSize / 2;
					Ad4 = Ad3 + gpSize;
					rt1 = x[base+Ad1];
					x[base+Ad1] = x[base+Ad1] + x[base+Ad2];
					x[base+Ad2] = rt1 - x[base+Ad2];
					rt1 = x[base+Ad3];
					x[base+Ad3] = x[base+Ad3] + x[base+Ad4];
					x[base+Ad4] = rt1 - x[base+Ad4];
					for (bfNum=1; bfNum<numBfs; bfNum++) 
					{
					// subsequent BF's dealt with together
						Ad1 = bfNum + Ad0;
						Ad2 = Ad1 + gpSize;
						Ad3 = gpSize - bfNum + Ad0;
						Ad4 = Ad3 + gpSize;

						CSAd = bfNum * numGps;
						rt1 = x[base+Ad2] * c[CSAd] + x[base+Ad4] * s[CSAd];
						rt2 = x[base+Ad4] * c[CSAd] - x[base+Ad2] * s[CSAd];

						x[base+Ad2] = x[base+Ad1] - rt1;
						x[base+Ad1] = x[base+Ad1] + rt1;
						x[base+Ad4] = x[base+Ad3] + rt2;
						x[base+Ad3] = x[base+Ad3] - rt2;

					} /* end bfNum loop */
				} /* end gpNum loop */
				
				gpSize *= 2;
				numBfs *= 2;
				numGps = numGps / 2;
				
			} /* end for all stages */
		} /* end if Nlog2 > 2 */
	}
	
  int log2 (int x) 
	{
		int count = 15;
		while (!btst(x, count))
			count--;
		return count;
	}
	
	private boolean btst (int  x, int bit) 
	{
		//int mask = 1;
		return ((x & (1<<bit)) != 0);
	}
	
	 void BitRevRArr (float[] x, int base, int bitlen, int maxN) 
	{
		int    l;
		float[] tempArr = new float[maxN];
		for (int i=0; i<maxN; i++)  
		{
			l = BitRevX (i, bitlen);  //i=1, l=32767, bitlen=15
			tempArr[i] = x[base+l];
		}
		
		for (int i=0; i<maxN; i++)
			x[base+i] = tempArr[i];
	}
	
	private int BitRevX (int  x, int bitlen) 
	{
		int  temp = 0;
		for (int i=0; i<=bitlen; i++)
			if ((x & (1<<i)) !=0)
				temp  |= (1<<(bitlen-i-1));
		return temp & 0x0000ffff;
	}
	
	private int bset (int x, int bit) 
	{
		x |= (1<<bit);
		return x;
	}
	
    static public void showAbout()
	{
        IJ.showMessage( "About Iterative Decon 3D ...",
                        "Iterative convolution and positive deconvolution\n" +
                        "in 3D");
    }
    

 }//Iterative_Deconvolve_3D