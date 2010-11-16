package ij.process;
import java.util.Arrays;

/** A table for easier downsizing by convolution with a kernel.
 *	Supports the interpolation methods of ImageProcessor: none, bilinear, bicubic
 *	Convention used: The left edges of the first pixel are the same for source and destination.
 *	E.g. when downsizing by a factor of 2, pixel 0 of the destination
 *	takes the space of pixels 0 and 1 of the source.
 *
 *	Example for use: Downsizing row 0 of 'pixels' from 'roi.width' to 'destinationWidth'.
 *	The input range is given by the roi rectangle.
 *	Output is written to row 0 of 'pixels2' (width: 'destinationWidth')
 <code>
	DownSizeTable dt = new DownSizeTable(width, roi.x, roi.width, destinationWidth, ImageProcessor.BICUBIC);
	int tablePointer = 0;
	for (int srcPoint=dt.srcStart, srcPoint<=dt.srcEnd; srcPoint++) {
		float v = pixels[srcPoint];
		for (int i=0; i<dt.kernelSize; i++, tablePointer++)
			pixels2[dt.indices[tablePointer]] += v * dt.weights[tablePointer];
 </code>
 */
public class DownsizeTable {
	/** Number of kernel points per source data point */
	public final int kernelSize;
	/** index of the first point of the source data that should be accessed */
	public final int srcStart;
	/** index of the last point of the source data that should be accessed */
	public final int srcEnd;
	/** For each source point between srcStart and srcEnd, indices of destination
	 *	points where the data should be added.
	 *	Arranged in blocks of 'kernelSize' points. E.g. for kernelSize=2, array
	 *	elements 0,1 are for point srcStart, 2,3 for point srcStart+1, etc. */
	public final int[] indices;
	/** For each source point, weights for adding it to the destination point
	 *	given in the corresponding element of 'indices' */
	public final float[] weights;
	/** Kernel sizes corresponding to the interpolation methods NONE, BILINEAR, BICUBIC */
	private final static int[] kernelSizes = new int[] {1, 2, 4};
	private final int srcOrigin, srcLength;
	private final double scale;			//source/destination pixel numbers
	private final int interpolationMethod;
	private final static int UNUSED=-1; //marks unused entries in 'indices' array


	/** Create a table for 1-dimensional downscaling interpolation.
	 *	Interpolation is done by 
	 * @param srcSize	 Size of source data, i.e., width or height of input image
	 * @param srcOrigin	 Index of first pixel of source data that corresponds to an ouput pixel,
	 *					 0 or origin of source rectangle if only a roi is scaled
	 * @param srcLength	 Number of pixels of source data that should correspond to output, i.e.,
	 *					 width or height of source roi
	 * @param dstSize	 Number of destination pixels.
	 * @param interpolationMethod One of the methods defined in ImageProcessor: NONE, BILINEAR, BICUBIC
	 */
	DownsizeTable(int srcSize, int srcOrigin, int srcLength, int dstSize, int interpolationMethod) {
		this.srcOrigin = srcOrigin;
		this.srcLength = srcLength;
		this.interpolationMethod = interpolationMethod;
		this.scale = srcLength / (double)dstSize;
		this.kernelSize = kernelSizes[interpolationMethod];
		int srcStartUncorr = (int)(Math.ceil(1e-8+srcIndex(-0.5*kernelSize))); //may be <0
		srcStart = srcStartUncorr < 0 ? 0 : srcStartUncorr;	   //corrected value, avoids pointing out of array
		int srcEndUncorr = (int)(Math.floor(1e-8+srcIndex(dstSize-1 + 0.5*kernelSize)));
		srcEnd = srcEndUncorr >=srcSize ? srcSize-1 : srcEndUncorr;
		int arraySize = (srcEnd - srcStart + 1) * kernelSize;
		indices = new int[arraySize];
		weights = new float[arraySize];
		Arrays.fill(indices, UNUSED);
		//IJ.log("src size="+srcSize+" range="+srcStart+"-"+srcEnd+" array:"+arraySize+" scale="+(float)scale);

		for (int dst=0; dst<dstSize; dst++) {
			double sum = 0;
			int lowestS = (int)(Math.ceil(1e-8+srcIndex(dst-0.5*kernelSize)));
			int highestS = (int)(Math.floor(-1e-8+srcIndex(dst+0.5*kernelSize)));
			for (int src=lowestS; src<=highestS; src++) {
				//out of bounds policy is 'use value of edge pixel'.
				//We therfore replace an out-of-bounds pixel by the edge pixel:
				int s = src < 0 ? 0 : (src >= srcSize ? srcSize-1 : src);
				int p = (s-srcStart)*kernelSize;// points to first value in 'indices' and 'weights'
												// arrays reserved for this source pixel
				while(indices[p]!=UNUSED && indices[p]!=dst)
					p++;					//position used for other destination pixel, try the next one
				//if(p-(s-srcStart)*kernelSize>=kernelSize)IJ.log(srcSize+">"+dstSize+": too long: src="+src+" dst="+dst);
				indices[p] = dst;
				float weight = kernel(dst - dstIndex(src));
				sum += weight;
				weights[p] += weight;
				//IJ.log("src="+src+"("+s+") to "+dst+" w="+weight+" p="+p);
			}
			//normalize: sum of weights contributing to this destination pixel should be 1
			int iStart = (lowestS-srcStart)*kernelSize;
			if (iStart < 0) iStart = 0;
			int iStop = (highestS-srcStart)*kernelSize+(kernelSize-1);
			if (iStop>=indices.length) iStop = indices.length-1;
			//IJ.log("normalize "+iStart+"-"+iStop+" sum="+sum);
			for (int i=iStart; i<=iStop; i++)
				if (indices[i] == dst)
					weights[i] = (float)(weights[i]/sum);
		}
		for (int i=0; i<indices.length; i++)
			if (indices[i]==UNUSED)
				indices[i] = 0;		//set unused entries to pixel 0 (weight is 0 anyhow), then they do no harm
	}
	// Converts a destination pixel coordinate (index) to the corresponding
	// source coordinate
	// All coordinates refer to the centers of the pixel
	// Also for fractional indices, e.g. dstIndex = i-0.5 for left edge of pixel.
	// The output is also fractional (not converted to int)
	// No check for source array bounds, may result in a value <0 or >= array size.
	private double srcIndex(double dstIndex) {
		return srcOrigin-0.5 + (dstIndex+0.5)*scale;
	}
	// Converts the coordinate (index) of a source pixel to the destination pixel
	private double dstIndex(int srcIndex) {
		return (srcIndex-srcOrigin+0.5)/scale - 0.5;
	}
	// Calculates the kernel value. Only valid within +/- 0.5*kernelSize
	protected float kernel(double x) {
		switch (interpolationMethod) {
			case ImageProcessor.NONE:
				return 1f;
			case ImageProcessor.BILINEAR:
				return 1f - (float)Math.abs(x);
			case ImageProcessor.BICUBIC:
				return (float)ImageProcessor.cubic(x);
		}
		return Float.NaN;
	}

}
