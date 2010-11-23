package ijx.process;
import ijx.IJ;
import ij.*;
import ijx.plugin.FFT;
import ijx.plugin.ContrastEnhancer;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import java.awt.image.ColorModel; 

/**
This class contains a Java implementation of the Fast Hartley
Transform. It is based on Pascal code in NIH Image contributed 
by Arlo Reeves (http://rsb.info.nih.gov/ij/docs/ImageFFT/).
The Fast Hartley Transform was restricted by U.S. Patent No. 4,646,256, 
but was placed in the public domain by Stanford University in 1995 
and is now freely available.
 
*/
public class FHT extends FloatProcessor {
	private boolean isFrequencyDomain;
	private int maxN;
	private float[] C;
	private float[] S;
	private int[] bitrev;
	private float[] tempArr;
	private boolean showProgress = true;
	
	/** Used by the FFT class. */
	public boolean quadrantSwapNeeded;
	/** Used by the FFT class. */
	public ColorProcessor rgb;
	/** Used by the FFT class. */
	public int originalWidth;
	/** Used by the FFT class. */
	public int originalHeight;
	/** Used by the FFT class. */
	public int originalBitDepth;
	/** Used by the FFT class. */
	public ColorModel originalColorModel;

	/** Constructs a FHT object from an ImageProcessor. Byte, short and RGB images 
		are converted to float. Float images are duplicated. */
	public FHT(ImageProcessor ip) {
		super(ip.getWidth(), ip.getHeight(), (float[])((ip instanceof FloatProcessor)?ip.duplicate().getPixels():ip.convertToFloat().getPixels()), null);
		maxN = getWidth();
		resetRoi();
		//IJ.log("FHT: "+maxN);
	}

	/** Returns true of this FHT contains a square image with a width that is a power of two. */
	public boolean powerOf2Size() {
		int i=2;
		while(i<width) i *= 2;
		return i==width && width==height;
	}

	/** Performs a foreward transform, converting this image into the frequency domain. 
		The image contained in this FHT must be square and its width must be a power of 2. */
	public void transform() {
		transform(false);
	}

	/** Performs an inverse transform, converting this image into the space domain. 
		The image contained in this FHT must be square and its width must be a power of 2. */
	public void inverseTransform() {
		transform(true);
	}
	
	/** Returns an inverse transform of this image, which is assumed to be in the frequency domain. */ 
	//public FloatProcessor getInverseTransform() {
	//	if (!isFrequencyDomain) {
	//		throw new  IllegalArgumentException("Frequency domain image required");
	//	snapshot();
	//	transform(true);
	//	FloatProcessor fp = this.duplicate();
	//	reset();
	//	isFrequencyDomain = true;
	//}


	void transform(boolean inverse) {
		//IJ.log("transform: "+maxN+" "+inverse);
		if (!powerOf2Size())
			throw new  IllegalArgumentException("Image not power of 2 size or not square: "+width+"x"+height);
		maxN = width;
		if (S==null) {
			makeSinCosTables(maxN);
			makeBitReverseTable(maxN);
			tempArr = new float[maxN];
		}
		float[] fht = (float[])getPixels();
	 	rc2DFHT(fht, inverse, maxN);
		isFrequencyDomain = !inverse;
	}

	void makeSinCosTables(int maxN) {
		int n = maxN/4;
		C = new float[n];
		S = new float[n];
		double theta = 0.0;
		double dTheta = 2.0 * Math.PI/maxN;
		for (int i=0; i<n; i++) {
			C[i] = (float)Math.cos(theta);
			S[i] = (float)Math.sin(theta);
			theta += dTheta;
		}
	}
	
	void makeBitReverseTable(int maxN) {
		bitrev = new int[maxN];
		int nLog2 = log2(maxN);
		for (int i=0; i<maxN; i++)
			bitrev[i] = bitRevX(i, nLog2);
	}

	/** Performs a 2D FHT (Fast Hartley Transform). */
	public void rc2DFHT(float[] x, boolean inverse, int maxN) {
		//IJ.write("FFT: rc2DFHT (row-column Fast Hartley Transform)");
		for (int row=0; row<maxN; row++)
			dfht3(x, row*maxN, inverse, maxN);		
		progress(0.4);
		transposeR(x, maxN);
		progress(0.5);
		for (int row=0; row<maxN; row++)		
			dfht3(x, row*maxN, inverse, maxN);
		progress(0.7);
		transposeR(x, maxN);
		progress(0.8);

		int mRow, mCol;
		float A,B,C,D,E;
		for (int row=0; row<=maxN/2; row++) { // Now calculate actual Hartley transform
			for (int col=0; col<=maxN/2; col++) {
				mRow = (maxN - row) % maxN;
				mCol = (maxN - col)  % maxN;
				A = x[row * maxN + col];	//  see Bracewell, 'Fast 2D Hartley Transf.' IEEE Procs. 9/86
				B = x[mRow * maxN + col];
				C = x[row * maxN + mCol];
				D = x[mRow * maxN + mCol];
				E = ((A + D) - (B + C)) / 2;
				x[row * maxN + col] = A - E;
				x[mRow * maxN + col] = B + E;
				x[row * maxN + mCol] = C + E;
				x[mRow * maxN + mCol] = D - E;
			}
		}
		progress(0.95);
	}
	
	void progress(double percent) {
		if (showProgress)
			IJ.showProgress(percent);
	}
	
	/** Performs an optimized 1D FHT. */
	public void dfht3 (float[] x, int base, boolean inverse, int maxN) {
		int i, stage, gpNum, gpIndex, gpSize, numGps, Nlog2;
		int bfNum, numBfs;
		int Ad0, Ad1, Ad2, Ad3, Ad4, CSAd;
		float rt1, rt2, rt3, rt4;

		Nlog2 = log2(maxN);
		BitRevRArr(x, base, Nlog2, maxN);	//bitReverse the input array
		gpSize = 2;     //first & second stages - do radix 4 butterflies once thru
		numGps = maxN / 4;
		for (gpNum=0; gpNum<numGps; gpNum++)  {
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

		if (Nlog2 > 2) {
			 // third + stages computed here
			gpSize = 4;
			numBfs = 2;
			numGps = numGps / 2;
			//IJ.write("FFT: dfht3 "+Nlog2+" "+numGps+" "+numBfs);
			for (stage=2; stage<Nlog2; stage++) {
				for (gpNum=0; gpNum<numGps; gpNum++) {
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
					for (bfNum=1; bfNum<numBfs; bfNum++) {
					// subsequent BF's dealt with together
						Ad1 = bfNum + Ad0;
						Ad2 = Ad1 + gpSize;
						Ad3 = gpSize - bfNum + Ad0;
						Ad4 = Ad3 + gpSize;

						CSAd = bfNum * numGps;
						rt1 = x[base+Ad2] * C[CSAd] + x[base+Ad4] * S[CSAd];
						rt2 = x[base+Ad4] * C[CSAd] - x[base+Ad2] * S[CSAd];

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

		if (inverse)  {
			for (i=0; i<maxN; i++)
			x[base+i] = x[base+i] / maxN;
		}
	}

	void transposeR (float[] x, int maxN) {
		int   r, c;
		float  rTemp;

		for (r=0; r<maxN; r++)  {
			for (c=r; c<maxN; c++) {
				if (r != c)  {
					rTemp = x[r*maxN + c];
					x[r*maxN + c] = x[c*maxN + r];
					x[c*maxN + r] = rTemp;
				}
			}
		}
	}
	
	int log2 (int x) {
		int count = 15;
		while (!btst(x, count))
			count--;
		return count;
	}

	
	private boolean btst (int  x, int bit) {
		//int mask = 1;
		return ((x & (1<<bit)) != 0);
	}

	void BitRevRArr (float[] x, int base, int bitlen, int maxN) {
		for (int i=0; i<maxN; i++)
			tempArr[i] = x[base+bitrev[i]];
		for (int i=0; i<maxN; i++)
			x[base+i] = tempArr[i];
	}

	private int bitRevX (int  x, int bitlen) {
		int  temp = 0;
		for (int i=0; i<=bitlen; i++)
			if ((x & (1<<i)) !=0)
				temp  |= (1<<(bitlen-i-1));
		return temp & 0x0000ffff;
	}

	private int bset (int x, int bit) {
		x |= (1<<bit);
		return x;
	}

	/** Returns an 8-bit power spectrum, log-scaled to 1-254. The image in this
		FHT is assumed to be in the frequency domain. */
	public ImageProcessor getPowerSpectrum () {
		if (!isFrequencyDomain)
			throw new  IllegalArgumentException("Frequency domain image required");
		int base;
		float  r, scale;
		float min = Float.MAX_VALUE;
  		float max = Float.MIN_VALUE;
   		float[] fps = new float[maxN*maxN];
 		byte[] ps = new byte[maxN*maxN];
		float[] fht = (float[])getPixels();

  		for (int row=0; row<maxN; row++) {
			FHTps(row, maxN, fht, fps);
			base = row * maxN;
			for (int col=0; col<maxN; col++) {
				r = fps[base+col];
				if (r<min) min = r;
				if (r>max) max = r;
			}
		}

		if (min<1.0)
			min = 0f;
		else
			min = (float)Math.log(min);
		max = (float)Math.log(max);
		scale = (float)(253.0/(max-min));

		for (int row=0; row<maxN; row++) {
			base = row*maxN;
			for (int col=0; col<maxN; col++) {
				r = fps[base+col];
				if (r<1f)
					r = 0f;
				else
					r = (float)Math.log(r);
				ps[base+col] = (byte)(((r-min)*scale+0.5)+1);
			}
		}
		ImageProcessor ip = new ByteProcessor(maxN, maxN, ps, null);
		swapQuadrants(ip);
		if (FFT.displayRawPS) {
			ImageProcessor ip2 = new FloatProcessor(maxN, maxN, fps, null);
			swapQuadrants(ip2);
			IJ.getFactory().newImagePlus("PS of "+FFT.fileName, ip2).show();
		}
		if (FFT.displayFHT) {
			ImageProcessor ip3 = new FloatProcessor(maxN, maxN, fht, null);
			IjxImagePlus imp2 = IJ.getFactory().newImagePlus("FHT of "+FFT.fileName, ip3.duplicate());
			(new ContrastEnhancer()).stretchHistogram(imp2, 0.1);
			imp2.show();
		}
		if (FFT.displayComplex) {
			IjxImageStack ct = getComplexTransform();
			IjxImagePlus imp2 = IJ.getFactory().newImagePlus("Complex of "+FFT.fileName, ct);
			(new ContrastEnhancer()).stretchHistogram(imp2, 0.1);
			imp2.setProperty("FFT width", ""+originalWidth);
			imp2.setProperty("FFT height", ""+originalHeight);
			imp2.show();
		}
		return ip;
	}

	/** Power Spectrum of one row from 2D Hartley Transform. */
 	void FHTps(int row, int maxN, float[] fht, float[] ps) {
 		int base = row*maxN;
		int l;
		for (int c=0; c<maxN; c++) {
			l = ((maxN-row)%maxN) * maxN + (maxN-c)%maxN;
			ps[base+c] = (sqr(fht[base+c]) + sqr(fht[l]))/2f;
 		}
	}

	/** Converts this FHT to a complex Fourier transform and returns it as a two slice stack.
	*	@author Joachim Wesner
	*/
	public IjxImageStack getComplexTransform() {
		if (!isFrequencyDomain)
			throw new  IllegalArgumentException("Frequency domain image required");
		float[] fht = (float[])getPixels();
		float[] re = new float[maxN*maxN];
		float[] im = new float[maxN*maxN];
		for (int i=0; i<maxN; i++) {
			FHTreal(i, maxN, fht, re);
			FHTimag(i, maxN, fht, im);
		}
		swapQuadrants(new FloatProcessor(maxN, maxN, re, null));
		swapQuadrants(new FloatProcessor(maxN, maxN, im, null));
		IjxImageStack stack = IJ.getFactory().newImageStack(maxN, maxN);
		stack.addSlice("Real", re);
		stack.addSlice("Imaginary", im);
		return stack;
	}

	/**	 FFT real value of one row from 2D Hartley Transform.
	*	@author Joachim Wesner
	*/
      void FHTreal(int row, int maxN, float[] fht, float[] real) {
            int base = row*maxN;
            int offs = ((maxN-row)%maxN) * maxN;
            for (int c=0; c<maxN; c++) {
                  real[base+c] = (fht[base+c] + fht[offs+((maxN-c)%maxN)])*0.5f;
            }
      }


	/** FFT imag value of one row from 2D Hartley Transform.
	*	@author Joachim Wesner
	*/
      void FHTimag(int row, int maxN, float[] fht, float[] imag) {
            int base = row*maxN;
            int offs = ((maxN-row)%maxN) * maxN;
            for (int c=0; c<maxN; c++) {
                  imag[base+c] = (-fht[base+c] + fht[offs+((maxN-c)%maxN)])*0.5f;
            }
      }

	ImageProcessor calculateAmplitude(float[] fht, int maxN) {
   		float[] amp = new float[maxN*maxN];
   		for (int row=0; row<maxN; row++) {
			amplitude(row, maxN, fht, amp);
		}
		ImageProcessor ip = new FloatProcessor(maxN, maxN, amp, null);
		swapQuadrants(ip);
		return ip;
	}

	/** Amplitude of one row from 2D Hartley Transform. */
 	void amplitude(int row, int maxN, float[] fht, float[] amplitude) {
 		int base = row*maxN;
		int l;
		for (int c=0; c<maxN; c++) {
			l = ((maxN-row)%maxN) * maxN + (maxN-c)%maxN;
			amplitude[base+c] = (float)Math.sqrt(sqr(fht[base+c]) + sqr(fht[l]));
 		}
	}

	float sqr(float x) {
		return x*x;
	}

	/**	Swap quadrants 1 and 3 and 2 and 4 of the specified ImageProcessor 
		so the power spectrum origin is at the center of the image.
		<pre>
		    2 1
		    3 4
		</pre>
	*/
 	public void swapQuadrants(ImageProcessor ip) {
		//IJ.log("swap");
 		ImageProcessor t1, t2;
		int size = ip.getWidth()/2;
		ip.setRoi(size,0,size,size);
		t1 = ip.crop();
  		ip.setRoi(0,size,size,size);
		t2 = ip.crop();
		ip.insert(t1,0,size);
		ip.insert(t2,size,0);
		ip.setRoi(0,0,size,size);
		t1 = ip.crop();
  		ip.setRoi(size,size,size,size);
		t2 = ip.crop();
		ip.insert(t1,size,size);
		ip.insert(t2,0,0);
		ip.resetRoi();
	}

	/**	Swap quadrants 1 and 3 and 2 and 4 of the image
		contained in this FHT. */
 	public void swapQuadrants () {
 		swapQuadrants(this);
 	}
 	
	void changeValues(ImageProcessor ip, int v1, int v2, int v3) {
		byte[] pixels = (byte[])ip.getPixels();
		int v;
		//IJ.log(v1+" "+v2+" "+v3+" "+pixels.length);
		for (int i=0; i<pixels.length; i++) {
			v = pixels[i]&255;
			if (v>=v1 && v<=v2)
				pixels[i] = (byte)v3;
		}
	}

	/** Returns the image resulting from the point by point Hartley multiplication
		of this image and the specified image. Both images are assumed to be in
		the frequency domain. Multiplication in the frequency domain is equivalent 
		to convolution in the space domain. */
	public FHT multiply(FHT fht) {
		return multiply(fht, false);
	}

	/** Returns the image resulting from the point by point Hartley conjugate 
		multiplication of this image and the specified image. Both images are 
		assumed to be in the frequency domain. Conjugate multiplication in
		the frequency domain is equivalent to correlation in the space domain. */
	public FHT conjugateMultiply(FHT fht) {
		return multiply(fht, true);
	}

	FHT multiply(FHT fht, boolean  conjugate) {
		int rowMod, cMod, colMod;
		double h2e, h2o;
		float[] h1 = (float[])getPixels();
		float[] h2 = (float[])fht.getPixels();
		float[] tmp = new float[maxN*maxN];
		for (int r =0; r<maxN; r++) {
			rowMod = (maxN - r) % maxN;
			for (int c=0; c<maxN; c++) {
				colMod = (maxN - c) % maxN;
				h2e = (h2[r * maxN + c] + h2[rowMod * maxN + colMod]) / 2;
				h2o = (h2[r * maxN + c] - h2[rowMod * maxN + colMod]) / 2;
				if (conjugate) 
					tmp[r * maxN + c] = (float)(h1[r * maxN + c] * h2e - h1[rowMod * maxN + colMod] * h2o);
				else
					tmp[r * maxN + c] = (float)(h1[r * maxN + c] * h2e + h1[rowMod * maxN + colMod] * h2o);
			}
		}
		FHT fht2 =  new FHT(new FloatProcessor(maxN, maxN, tmp, null));
		fht2.isFrequencyDomain = true;
		return fht2;
	}
		
	/** Returns the image resulting from the point by point Hartley division
		of this image by the specified image. Both images are assumed to be in
		the frequency domain. Division in the frequency domain is equivalent 
		to deconvolution in the space domain. */
	public FHT divide(FHT fht) {
		int rowMod, cMod, colMod;
		double mag, h2e, h2o;
		float[] h1 = (float[])getPixels();
		float[] h2 = (float[])fht.getPixels();
		float[] out = new float[maxN*maxN];
		for (int r=0; r<maxN; r++) {
			rowMod = (maxN - r) % maxN;
			for (int c=0; c<maxN; c++) {
				colMod = (maxN - c) % maxN;
				mag =h2[r*maxN+c] * h2[r*maxN+c] + h2[rowMod*maxN+colMod] * h2[rowMod*maxN+colMod];
				if (mag<1e-20)
					mag = 1e-20;
				h2e = (h2[r*maxN+c] + h2[rowMod*maxN+colMod]);
				h2o = (h2[r*maxN+c] - h2[rowMod*maxN+colMod]);
				double tmp = (h1[r*maxN+c] * h2e - h1[rowMod*maxN+colMod] * h2o);
				out[r*maxN+c] = (float)(tmp/mag);
			}
		}
		FHT fht2 = new FHT(new FloatProcessor(maxN, maxN, out, null));
		fht2.isFrequencyDomain = true;
		return fht2;
	}
			
	/** Enables/disables display of the progress bar during transforms. */
	public void setShowProgress(boolean showProgress) {
		this.showProgress = showProgress;
	}
	
	/** Returns a clone of this FHT. */
	public FHT getCopy() {
		ImageProcessor ip = super.duplicate();
		FHT fht = new FHT(ip);
		fht.isFrequencyDomain = isFrequencyDomain;
		fht.quadrantSwapNeeded = quadrantSwapNeeded;
		fht.rgb = rgb;
		fht.originalWidth = originalWidth;
		fht.originalHeight = originalHeight;
		fht.originalBitDepth = originalBitDepth;		
		fht.originalColorModel = originalColorModel;		
		return fht;
	}
		
	/** Returns a string containing information about this FHT. */
	public String toString() {
		return "FHT, " + getWidth() + "x"+getHeight() + ", fd=" + isFrequencyDomain;
	}
	
}
