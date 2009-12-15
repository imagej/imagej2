package ij.process;
import java.awt.image.*;

	/* This is an indexed color model that allows an
		lower and upper bound to be specified. */
    public class LUT extends IndexColorModel implements Cloneable {
	public double min, max;
	
    public LUT(byte r[], byte g[], byte b[]) {
    	this(8, 256, r, g, b);
	}
	
    public LUT(int bits, int size, byte r[], byte g[], byte b[]) {
    	super(bits, size, r, g, b);
	}
	
	public byte[] getBytes() {
		int size = getMapSize();
		if (size!=256) return null;
		byte[] bytes = new byte[256*3];
		for (int i=0; i<256; i++) bytes[i] = (byte)getRed(i);
		for (int i=0; i<256; i++) bytes[256+i] = (byte)getGreen(i);
		for (int i=0; i<256; i++) bytes[512+i] = (byte)getBlue(i);
		return bytes;
	}
	
	public LUT createInvertedLut() {
		int mapSize = getMapSize();
		byte[] reds = new byte[mapSize];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];	
		byte[] reds2 = new byte[mapSize];
		byte[] greens2 = new byte[mapSize];
		byte[] blues2 = new byte[mapSize];	
		getReds(reds); 
		getGreens(greens); 
		getBlues(blues);
		for (int i=0; i<mapSize; i++) {
			reds2[i] = (byte)(reds[mapSize-i-1]&255);
			greens2[i] = (byte)(greens[mapSize-i-1]&255);
			blues2[i] = (byte)(blues[mapSize-i-1]&255);
		}
		return new LUT(8, mapSize, reds2, greens2, blues2);
	}



	public synchronized Object clone() {
		try {return super.clone();}
		catch (CloneNotSupportedException e) {return null;}
	}
	
}
