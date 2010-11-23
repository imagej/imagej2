package ijx.process;
import java.awt.*;
import java.awt.image.*;

/** This class does bit blitting of RGB images. */
public class ColorBlitter implements Blitter {

	private ColorProcessor ip;
	private int width, height;
	private int[] pixels;
	private int transparent = 0xffffff;
	
	/** Constructs a ColorBlitter from a ColorProcessor. */
	public ColorBlitter(ColorProcessor ip) {
		this.ip = ip;
		width = ip.getWidth();
		height = ip.getHeight();
		pixels = (int[])ip.getPixels();
	}

	public void setTransparentColor(Color c) {
		transparent = c.getRGB()&0xffffff;
	}

	/** Copies the RGB image in 'ip' to (x,y) using the specified mode. */
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
		int srcIndex, dstIndex;
		int xSrcBase, ySrcBase;
		int[] srcPixels;
		
		int srcWidth = ip.getWidth();
		int srcHeight = ip.getHeight();
		Rectangle rect1 = new Rectangle(srcWidth, srcHeight);
		rect1.setLocation(xloc, yloc);
		Rectangle rect2 = new Rectangle(width, height);
		if (!rect1.intersects(rect2))
			return;
		if (ip instanceof ByteProcessor) {
			byte[] pixels8 = (byte[])ip.getPixels();
			ColorModel cm = ip.getColorModel();
			if (ip.isInvertedLut())
				cm = ip.getDefaultColorModel();
			int size = ip.getWidth()*ip.getHeight();
			srcPixels = new int[size];
			int v;
			for (int i=0; i<size; i++)
				srcPixels[i] = cm.getRGB(pixels8[i]&255);
		} else
			srcPixels = (int[])ip.getPixels();
		rect1 = rect1.intersection(rect2);
		xSrcBase = (xloc<0)?-xloc:0;
		ySrcBase = (yloc<0)?-yloc:0;
		int c1, c2, r1, g1, b1, r2, g2, b2;
		int src, dst;
		
		if (mode==COPY||mode==COPY_TRANSPARENT|| mode==COPY_ZERO_TRANSPARENT) {
			for (int y=rect1.y; y<(rect1.y+rect1.height); y++) {
				srcIndex = (y-yloc)*srcWidth + (rect1.x-xloc);
				dstIndex = y * width + rect1.x;
				int trancolor = mode==COPY_ZERO_TRANSPARENT?0:transparent;
				if (mode==COPY) {
					for (int i=rect1.width; --i>=0;)
						pixels[dstIndex++] = srcPixels[srcIndex++];
				} else {
					for (int i=rect1.width; --i>=0;) {
						src = srcPixels[srcIndex++];
						dst = pixels[dstIndex];
						pixels[dstIndex++] = (src&0xffffff)==trancolor?dst:src;
					}
				} 
			}
			return;
		}
		
		for (int y=rect1.y; y<(rect1.y+rect1.height); y++) {
			srcIndex = (y-yloc)*srcWidth + (rect1.x-xloc);
			dstIndex = y * width + rect1.x;
			for (int i=rect1.width; --i>=0;) {
				c1 = srcPixels[srcIndex++];
				r1 = (c1&0xff0000)>>16;
				g1 = (c1&0xff00)>>8;
				b1 = c1&0xff;
				c2 = pixels[dstIndex];
				r2 = (c2&0xff0000)>>16;
				g2 = (c2&0xff00)>>8;
				b2 = c2&0xff;
				switch (mode) {
					case COPY_INVERTED:
						break;
					case ADD:
							r2=r1+r2; g2=g1+g2; b2=b1+b2;
							if (r2>255) r2=255; if (g2>255) g2=255; if (b2>255) b2=255;
						break;
					case AVERAGE:
							r2=(r1+r2)/2; g2=(g1+g2)/2; b2=(b1+b2)/2;
						break;
					case SUBTRACT:
							r2=r2-r1; g2=g2-g1; b2=b2-b1;
							if (r2<0) r2=0; if (g2<0) g2=0; if (b2<0) b2=0;
						break;
					case DIFFERENCE:
							r2=r2-r1; if (r2<0) r2=-r2;
							g2=g2-g1; if (g2<0) g2=-g2;
							b2=b2-b1; if (b2<0) b2=-b2;
						break;
					case MULTIPLY:
							r2=r1*r2; g2=g1*g2; b2=b1*b2;
							if (r2>255) r2=255; if (g2>255) g2=255; if (b2>255) b2=255;
						break;
					case DIVIDE:
							if (r1==0) r2=255; else r2=r2/r1;
							if (g1==0) g2=255; else g2=g2/g1;
							if (b1==0) b2=255; else b2=b2/b1;
						break;
					case AND:
						r2=r1&r2; g2=g1&g2; b2=b1&b2;
						break;
					case OR:
						r2=r1|r2; g2=g1|g2; b2=b1|b2;
						break;
					case XOR:
						r2=r1^r2; g2=g1^g2; b2=b1^b2;
						break;
					case MIN:
						if (r1<r2) r2 = r1;
						if (g1<g2) g2 = g1;
						if (b1<b2) b2 = b1;
						break;
					case MAX:
						if (r1>r2) r2 = r1;
						if (g1>g2) g2 = g1;
						if (b1>b2) b2 = b1;
						break;
				}
				pixels[dstIndex++] = 0xff000000 + (r2<<16) + (g2<<8) + b2;
			}
			if (y%20==0)
				ip.showProgress((double)(y-rect1.y)/rect1.height);
		}
		ip.showProgress(1.0);
	}
}
