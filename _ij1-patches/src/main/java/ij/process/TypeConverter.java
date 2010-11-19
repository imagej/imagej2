package ij.process;
import java.awt.*;
import java.awt.image.*;

/** This class converts an ImageProcessor to another data type. */
public class TypeConverter {

	private enum ConvertFrom {BYTE, SHORT, FLOAT, RGB, OTHER};
	private ImageProcessor ip;
	private ConvertFrom type;
	boolean doScaling = true;
	int width, height;

	public TypeConverter(ImageProcessor ip, boolean doScaling) {
		this.ip = ip;
		this.doScaling = doScaling;
		if (ip instanceof ByteProcessor)
			type = ConvertFrom.BYTE;
		else if (ip instanceof ShortProcessor)
			type = ConvertFrom.SHORT;
		else if (ip instanceof FloatProcessor)
			type = ConvertFrom.FLOAT;
		else if (ip instanceof ColorProcessor)
			type = ConvertFrom.RGB;
		else
			type = ConvertFrom.OTHER;
		
		width = ip.getWidth();
		height = ip.getHeight();
	}

	/** Converts processor to a ByteProcessor. */
	public ImageProcessor convertToByte() {
		switch (type) {
			case BYTE:
				return ip;
			case SHORT:
				return convertShortToByte();
			case FLOAT:
				return convertFloatToByte();
			case RGB:
				return convertRGBToByte();
			case OTHER:
				return convertOtherToByte();
			default:
				return null;
		}
	}

	/** Converts a ShortProcessor to a ByteProcessor. */
	ByteProcessor convertShortToByte() {
		int size = width*height;
		short[] pixels16 = (short[])ip.getPixels();
		byte[] pixels8 = new byte[size];
		if (doScaling) {
			int value, min=(int)ip.getMin(), max=(int)ip.getMax();
			double scale = 256.0/(max-min+1);
			for (int i=0; i<size; i++) {
				value = (pixels16[i]&0xffff)-min;
				if (value<0) value = 0;
				value = (int)(value*scale+0.5);
				if (value>255) value = 255;
				pixels8[i] = (byte)value;
			}
			return new ByteProcessor(width, height, pixels8, ip.getCurrentColorModel());
		} else {
			int value;
			for (int i=0; i<size; i++) {
				value = pixels16[i]&0xffff;
				if (value>255) value = 255;
				pixels8[i] = (byte)value;
			}
			return new ByteProcessor(width, height, pixels8, ip.getColorModel());
		}
	}

	/** Converts a FloatProcessor to a ByteProcessor. */
	ByteProcessor convertFloatToByte() {
		if (doScaling) {
			Image img = ip.createImage();
			return new ByteProcessor(img);
		} else {
			ByteProcessor bp = new ByteProcessor(width, height);
			bp.setPixels(0, (FloatProcessor)ip);
			bp.setColorModel(ip.getColorModel());
			bp.resetMinAndMax();		//don't take min&max from ip
			return bp;
		}
	}

	/** Converts a ColorProcessor to a ByteProcessor. 
		The pixels are converted to grayscale using the formula
		g=r/3+g/3+b/3. Call ColorProcessor.setWeightingFactors() 
		to do weighted conversions. */
	ByteProcessor convertRGBToByte() {
		int c, r, g, b;
		int[] pixels32;
		byte[] pixels8;
		
		//get RGB pixels
		pixels32 = (int[])ip.getPixels();
		
		//convert to grayscale
		double[] w = ColorProcessor.getWeightingFactors();
		double rw=w[0], gw=w[1], bw=w[2];
		pixels8 = new byte[width*height];
		for (int i=0; i < width*height; i++) {
			c = pixels32[i];
			r = (c&0xff0000)>>16;
			g = (c&0xff00)>>8;
			b = c&0xff;
			pixels8[i] = (byte)(r*rw + g*gw + b*bw + 0.5);
		}
		
		return new ByteProcessor(width, height, pixels8, null);
	}
	
	/** creates a ByteProcessor from another processor */
	ByteProcessor convertOtherToByte()
	{
		int totPix = width * height;
		
		byte[] bytes = new byte[totPix];
		
		for (int i = 0; i < totPix; i++)
		{
			double value = ip.getd(i);
			byte theByte;
			if (doScaling)
			{
				double min = ip.getMin();
				double max = ip.getMax();
				double newValue = Math.round(255*(value-min)/(max-min));
				if (newValue < 0) newValue = 0;
				if (newValue > 255) newValue = 255;
				theByte = (byte) newValue;
			}
			else
				theByte = (byte) value;
			
			bytes[i] = theByte;
		}
		
		return new ByteProcessor(width, height, bytes, ip.getColorModel());
	}
	
	/** Converts processor to a ShortProcessor. */
	public ImageProcessor convertToShort() {
		switch (type) {
			case BYTE:
				return convertByteToShort();
			case SHORT:
				return ip;
			case FLOAT:
				return convertFloatToShort();
			case RGB:
				ip = convertRGBToByte();
				return convertByteToShort();
			case OTHER:
				return convertOtherToShort();
			default:
				return null;
		}
	}

	/** Converts a ByteProcessor to a ShortProcessor. */
	ShortProcessor convertByteToShort() {
		if (!ip.isDefaultLut() && !ip.isColorLut() && !ip.isInvertedLut()) {
			// apply custom LUT
			ip = convertToRGB();
			ip = convertRGBToByte();
			return (ShortProcessor)convertByteToShort();
		}
		byte[] pixels8 = (byte[])ip.getPixels();
		short[] pixels16 = new short[width * height];
		for (int i=0; i<width*height; i++)
			pixels16[i] = (short)(pixels8[i]&0xff);
	    return new ShortProcessor(width, height, pixels16, ip.getColorModel());
	}

	/** Converts a FloatProcessor to a ShortProcessor. */
	ShortProcessor convertFloatToShort() {
		float[] pixels32 = (float[])ip.getPixels();
		short[] pixels16 = new short[width*height];
		double min = ip.getMin();
		double max = ip.getMax();
		double scale;
		if ((max-min)==0.0)
			scale = 1.0;
		else
			scale = 65535.0/(max-min);
		double value;
		for (int i=0; i<width*height; i++) {
			if (doScaling)
				value = (pixels32[i]-min)*scale;
			else
				value = pixels32[i];
			if (value<0.0) value = 0.0;
			if (value>65535.0) value = 65535.0;
			pixels16[i] = (short)(value+0.5);
		}
	    return new ShortProcessor(width, height, pixels16, ip.getColorModel());
	}

	/** creates a ShortProcessor from an processor of unknown type */
	ShortProcessor convertOtherToShort()
	{
		int totPix = width * height;
		
		short[] shorts = new short[totPix];
		
		for (int i = 0; i < totPix; i++)
		{
			double value = ip.getd(i);
			short theShort;
			if (doScaling)
			{
				double min = ip.getMin();
				double max = ip.getMax();
				double newValue = Math.round(65535*(value-min)/(max-min));
				if (newValue < 0) newValue = 0;
				if (newValue > 65535) newValue = 65535;
				theShort = (short) newValue;
			}
			else
				theShort = (short) value;
			
			shorts[i] = theShort;
		}
		
		return new ShortProcessor(width, height, shorts, ip.getColorModel());
	}
	
	/** Converts processor to a FloatProcessor. */
	public ImageProcessor convertToFloat(float[] ctable) {
		switch (type) {
			case BYTE:
				return convertByteToFloat(ctable);
			case SHORT:
				return convertShortToFloat(ctable);
			case FLOAT:
				return ip;
			case RGB:
				ip = convertRGBToByte();
				return convertByteToFloat(null);
			case OTHER:
				return convertOtherToFloat();
			default:
				return null;
		}
	}

	/** Converts a ByteProcessor to a FloatProcessor. Applies a
		calibration function if the calibration table is not null.
		@see ImageProcessor.setCalibrationTable
	 */
	FloatProcessor convertByteToFloat(float[] cTable) {
		if (!ip.isDefaultLut() && !ip.isColorLut() && !ip.isInvertedLut()) {
			// apply custom LUT
			ip = convertToRGB();
			ip = convertRGBToByte();
			return (FloatProcessor)convertByteToFloat(null);
		}
		byte[] pixels8 = (byte[])ip.getPixels();
		float[] pixels32 = new float[width*height];
		if (cTable!=null && cTable.length==256) {
			for (int i=0; i<width*height; i++)
				pixels32[i] = cTable[pixels8[i]&255];
		} else {
			for (int i=0; i<width*height; i++)
				pixels32[i] = pixels8[i]&255;
		}
	    ColorModel cm = ip.getColorModel();
	    return new FloatProcessor(width, height, pixels32, cm);
	}

	/** Converts a ShortProcessor to a FloatProcessor. Applies a
		calibration function if the calibration table is not null.
		@see ImageProcessor.setCalibrationTable
	 */
	FloatProcessor convertShortToFloat(float[] cTable) {
		short[] pixels16 = (short[])ip.getPixels();
		float[] pixels32 = new float[width*height];
		if (cTable!=null && cTable.length==65536)
			for (int i=0; i<width*height; i++)
				pixels32[i] = cTable[pixels16[i]&0xffff];
		else
			for (int i=0; i<width*height; i++)
				pixels32[i] = pixels16[i]&0xffff;
	    ColorModel cm = ip.getColorModel();
	    return new FloatProcessor(width, height, pixels32, cm);
	}
	
	/** Creates an FloatProcessor from an unknown processor */
	FloatProcessor convertOtherToFloat()
	{
		int totPix = width * height;
		
		float[] floats = new float[width*height];
		
		for (int i = 0; i < totPix; i++)
		{
			floats[i] = ip.getf(i);
		}
		
	    return new FloatProcessor(width, height, floats, ip.getColorModel());
	}
	
	/** Converts processor to a ColorProcessor. */
	public ImageProcessor convertToRGB() {
		if (type==ConvertFrom.RGB)
			return ip;
		else {
			ImageProcessor ip2 = ip.convertToByte(doScaling);
			return new ColorProcessor(ip2.createImage());
		}
	}

}
