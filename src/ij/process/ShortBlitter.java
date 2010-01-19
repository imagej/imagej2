package ij.process;
import java.awt.*;

/** This class does bit blitting of 16-bit images. */
public class ShortBlitter implements Blitter {

	private ShortProcessor ip;
	private int width, height;
	private short[] pixels;
	
	public void setTransparentColor(Color c) {
	}

	/** Constructs a ShortBlitter from a ShortProcessor. */
	public ShortBlitter(ShortProcessor ip) {
		this.ip = ip;
		width = ip.getWidth();
		height = ip.getHeight();
		pixels = (short[])ip.getPixels();
	}

	/** Copies the 16-bit image in 'ip' to (x,y) using the specified mode. */
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
		Rectangle r1, r2;
		int srcIndex, dstIndex;
		int xSrcBase, ySrcBase;
		short[] srcPixels;
		
		int srcWidth = ip.getWidth();
		int srcHeight = ip.getHeight();
		r1 = new Rectangle(srcWidth, srcHeight);
		r1.setLocation(xloc, yloc);
		r2 = new Rectangle(width, height);
		if (!r1.intersects(r2))
			return;
		srcPixels = (short [])ip.getPixels();
//new ij.ImagePlus("srcPixels", new ShortProcessor(srcWidth, srcHeight, srcPixels, null)).show();
		r1 = r1.intersection(r2);
		xSrcBase = (xloc<0)?-xloc:0;
		ySrcBase = (yloc<0)?-yloc:0;
		int src, dst;
		for (int y=r1.y; y<(r1.y+r1.height); y++) {
			srcIndex = (y-yloc)*srcWidth + (r1.x-xloc);
			dstIndex = y * width + r1.x;
			switch (mode) {
				case COPY: case COPY_INVERTED: case COPY_TRANSPARENT:
					for (int i=r1.width; --i>=0;)
						pixels[dstIndex++] = srcPixels[srcIndex++];
					break;
				case COPY_ZERO_TRANSPARENT:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&0xffff;
						if (src==0)
							dst = pixels[dstIndex];
						else
							dst = src;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case ADD:
					for (int i=r1.width; --i>=0;) {
						dst = (srcPixels[srcIndex++]&0xffff)+(pixels[dstIndex]&0xffff);
						if (dst<0) dst = 0;
						if (dst>65535) dst = 65535;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case AVERAGE:
					for (int i=r1.width; --i>=0;) {
						dst = ((srcPixels[srcIndex++]&0xffff)+(pixels[dstIndex]&0xffff))/2;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case DIFFERENCE:
					for (int i=r1.width; --i>=0;) {
						dst = (pixels[dstIndex]&0xffff)-(srcPixels[srcIndex++]&0xffff);
						if (dst<0) dst = -dst;
						if (dst>65535) dst = 65535;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case SUBTRACT:
					for (int i=r1.width; --i>=0;) {
						dst = (pixels[dstIndex]&0xffff)-(srcPixels[srcIndex++]&0xffff);
						if (dst<0) dst = 0;
						if (dst>65535) dst = 65535;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case MULTIPLY:
					for (int i=r1.width; --i>=0;) {
						dst = (srcPixels[srcIndex++]&0xffff)*(pixels[dstIndex]&0xffff);
						if (dst<0) dst = 0;
						if (dst>65535) dst = 65535;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case DIVIDE:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&0xffff;
						if (src==0)
							dst = 65535;
						else
							dst = pixels[dstIndex]/src;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case AND:
					for (int i=r1.width; --i>=0;) {
						dst = srcPixels[srcIndex++]&pixels[dstIndex]&0xffff;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case OR:
					for (int i=r1.width; --i>=0;) {
						dst = srcPixels[srcIndex++]|pixels[dstIndex];
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case XOR:
					for (int i=r1.width; --i>=0;) {
						dst = srcPixels[srcIndex++]^pixels[dstIndex];
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case MIN:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&0xffff;
						dst = pixels[dstIndex]&0xffff;
						if (src<dst) dst = src;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case MAX:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&0xffff;
						dst = pixels[dstIndex]&0xffff;
						if (src>dst) dst = src;
						pixels[dstIndex++] = (short)dst;
					}
					break;
			}
			if (y%20==0)
				ip.showProgress((double)(y-r1.y)/r1.height);
		}
		ip.showProgress(1.0);
	}
}
