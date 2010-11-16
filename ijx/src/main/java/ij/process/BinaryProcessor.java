package ij.process;
//import ij.*;
import java.awt.*;

/** This class processes binary images. */
public class BinaryProcessor extends ByteProcessor {

	static int[] table  =
		//0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1
		 {0,0,0,1,0,0,1,3,0,0,3,1,1,0,1,3,0,0,0,0,0,0,0,0,2,0,2,0,3,0,3,3,
		  0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,3,0,2,2,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  2,0,0,0,0,0,0,0,2,0,0,0,2,0,0,0,3,0,0,0,0,0,0,0,3,0,0,0,3,0,2,0,
		  0,0,3,1,0,0,1,3,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,
		  3,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  2,3,1,3,0,0,1,3,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  2,3,0,1,0,0,0,1,0,0,0,0,0,0,0,0,3,3,0,1,0,0,0,0,2,2,0,0,2,0,0,0};
	private ByteProcessor parent;
	
	/** Creates a BinaryProcessor from a ByteProcessor. The ByteProcessor
		must contain a binary image (pixels values are either 0 or 255).
		Backgound is assumed to be white. */
	public BinaryProcessor(ByteProcessor ip) {
		super(ip.getWidth(), ip.getHeight(), (byte[])ip.getPixels(), ip.getColorModel());
		setRoi(ip.getRoi());
		parent = ip;
	}

	static final int OUTLINE=0;
	
	void process(int type, int count) {
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int inc = roiHeight/25;
		if (inc<1) inc = 1;
		int bgColor = 255;
		if (parent.isInvertedLut())
			bgColor = 0;

		byte[] pixels2 = (byte[])parent.getPixelsCopy();
		int offset, v=0, sum;
        int rowOffset = width;
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			p2 = pixels2[offset-rowOffset-1]&0xff;
			p3 = pixels2[offset-rowOffset]&0xff;
			p5 = pixels2[offset-1]&0xff;
			p6 = pixels2[offset]&0xff;
			p8 = pixels2[offset+rowOffset-1]&0xff;
			p9 = pixels2[offset+rowOffset]&0xff;

			for (int x=xMin; x<=xMax; x++) {
				p1 = p2; p2 = p3;
				p3 = pixels2[offset-rowOffset+1]&0xff;
				p4 = p5; p5 = p6;
				p6 = pixels2[offset+1]&0xff;
				p7 = p8; p8 = p9;
				p9 = pixels2[offset+rowOffset+1]&0xff;

				switch (type) {
					case OUTLINE:
						v = p5;
						if (v!=bgColor) {
							if (!(p1==bgColor || p2==bgColor || p3==bgColor || p4==bgColor
								|| p6==bgColor || p7==bgColor || p8==bgColor || p9==bgColor))
									v = bgColor;
						}
						break;
				}
				
				pixels[offset++] = (byte)v;
			}
			if (y%inc==0)
				parent.showProgress((double)(y-roiY)/roiHeight);
		}
		parent.hideProgress();
	}

	/** Uses a lookup table to repeatably removes pixels from the
		edges of objects in a binary image, reducing them to single
		pixel wide skeletons. Based on an a thinning algorithm by
		by Zhang and Suen (CACM, March 1984, 236-239). There is
		an entry in the table for each of the 256 possible 3x3 neighborhood
		configurations. An entry of '1' means delete pixel on first pass, '2' means
		delete pixel on second pass, and '3' means delete on either pass. A graphical
		representation of the 256 neighborhoods indexed by the table is available
		at "http://rsb.info.nih.gov/ij/images/skeletonize-table.gif".
	*/
	public void  skeletonize() {
		int pass = 0;
		int pixelsRemoved;
		resetRoi();
		setColor(Color.white);
		moveTo(0,0); lineTo(0,height-1);
		moveTo(0,0); lineTo(width-1,0);
		moveTo(width-1,0); lineTo(width-1,height-1);
		moveTo(0,height-1); lineTo(width/*-1*/,height-1);
		//IjxImageStack movie=null;
		//boolean debug = IJ.altKeyDown();
		//if (debug) movie = IJ.getFactory().newImageStack(width, height);
		do {
			snapshot();
			//if (debug) movie.addSlice(""+pass, duplicate());
			pixelsRemoved = thin(pass++, table);
			snapshot();
			//if (debug) movie.addSlice(""+pass, duplicate());
			pixelsRemoved = thin(pass++, table);
			//ij.IJ.write(pass+" "+pixelsRemoved);
		} while (pixelsRemoved>0);
		//if (debug) IJ.getFactory().newImagePlus("Skel Movie", movie).show();
	}

	int thin(int pass, int[] table) {
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int inc = roiHeight/25;
		if (inc<1) inc = 1;
		int bgColor = 255;
		if (parent.isInvertedLut())
			bgColor = 0;
			
		byte[] pixels2 = (byte[])getPixelsCopy();
		int v, index, code;
        int offset, rowOffset = width;
        int pixelsRemoved = 0;
        int count = 100;
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			for (int x=xMin; x<=xMax; x++) {
				p5 = pixels2[offset]&0xff;
				v = p5;
				if (v!=bgColor) {
					p1 = pixels2[offset-rowOffset-1]&0xff;
					p2 = pixels2[offset-rowOffset]&0xff;
					p3 = pixels2[offset-rowOffset+1]&0xff;
					p4 = pixels2[offset-1]&0xff;
					p6 = pixels2[offset+1]&0xff;
					p7 = pixels2[offset+rowOffset-1]&0xff;
					p8 = pixels2[offset+rowOffset]&0xff;
					p9 = pixels2[offset+rowOffset+1]&0xff;
					index = 0;
					if (p1!=bgColor) index |= 1;
					if (p2!=bgColor) index |= 2;
					if (p3!=bgColor) index |= 4;
					if (p6!=bgColor) index |= 8;
					if (p9!=bgColor) index |= 16;
					if (p8!=bgColor) index |= 32;
					if (p7!=bgColor) index |= 64;
					if (p4!=bgColor) index |= 128;
					code = table[index];
					if ((pass&1)==1) { //odd pass
						if (code==2 || code==3) {
							v = bgColor;
							pixelsRemoved++;
						}
					} else { //even pass
						if (code==1 || code==3) {
							v = bgColor;
							pixelsRemoved++;
						}
					}
				}
				pixels[offset++] = (byte)v;
			}
			if (y%inc==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		hideProgress();
		return pixelsRemoved;
	}
	
	public void outline() {
		process(OUTLINE, 0);
	}
	
}
