package ijx;
import ijx.process.ImageProcessor;
import ijx.process.ColorProcessor;
import ijx.process.ByteProcessor;
import java.awt.*;
import java.awt.image.*;


/** This class represents a color look-up table. */
public class LookUpTable extends Object {
	private int width, height;
	private byte[] pixels;
	private int mapSize = 0;
	private ColorModel cm;
	private byte[] rLUT, gLUT,bLUT;

	/** Constructs a LookUpTable object from an AWT Image. */
	public LookUpTable(Image img) {
		PixelGrabber pg = new PixelGrabber(img, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
			cm = pg.getColorModel();
		}
		catch (InterruptedException e){};
		getColors(cm);
	}

	/** Constructs a LookUpTable object from a ColorModel. */
	public LookUpTable(ColorModel cm) {
		this.cm = cm;
		getColors(cm);
	}
	
	void getColors(ColorModel cm) {
    	if (cm instanceof IndexColorModel) {
    		IndexColorModel m = (IndexColorModel)cm;
    		mapSize = m.getMapSize();
    		rLUT = new byte[mapSize];
    		gLUT = new byte[mapSize];
    		bLUT = new byte[mapSize];
    		m.getReds(rLUT); 
    		m.getGreens(gLUT); 
    		m.getBlues(bLUT); 
    	}
	}
	
	public int getMapSize() {
		return mapSize;
	}
    
    public byte[] getReds() {
    	return rLUT;
    }

    public byte[] getGreens() {
    	return gLUT;
    }

    public byte[] getBlues() {
    	return bLUT;
    }

	public ColorModel getColorModel() {
		return cm;
	}

	/** Returns <code>true</code> if this is a 256 entry grayscale LUT.
		@see ij.process.ImageProcessor#isColorLut
	*/
	public boolean isGrayscale() {
		boolean isGray = true;
		
		if (mapSize < 256)
			return false;
		for (int i=0; i<mapSize; i++)
			if ((rLUT[i] != gLUT[i]) || (gLUT[i] != bLUT[i]))
				isGray = false;
		return isGray;
	}
			
	public void drawColorBar(Graphics g, int x, int y, int width, int height) {
		if (mapSize == 0)
			return;
		ColorProcessor cp = new ColorProcessor(width, height);
		double scale = 256.0/mapSize;
		for (int i = 0; i<256; i++) {
			int index = (int)(i/scale);
			cp.setColor(new Color(rLUT[index]&0xff,gLUT[index]&0xff,bLUT[index]&0xff));
			cp.moveTo(i,0); cp.lineTo(i,height);
		}
		g.drawImage(cp.createImage(),x,y,null);
		g.setColor(Color.black);
		g.drawRect(x, y, width, height);
	}

	public void drawUnscaledColorBar(ImageProcessor ip, int x, int y, int width, int height) {
		ImageProcessor bar = null;
		if (ip instanceof ColorProcessor)
			bar = new ColorProcessor(width, height);
		else
			bar = new ByteProcessor(width, height);
		if (mapSize == 0) {  //no color table; draw a grayscale bar
			for (int i = 0; i < 256; i++) {
				bar.setColor(new Color(i, i, i));
				bar.moveTo(i, 0); bar.lineTo(i, height);
			}
		}
		else {
			for (int i = 0; i<mapSize; i++) {
				bar.setColor(new Color(rLUT[i]&0xff, gLUT[i]&0xff, bLUT[i]&0xff));
				bar.moveTo(i, 0); bar.lineTo(i, height);
			}
		}
		ip.insert(bar, x,y);
		ip.setColor(Color.black);
		ip.drawRect(x-1, y, width+2, height);
	}
			
	public static ColorModel createGrayscaleColorModel(boolean invert) {
		byte[] rLUT = new byte[256];
		byte[] gLUT = new byte[256];
		byte[] bLUT = new byte[256];
		if (invert)
			for(int i=0; i<256; i++) {
				rLUT[255-i]=(byte)i;
				gLUT[255-i]=(byte)i;
				bLUT[255-i]=(byte)i;
			}
		else {
			for(int i=0; i<256; i++) {
				rLUT[i]=(byte)i;
				gLUT[i]=(byte)i;
				bLUT[i]=(byte)i;
			}
		}
		return(new IndexColorModel(8, 256, rLUT, gLUT, bLUT));
	}
	
}

