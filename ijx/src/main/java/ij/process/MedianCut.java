package ij.process;

import java.awt.*;
import java.awt.image.*;
import ij.*; //??

/** Converts an RGB image to 8-bit index color using Heckbert's median-cut
    color quantization algorithm. Based on median.c by Anton Kruger from the
    September, 1994 issue of Dr. Dobbs Journal.
*/
public class MedianCut {
	
	static final int MAXCOLORS = 256;	// maximum # of output colors
	static final int HSIZE = 32768;		// size of image histogram
	private int[] hist;					// RGB histogram and reverse color lookup table
	private int[] histPtr;				// points to colors in "hist"
	private Cube[] list;				// list of cubes
	private int[] pixels32;
	private int width, height;
	private IndexColorModel cm; 

	public MedianCut(int[] pixels, int width, int height) {
		int color16;

		pixels32 = pixels;
		this.width = width;
		this.height = height;
		
		//build 32x32x32 RGB histogram
		IJ.showProgress(0.3);
		IJ.showStatus("Building 32x32x32 RGB histogram");
		hist = new int[HSIZE];
		for (int i=0; i<width*height; i++) {
			color16 = rgb(pixels32[i]);
			hist[color16]++;
		}
	}
	
	public MedianCut(ColorProcessor ip) {
		this((int[])ip.getPixels(), ip.getWidth(), ip.getHeight());
	}
	
	int getColorCount() {
		int count = 0;
		for (int i=0; i<HSIZE; i++)
			if (hist[i]>0) count++;
		return count;
	}
	

	Color getModalColor() {
		int max=0;
		int c = 0;
		for (int i=0; i<HSIZE; i++)
			if (hist[i]>max) {
				max = hist[i];
				c = i;
			}
		return new Color(red(c), green(c), blue(c));
	}
	

	// Convert from 24-bit to 15-bit color
	private final int rgb(int c) {
		int r = (c&0xf80000)>>19;
		int g = (c&0xf800)>>6;
		int b = (c&0xf8)<<7;
		return b | g | r;
	}
	
	// Get red component of a 15-bit color
	private final int red(int x) {
		return (x&31)<<3;
	}
	
	// Get green component of a 15-bit color
	private final int green(int x) {
		return (x>>2)&0xf8;
	}
	
	// Get blue component of a 15-bit color
	private final int blue(int x) {
		return (x>>7)&0xf8;
	}


	/** Uses Heckbert's median-cut algorithm to divide the color space defined by
	"hist" into "maxcubes" cubes. The centroids (average value) of each cube
	are are used to create a color table. "hist" is then updated to function
	as an inverse color map that is used to generate an 8-bit image. */
	public Image convert(int maxcubes) {
		ImageProcessor ip = convertToByte(maxcubes);
		return ip.createImage();
	}

	/** This is a version of convert that returns a ByteProcessor. */
	public ImageProcessor convertToByte(int maxcubes) {
		int lr, lg, lb;
		int i, median, color;
		int count;
		int k, level, ncubes, splitpos;
		int num, width;
		int longdim=0;	//longest dimension of cube
		Cube cube, cubeA, cubeB;
		
		// Create initial cube
		IJ.showStatus("Median cut");
		list = new Cube[MAXCOLORS];
		histPtr = new int[HSIZE];
		ncubes = 0;
		cube = new Cube();
		for (i=0,color=0; i<=HSIZE-1; i++) {
			if (hist[i] != 0) {
				histPtr[color++] = i;
				cube.count = cube.count + hist[i];
			}
		}
		cube.lower = 0; cube.upper = color-1;
		cube.level = 0;
		Shrink(cube);
		list[ncubes++] = cube;

		//Main loop
		while (ncubes < maxcubes) { 

			// Search the list of cubes for next cube to split, the lowest level cube
			level = 255; splitpos = -1; 
			for (k=0; k<=ncubes-1; k++) {
				if (list[k].lower == list[k].upper)  
					;	// single color; cannot be split
				else if (list[k].level < level) {
					level = list[k].level;
					splitpos = k;
				}
			}
			if (splitpos == -1)	// no more cubes to split
				break;

			// Find longest dimension of this cube
			cube = list[splitpos];
			lr = cube.rmax - cube.rmin;
			lg = cube.gmax - cube.gmin;
			lb = cube.bmax - cube.bmin;
			if (lr >= lg && lr >= lb) longdim = 0;
			if (lg >= lr && lg >= lb) longdim = 1;
			if (lb >= lr && lb >= lg) longdim = 2;
			
			// Sort along "longdim"
			reorderColors(histPtr, cube.lower, cube.upper, longdim);
			quickSort(histPtr, cube.lower, cube.upper);
			restoreColorOrder(histPtr, cube.lower, cube.upper, longdim);

			// Find median
			count = 0;
			for (i=cube.lower;i<=cube.upper-1;i++) {
				if (count >= cube.count/2) break;
				color = histPtr[i];
				count = count + hist[color];
			}
			median = i;

			// Now split "cube" at the median and add the two new
			// cubes to the list of cubes.
			cubeA = new Cube();
			cubeA.lower = cube.lower; 
			cubeA.upper = median-1;
			cubeA.count = count;
			cubeA.level = cube.level + 1;
			Shrink(cubeA);
			list[splitpos] = cubeA;				// add in old slot

			cubeB = new Cube();
			cubeB.lower = median; 
			cubeB.upper = cube.upper; 
			cubeB.count = cube.count - count;
			cubeB.level = cube.level + 1;
			Shrink(cubeB);
			list[ncubes++] = cubeB;				// add in new slot */
			if (ncubes%15==0)
				IJ.showProgress(0.3 + (0.6*ncubes)/maxcubes);
		}

		// We have enough cubes, or we have split all we can. Now
		// compute the color map, the inverse color map, and return
		// an 8-bit image.
		IJ.showProgress(0.9);
		makeInverseMap(hist, ncubes);
		IJ.showProgress(0.95);
		return makeImage();
	}
	
	void Shrink(Cube cube) {
	// Encloses "cube" with a tight-fitting cube by updating the
	// (rmin,gmin,bmin) and (rmax,gmax,bmax) members of "cube".

		int r, g, b;
		int color;
		int rmin, rmax, gmin, gmax, bmin, bmax;

		rmin = 255; rmax = 0;
		gmin = 255; gmax = 0;
		bmin = 255; bmax = 0;
		for (int i=cube.lower; i<=cube.upper; i++) {
			color = histPtr[i];
			r = red(color);
			g = green(color);
			b = blue(color);
			if (r > rmax) rmax = r;
			if (r < rmin) rmin = r;
			if (g > gmax) gmax = g;
			if (g < gmin) gmin = g;
			if (b > bmax) bmax = b;
			if (b < bmin) bmin = b;
		}
		cube.rmin = rmin; cube.rmax = rmax;
		cube.gmin = gmin; cube.gmax = gmax;
		cube.bmin = bmin; cube.bmax = bmax;
	}


	void makeInverseMap(int[] hist, int ncubes) {
	// For each cube in the list of cubes, computes the centroid
	// (average value) of the colors enclosed by that cube, and
	// then loads the centroids in the color map. Next loads
	// "hist" with indices into the color map

		int r, g, b;
		int color;
		float rsum, gsum, bsum;
		Cube cube;
		byte[] rLUT = new byte[256];
		byte[] gLUT = new byte[256];
		byte[] bLUT = new byte[256];

		IJ.showStatus("Making inverse map");
		for (int k=0; k<=ncubes-1; k++) {
			cube = list[k];
			rsum = gsum = bsum = (float)0.0;
			for (int i=cube.lower; i<=cube.upper; i++) {
				color = histPtr[i];
				r = red(color);
				rsum += (float)r*(float)hist[color];
				g = green(color);
				gsum += (float)g*(float)hist[color];
				b = blue(color);
				bsum += (float)b*(float)hist[color];
			}

			// Update the color map
			r = (int)(rsum/(float)cube.count);
			g = (int)(gsum/(float)cube.count);
			b = (int)(bsum/(float)cube.count);
			if (r==248 && g==248 && b==248)
				r=g=b=255;  // Restore white (255,255,255)
			rLUT[k] = (byte)r;
			gLUT[k] = (byte)g;
			bLUT[k] = (byte)b;
		}
		cm = new IndexColorModel(8, ncubes, rLUT, gLUT, bLUT);
		
		// For each color in each cube, load the corre- 
		// sponding slot in "hist" with the centroid of the cube.
		for (int k=0; k<=ncubes-1; k++) {
			cube = list[k];
			for (int i=cube.lower; i<=cube.upper; i++) {
				color = histPtr[i];
				hist[color] = k;
			}
		}
	}
	

	void reorderColors(int[] a, int lo, int hi, int longDim) {
	// Change the ordering of the 5-bit colors in each word of int[]
	// so we can sort on the 'longDim' color
	
		int c, r, g, b;
		switch (longDim) {
			case 0: //red
				for (int i=lo; i<=hi; i++) {
					c = a[i];
					r = c & 31;
					a[i] = (r<<10) | (c>>5);
					}
				break;
			case 1: //green
				for (int i=lo; i<=hi; i++) {
					c = a[i];
					r = c & 31;
					g = (c>>5) & 31;
					b = c>>10;
					a[i] = (g<<10) | (b<<5) | r;
					}
				break;
			case 2: //blue; already in the needed order
				break;
		}
	}
	

	void restoreColorOrder(int[] a, int lo, int hi, int longDim) {
	// Restore the 5-bit colors to the original order
	
		int c, r, g, b;
		switch (longDim){
			case 0: //red
				for (int i=lo; i<=hi; i++) {
					c = a[i];
					r = c >> 10;
					a[i] = ((c&1023)<<5) | r;
				}
				break;
			case 1: //green
				for (int i=lo; i<=hi; i++) {
					c = a[i];
					r = c & 31;
					g = c>>10;
					b = (c>>5) & 31;
					a[i] = (b<<10) | (g<<5) | r;
				}
				break;
			case 2: //blue
				break;
		}
	}
	
	
	void quickSort(int a[], int lo0, int hi0) {
   // Based on the QuickSort method by James Gosling from Sun's SortDemo applet
   
      int lo = lo0;
      int hi = hi0;
      int mid, t;

      if ( hi0 > lo0) {
         mid = a[ ( lo0 + hi0 ) / 2 ];
         while( lo <= hi ) {
            while( ( lo < hi0 ) && ( a[lo] < mid ) )
               ++lo;
            while( ( hi > lo0 ) && ( a[hi] > mid ) )
               --hi;
            if( lo <= hi ) {
		      t = a[lo]; 
		      a[lo] = a[hi];
		      a[hi] = t;
               ++lo;
               --hi;
            }
         }
         if( lo0 < hi )
            quickSort( a, lo0, hi );
         if( lo < hi0 )
            quickSort( a, lo, hi0 );

      }
   }


	ImageProcessor makeImage() {
	// Generate 8-bit image
	
		Image img8;
		byte[] pixels8;
		int color16;
		
		IJ.showStatus("Creating 8-bit image");
	    pixels8 = new byte[width*height];
	    for (int i=0; i<width*height; i++) {
	    	color16 = rgb(pixels32[i]);
	    	pixels8[i] = (byte)hist[color16];
	    }
	    ImageProcessor ip = new ByteProcessor(width, height, pixels8, cm);
        IJ.showProgress(1.0);
		return ip;
	}
	
	
} //class MedianCut


class Cube {			// structure for a cube in color space
	int  lower;			// one corner's index in histogram
	int  upper;			// another corner's index in histogram
	int  count;			// cube's histogram count
	int  level;			// cube's level
	int  rmin, rmax;
	int  gmin, gmax;
	int  bmin, bmax;
	
	Cube() {
		count = 0;
	}   

	public String toString() {
		String s = "lower=" + lower + " upper=" + upper;
		s = s + " count=" + count + " level=" + level;
		s = s + " rmin=" + rmin + " rmax=" + rmax;
		s = s + " gmin=" + gmin + " gmax=" + gmax;
		s = s + " bmin=" + bmin + " bmax=" + bmax;
		return s;
	}
	
}

