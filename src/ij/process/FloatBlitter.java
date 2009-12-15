package ij.process;
import ij.Prefs;
import java.awt.*;

/** This class does bit blitting of 32-bit floating-point images. */
public class FloatBlitter implements Blitter {

	public static float divideByZeroValue;
	
	private FloatProcessor ip;
	private int width, height;
	private float[] pixels;
	
	static {
		divideByZeroValue = (float)Prefs.getDouble(Prefs.DIV_BY_ZERO_VALUE, Float.POSITIVE_INFINITY);
		if (divideByZeroValue==Float.MAX_VALUE)
			divideByZeroValue = Float.POSITIVE_INFINITY;
	}
	
	/** Constructs a FloatBlitter from a FloatProcessor. */
	public FloatBlitter(FloatProcessor ip) {
		this.ip = ip;
		width = ip.getWidth();
		height = ip.getHeight();
		pixels = (float[])ip.getPixels();
	}

	public void setTransparentColor(Color c) {
	}

	/** Copies the float image in 'ip' to (x,y) using the specified mode. */
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
		Rectangle r1, r2;
		int srcIndex, dstIndex;
		int xSrcBase, ySrcBase;
		float[] srcPixels;
		
		if (!(ip instanceof FloatProcessor))
			ip = ip.convertToFloat();
		int srcWidth = ip.getWidth();
		int srcHeight = ip.getHeight();
		r1 = new Rectangle(srcWidth, srcHeight);
		r1.setLocation(xloc, yloc);
		r2 = new Rectangle(width, height);
		if (!r1.intersects(r2))
			return;
		srcPixels = (float [])ip.getPixels();
		r1 = r1.intersection(r2);
		xSrcBase = (xloc<0)?-xloc:0;
		ySrcBase = (yloc<0)?-yloc:0;
		boolean useDBZValue = !Float.isInfinite(divideByZeroValue);
		float src, dst;
		for (int y=r1.y; y<(r1.y+r1.height); y++) {
			srcIndex = (y-yloc)*srcWidth + (r1.x-xloc);
			dstIndex = y * width + r1.x;
			switch (mode) {
				case COPY: case COPY_INVERTED:
					for (int i=r1.width; --i>=0;)
						pixels[dstIndex++] = srcPixels[srcIndex++];
					break;
				case ADD:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
						pixels[dstIndex] = srcPixels[srcIndex]+pixels[dstIndex];
					break;
				case AVERAGE:
					for (int i=r1.width; --i>=0;) {
						dst =(srcPixels[srcIndex++]+pixels[dstIndex])/2;
						pixels[dstIndex++] = dst;
					}
					break;
				case DIFFERENCE:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++) {
						dst = pixels[dstIndex]-srcPixels[srcIndex];
						pixels[dstIndex] = dst<0?-dst:dst;
					}
					break;
				case SUBTRACT:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
						pixels[dstIndex] = pixels[dstIndex]-srcPixels[srcIndex];
					break;
				case MULTIPLY:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
						pixels[dstIndex] = srcPixels[srcIndex]*pixels[dstIndex];
					break;
				case DIVIDE:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++) {
							src = srcPixels[srcIndex];
							if (useDBZValue && src==0.0)
								pixels[dstIndex] = divideByZeroValue;
							else
								pixels[dstIndex] = pixels[dstIndex]/src;
						}
					break;
				case AND:
					for (int i=r1.width; --i>=0;) {
						dst = (int)srcPixels[srcIndex++]&(int)pixels[dstIndex];
						pixels[dstIndex++] = dst;
					}
					break;
				case OR:
					for (int i=r1.width; --i>=0;) {
						dst = (int)srcPixels[srcIndex++]|(int)pixels[dstIndex];
						pixels[dstIndex++] = dst;
					}
					break;
				case XOR:
					for (int i=r1.width; --i>=0;) {
						dst = (int)srcPixels[srcIndex++]^(int)pixels[dstIndex];
						pixels[dstIndex++] = dst;
					}
					break;
				case MIN:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++];
						dst = pixels[dstIndex];
						if (src<dst) dst = src;
						pixels[dstIndex++] = dst;
					}
					break;
				case MAX:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++];
						dst = pixels[dstIndex];
						if (src>dst) dst = src;
						pixels[dstIndex++] = dst;
					}
					break;
			}
			if (y%20==0)
				ip.showProgress((double)(y-r1.y)/r1.height);
		}
		ip.showProgress(1.0);
	}
}