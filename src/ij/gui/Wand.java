package ij.gui;

import java.awt.*;
import ij.*;
import ij.process.*;

/** This class implements ImageJ's wand (tracing) tool. */
public class Wand {
	static final int UP=0, DOWN=1, UP_OR_DOWN=2, LEFT=3, RIGHT=4, LEFT_OR_RIGHT=5, NA=6;
	
	/** The number of points in the generated outline. */
	public int npoints;
	private int maxPoints = 1000; // will be increased if necessary
	/** The x-coordinates of the points in the outline. */
	public int[] xpoints = new int[maxPoints];
	/** The y-coordinates of the points in the outline. */
	public int[] ypoints = new int[maxPoints];

	private ImageProcessor ip;
	private byte[] bpixels;
	private int[] cpixels;
	private short[] spixels;
	private float[] fpixels;
	private int width, height;
	private float lowerThreshold, upperThreshold;

	/** Constructs a Wand object from an ImageProcessor. */
	public Wand(ImageProcessor ip) {
		if (ip instanceof ByteProcessor)
			bpixels = (byte[])ip.getPixels();
		else if (ip instanceof ColorProcessor)
			cpixels = (int[])ip.getPixels();
		else if (ip instanceof ShortProcessor)
			spixels = (short[])ip.getPixels();
		else if (ip instanceof FloatProcessor)
			fpixels = (float[])ip.getPixels();
		width = ip.getWidth();
		height = ip.getHeight();
	}
	
	private float getColorPixel(int x, int y) {
		if (x>=0 && x<width && y>=0 && y<height)
			return cpixels[y*width + x];
		else
			return Float.MAX_VALUE;
	}

	private float getBytePixel(int x, int y) {
		if (x>=0 && x<width && y>=0 && y<height)
			return bpixels[y*width + x] & 0xff;
		else
			return Float.MAX_VALUE;
	}

	private float getShortPixel(int x, int y) {
		if (x>=0 && x<width && y>=0 && y<height)
			return spixels[y*width + x] & 0xffff;
		else
			return Float.MAX_VALUE;
	}

	private float getFloatPixel(int x, int y) {
		if (x>=0 && x<width && y>=0 && y<height)
			return fpixels[y*width + x];
		else
			return Float.MAX_VALUE;
	}

	private float getPixel(int x, int y) {
		if (bpixels!=null)
			return getBytePixel(x,y);
		else if (spixels!=null)
			return getShortPixel(x,y);
		else if (fpixels!=null)
			return getFloatPixel(x,y);
		else
			return getColorPixel(x,y);
	}

	private boolean inside(int x, int y) {
		float value;
		if (bpixels!=null)
			value = getBytePixel(x,y);
		else if (spixels!=null)
			value = getShortPixel(x,y);
		else if (fpixels!=null)
			value = getFloatPixel(x,y);
		else
			value = getColorPixel(x,y);
		return value>=lowerThreshold && value<=upperThreshold;
	}

	/* Are we tracing a one pixel wide line? */
	boolean isLine(int xs, int ys) {
		int r = 5;
		int xmin=xs;
		int xmax=xs+2*r;
		if (xmax>=width) xmax=width-1;
		int ymin=ys-r;
		if (ymin<0) ymin=0;
		int ymax=ys+r;
		if (ymax>=height) ymax=height-1;
		int area = 0;
		int insideCount = 0;
		for (int x=xmin; (x<=xmax); x++)
			for (int y=ymin; y<=ymax; y++) {
				area++;
				if (inside(x,y))
					insideCount++;
			}
		if (IJ.debugMode)
			IJ.log((((double)insideCount)/area>=0.75?"line ":"blob ")+insideCount+" "+area+" "+IJ.d2s(((double)insideCount)/area));
		return ((double)insideCount)/area>=0.75;
	}
	
	/** Traces the boundary of an area of uniform color, where
		'startX' and 'startY' are somewhere inside the area. The
		boundary points are stored in the public xpoints and ypoints
		fields. A 16 entry lookup table is used to determine the
		direction at each step of the tracing process. Note that
		the four argument version [autoOutline(x, y, lower, upper)]
		works more reliably. */
	public void autoOutline(int startX, int startY) {
		int x = startX;
		int y = startY;
		int direction;
		lowerThreshold = upperThreshold = getPixel(startX, startY);
		do {x++;} while (inside(x,y));
		if (isLine(x, y)) {
			lowerThreshold = upperThreshold = getPixel(x, y);
			direction = UP;
		} else {
			if (!inside(x-1,y-1))
				direction = RIGHT;
			else if (inside(x,y-1))
				direction = LEFT;
			else
				direction = DOWN;
		}
		traceEdge(x, y, direction);
	}
		
	/** Traces an object defined by lower and upper threshold values. The
		boundary points are stored in the public xpoints and ypoints fields.
		This verson works more reliably than autoOutline(x, y). */
	public void autoOutline(int startX, int startY, double lower, double upper) {
		//IJ.log(startX+"  "+startY+"  "+lower+"  "+upper);
		npoints = 0;
		int x = startX;
		int y = startY;
		int direction;
		lowerThreshold = (float)lower;
		upperThreshold = (float)upper;
		if (inside(x,y)) {
			do {x++;} while (inside(x,y));
			if (!inside(x-1,y-1))
				direction = RIGHT;
			else if (inside(x,y-1))
				direction = LEFT;
			else
				direction = DOWN;
		} else {
			do {x++;} while (!inside(x,y) && x<width);
				direction = UP;
			if (x>=width) return;
		}
		traceEdge(x, y, direction);
	}

	/** This is a variation of autoOutline that uses int threshold arguments. */
	public void autoOutline(int startX, int startY, int lower, int upper) {
		autoOutline(startX, startY, (double)lower, (double)upper);
	}

	void traceEdge(int xstart, int ystart, int startingDirection) {
		int[] table = {
						// 1234, 1=upper left pixel,  2=upper right, 3=lower left, 4=lower right
			NA,			// 0000, should never happen
			RIGHT,		// 000X,
			DOWN,		// 00X0
			RIGHT,		// 00XX
			UP,			// 0X00
			UP,			// 0X0X
			UP_OR_DOWN, // 0XX0 Go up or down depending on current direction
			UP,			// 0XXX
			LEFT,		// X000
			LEFT_OR_RIGHT, // X00X  Go left or right depending on current direction
			DOWN,		// X0X0
			RIGHT,		// X0XX
			LEFT,		// XX00
			LEFT,		// XX0X
			DOWN,		// XXX0
			NA,			// XXXX Should never happen
			};
		int index;
		int newDirection;
		int x = xstart;
		int y = ystart;
		int direction = startingDirection;

		boolean UL = inside(x-1, y-1);	// upper left
		boolean UR = inside(x, y-1);	// upper right
		boolean LL = inside(x-1, y);	// lower left
		boolean LR = inside(x, y);		// lower right
		//xpoints[0] = x;
		//ypoints[0] = y;
		int count = 0;
	//IJ.write("");
	//IJ.write(count + " " + x + " " + y + " " + direction + " " + insideValue);
		do {
			index = 0;
			if (LR) index |= 1;
			if (LL) index |= 2;
			if (UR) index |= 4;
			if (UL) index |= 8;
			newDirection = table[index];
			if (newDirection==UP_OR_DOWN) {
				if (direction==RIGHT)
					newDirection = UP;
				else
					newDirection = DOWN;
			}
			if (newDirection==LEFT_OR_RIGHT) {
				if (direction==UP)
					newDirection = LEFT;
				else
					newDirection = RIGHT;
			}
			if (newDirection!=direction) {
				xpoints[count] = x;
				ypoints[count] = y;
				count++;
			if (count==xpoints.length) {
				int[] xtemp = new int[maxPoints*2];
				int[] ytemp = new int[maxPoints*2];
				System.arraycopy(xpoints, 0, xtemp, 0, maxPoints);
				System.arraycopy(ypoints, 0, ytemp, 0, maxPoints);
				xpoints = xtemp;
				ypoints = ytemp;
				maxPoints *= 2;
			}
		//if (count<10) IJ.write(count + " " + x + " " + y + " " + newDirection + " " + index);
			}
			switch (newDirection) {
				case UP:
					y = y-1;
					LL = UL;
					LR = UR;
					UL = inside(x-1, y-1);
					UR = inside(x, y-1);
					break;
				case DOWN:
					y = y + 1;
					UL = LL;
					UR = LR;
					LL = inside(x-1, y);
					LR = inside(x, y);
					break;
				case LEFT:
					x = x-1;
					UR = UL;
					LR = LL;
					UL = inside(x-1, y-1);
					LL = inside(x-1, y);
					break;
				case RIGHT:
					x = x + 1;
					UL = UR;
					LL = LR;
					UR = inside(x, y-1);
					LR = inside(x, y);
					break;
			}
			direction = newDirection;
		} while ((x!=xstart || y!=ystart || direction!=startingDirection));
		npoints = count;
	}

}