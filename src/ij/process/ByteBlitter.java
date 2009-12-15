package ij.process;
import java.awt.*;

/** This class does bit blitting of byte images. */
public class ByteBlitter implements Blitter {

	private ByteProcessor ip;
	private int width, height;
	private byte[] pixels;
	private int transparent = 255;
	
	/** Constructs a ByteBlitter from a ByteProcessor. */
	public ByteBlitter(ByteProcessor ip) {
		this.ip = ip;
		width = ip.getWidth();
		height = ip.getHeight();
		pixels = (byte[])ip.getPixels();
	}

	public void setTransparentColor(Color c) {
		transparent = ip.getBestIndex(c);
		//ij.IJ.write(c+" "+transparent);
	}
	
	/** Copies the byte image in 'ip' to (x,y) using the specified mode. */
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
		Rectangle r1, r2;
		int srcIndex, dstIndex;
		int xSrcBase, ySrcBase;
		byte[] srcPixels;
		
		int srcWidth = ip.getWidth();
		int srcHeight = ip.getHeight();
		r1 = new Rectangle(srcWidth, srcHeight);
		r1.setLocation(xloc, yloc);
		r2 = new Rectangle(width, height);
		if (!r1.intersects(r2))
			return;
		if (ip instanceof ColorProcessor) {
			int[] pixels32 = (int[])ip.getPixels();
			int size = ip.getWidth()*ip.getHeight();
			srcPixels = new byte[size];
			if (this.ip.isInvertedLut())
				for (int i=0; i<size; i++)
					srcPixels[i] = (byte)(255-pixels32[i]&255);
			else 
				for (int i=0; i<size; i++)
					srcPixels[i] = (byte)(pixels32[i]&255);
		} else
			srcPixels = (byte [])ip.getPixels();
		r1 = r1.intersection(r2);
		xSrcBase = (xloc<0)?-xloc:0;
		ySrcBase = (yloc<0)?-yloc:0;
		int src, dst;
		for (int y=r1.y; y<(r1.y+r1.height); y++) {
			srcIndex = (y-yloc)*srcWidth + (r1.x-xloc);
			dstIndex = y * width + r1.x;
			switch (mode) {
				case COPY:
					for (int i=r1.width; --i>=0;)
						pixels[dstIndex++] = srcPixels[srcIndex++];
					break;
				case COPY_INVERTED:
					for (int i=r1.width; --i>=0;)
						pixels[dstIndex++] = (byte)(255-srcPixels[srcIndex++]&255);
					break;
				case COPY_TRANSPARENT:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&255;
						if (src==transparent)
							dst = pixels[dstIndex];
						else
							dst = src;
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case COPY_ZERO_TRANSPARENT:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&255;
						if (src==0)
							dst = pixels[dstIndex];
						else
							dst = src;
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case ADD:
					for (int i=r1.width; --i>=0;) {
						dst = (srcPixels[srcIndex++]&255)+(pixels[dstIndex]&255);
						if (dst>255) dst = 255;
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case AVERAGE:
					for (int i=r1.width; --i>=0;) {
						dst = ((srcPixels[srcIndex++]&255)+(pixels[dstIndex]&255))/2;
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case SUBTRACT:
					for (int i=r1.width; --i>=0;) {
						dst = (pixels[dstIndex]&255)-(srcPixels[srcIndex++]&255);
						if (dst<0) dst = 0;
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case DIFFERENCE:
					for (int i=r1.width; --i>=0;) {
						dst = (pixels[dstIndex]&255)-(srcPixels[srcIndex++]&255);
						if (dst<0) dst = -dst;
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case MULTIPLY:
					for (int i=r1.width; --i>=0;) {
						dst = (srcPixels[srcIndex++]&255)*(pixels[dstIndex]&255);
						if (dst>255) dst = 255;
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case DIVIDE:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&255;
						if (src==0)
							dst = 255;
						else
							dst = (pixels[dstIndex]&255)/src;
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case AND:
					for (int i=r1.width; --i>=0;) {
						dst = srcPixels[srcIndex++]&pixels[dstIndex];
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case OR:
					for (int i=r1.width; --i>=0;) {
						dst = srcPixels[srcIndex++]|pixels[dstIndex];
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case XOR:
					for (int i=r1.width; --i>=0;) {
						dst = srcPixels[srcIndex++]^pixels[dstIndex];
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case MIN:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&255;
						dst = pixels[dstIndex]&255;
						if (src<dst) dst = src;
						pixels[dstIndex++] = (byte)dst;
					}
					break;
				case MAX:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&255;
						dst = pixels[dstIndex]&255;
						if (src>dst) dst = src;
						pixels[dstIndex++] = (byte)dst;
					}
					break;
			}
			if (y%20==0)
				ip.showProgress((double)(y-r1.y)/r1.height);
		}
		ip.hideProgress();
	}
}