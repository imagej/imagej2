package ij.io;
import ij.gui.*;
import java.io.*;
import java.util.*;
import java.net.*;

/*	ImageJ/NIH Image 64 byte ROI outline header
	2 byte numbers are big-endian signed shorts
	
	0-3		"Iout"
	4-5		version (>=217)
	6-7		roi type
	8-9		top
	10-11	left
	12-13	bottom
	14-15	right
	16-17	NCoordinates
	18-33	x1,y1,x2,y2 (straight line)
	34-35	line width (unused)
	36-39   ShapeRoi size (type must be 1 if this value>0)
	40-63	reserved (zero)
	64-67   x0, y0 (polygon)
	68-71   x1, y1 
	etc.
	
*/

/** Decodes an ImageJ, NIH Image or Scion Image ROI. */
public class RoiDecoder {

	private final int polygon=0, rect=1, oval=2, line=3, freeline=4, polyline=5, noRoi=6, freehand=7, traced=8, angle=9, point=10;
	private byte[] data;
	private String path;
	private InputStream is;
	private String name;
	private int size;

	/** Constructs an RoiDecoder using a file path. */
	public RoiDecoder(String path) {
		this.path = path;
	}

	/** Constructs an RoiDecoder using a byte array. */
	public RoiDecoder(byte[] bytes, String name) {
		is = new ByteArrayInputStream(bytes);	
		this.name = name;
		this.size = bytes.length;
	}

	/** Returns the ROI. */
	public Roi getRoi() throws IOException {
		if (path!=null) {
			File f = new File(path);
			size = (int)f.length();
			if (size>500000)
				throw new IOException("This is not an ImageJ ROI");
			name = f.getName();
			is = new FileInputStream(path);
		}
		data = new byte[size];

		int total = 0;
		while (total<size)
			total += is.read(data, total, size-total);
		is.close();
		if (getByte(0)!=73 || getByte(1)!=111)  //"Iout"
			throw new IOException("This is not an ImageJ ROI");
		int type = getByte(6);
		int top= getShort(8);
		int left = getShort(10);
		int bottom = getShort(12);
		int right = getShort(14);
		int width = right-left;
		int height = bottom-top;
		int n = getShort(16);
		
		if (name.endsWith(".roi"))
			name = name.substring(0, name.length()-4);
		boolean isComposite = getInt(36)>0;		
		if (isComposite)
			return getShapeRoi();

		Roi roi = null;
		switch (type) {
		case rect:
			roi = new Roi(left, top, width, height);
			break;
		case oval:
			roi = new OvalRoi(left, top, width, height);
			break;
		case line:
			int x1 = (int)getFloat(18);		
			int y1 = (int)getFloat(22);		
			int x2 = (int)getFloat(26);		
			int y2 = (int)getFloat(30);
			roi = new Line(x1, y1, x2, y2);		
			//IJ.write("line roi: "+x1+" "+y1+" "+x2+" "+y2);
			break;
		case polygon: case freehand: case traced: case polyline: case freeline: case angle: case point:
				//IJ.write("type: "+type);
				//IJ.write("n: "+n);
				//IJ.write("rect: "+left+","+top+" "+width+" "+height);
				if (n==0) break;
				int[] x = new int[n];
				int[] y = new int[n];
				int base1 = 64;
				int base2 = base1+2*n;
				int xtmp, ytmp;
				for (int i=0; i<n; i++) {
					xtmp = getShort(base1+i*2);
					if (xtmp<0) xtmp = 0;
					ytmp = getShort(base2+i*2);
					if (ytmp<0) ytmp = 0;
					x[i] = left+xtmp;
					y[i] = top+ytmp;
					//IJ.write(i+" "+getShort(base1+i*2)+" "+getShort(base2+i*2));
				}
				if (type==point) {
					roi = new PointRoi(x, y, n);
					break;
				}
				int roiType;
				if (type==polygon)
					roiType = Roi.POLYGON;
				else if (type==freehand)
					roiType = Roi.FREEROI;
				else if (type==traced)
					roiType = Roi.TRACED_ROI;
				else if (type==polyline)
					roiType = Roi.POLYLINE;
				else if (type==freeline)
					roiType = Roi.FREELINE;
				else if (type==angle)
					roiType = Roi.ANGLE;
				else
					roiType = Roi.FREEROI;
				roi = new PolygonRoi(x, y, n, roiType);
				break;
		default:
			throw new IOException("Unrecognized ROI type: "+type);
		}
		roi.setName(name);
		return roi;
	}
	
	public Roi getShapeRoi() throws IOException {
		int type = getByte(6);
		if (type!=rect)
			throw new IllegalArgumentException("Invalid composite ROI type");
		int top= getShort(8);
		int left = getShort(10);
		int bottom = getShort(12);
		int right = getShort(14);
		int width = right-left;
		int height = bottom-top;
		int n = getInt(36);

		ShapeRoi roi = null;
		float[] shapeArray = new float[n];
		int base = 64;
		for(int i=0; i<n; i++) {
			shapeArray[i] = getFloat(base);
			base += 4;
		}
		roi = new ShapeRoi(shapeArray);
		roi.setName(name);
		return roi;
	}

	int getByte(int base) {
		return data[base]&255;
	}

	int getShort(int base) {
		int b0 = data[base]&255;
		int b1 = data[base+1]&255;
		int n = (short)((b0<<8) + b1);
		if (n<-5000)
			n = (b0<<8) + b1; // assume n>32767 and unsigned
		return n;		
	}
	
	int getInt(int base) {
		int b0 = data[base]&255;
		int b1 = data[base+1]&255;
		int b2 = data[base+2]&255;
		int b3 = data[base+3]&255;
		return ((b0<<24) + (b1<<16) + (b2<<8) + b3);
	}

	float getFloat(int base) {
		return Float.intBitsToFloat(getInt(base));
	}

}