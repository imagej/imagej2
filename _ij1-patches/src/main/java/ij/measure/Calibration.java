package ij.measure;
import ij.*;
import ij.plugin.filter.Analyzer;

/** Calibration objects contain an image's spatial and density calibration data. */

// BDZ - BEGIN CHANGES
public class Calibration implements Cloneable {
// BDZ - END CHANGES

	public static final int STRAIGHT_LINE=0,POLY2=1,POLY3=2,POLY4=3,
		EXPONENTIAL=4,POWER=5,LOG=6,RODBARD=7,GAMMA_VARIATE=8, LOG2=9, RODBARD2=10;
	public static final int NONE=20, UNCALIBRATED_OD=21, CUSTOM=22;
	public static final String DEFAULT_VALUE_UNIT = "Gray Value";

	/** Pixel width in 'unit's */
	public double pixelWidth = 1.0;
	
	/** Pixel height in 'unit's */
	public double pixelHeight = 1.0;
	
	/** Pixel depth in 'unit's */
	public double pixelDepth = 1.0;
	
	/** Frame interval in 'timeUnit's */
	public double frameInterval;

	/** Frame rate in frames per second */
	public double fps;

	/** Loop back and forth when animating stack */
	public boolean loop;

	/** X origin in pixels. */
	public double xOrigin;

	/** Y origin in pixels. */
	public double yOrigin;

	/** Z origin in pixels. */
	public double zOrigin;

	/** Plugin writers can use this string to store information about the
		image. This string is saved in the TIFF header if it is not longer
		than 64 characters and it contains no '=' or '\n' characters. */
	public String info;

	/** Calibration function coefficients */
	private double[] coefficients;
		
	/* Default distance unit (e.g. 'cm', 'inch') */
	private String unit = "pixel";
	
	/* Y distance unit */
	private String yunit;

	/* Z distance unit */
	private String zunit;

	/* Distance units (e.g. 'microns', 'inches') */
	private String units;

	/* Pixel value unit (e.g. 'gray level', 'OD') */
	private String valueUnit = DEFAULT_VALUE_UNIT;

	/* Unit of time (e.g. 'sec', 'msec') */
	private String timeUnit = "sec";

	/* Calibration function ID */
	private int function = NONE;

	/* Calibration table */
	private float[] cTable;
	
	private boolean invertedLut;
	private int bitDepth = 8;
	private boolean zeroClip;
	private boolean invertY;

	/** Constructs a new Calibration object using the default values. */ 
	public Calibration(ImagePlus imp) {
		if (imp!=null) {
			bitDepth = imp.getBitDepth();
			invertedLut = imp.isInvertedLut();
		}
	}
	
	/** Constructs a new Calibration object using the default values.
		For density calibration, the image is assumed to be 8-bits. */ 
	public Calibration() {
	}
	
	/** Returns true if this image is spatially calibrated. */
	public boolean scaled() {
		return pixelWidth!=1.0 || pixelHeight!=1.0 || pixelDepth!=1.0 || !unit.equals("pixel");
	}
	
   	/** Sets the default length unit (e.g. "mm", "inch"). */
 	public void setUnit(String unit) {
 		if (unit==null || unit.equals(""))
 			this.unit = "pixel";
 		else {
 			if (unit.equals("um")) unit = "\u00B5m";
 			this.unit = unit;
 		}
 		units = null;
 	}
 	
   	/** Sets the X length unit. */
 	public void setXUnit(String unit) {
		setUnit(unit);
	}

   	/** Sets the Y length unit. */
 	public void setYUnit(String unit) {
 		yunit = unit;
	}

   	/** Sets the Z length unit. */
 	public void setZUnit(String unit) {
 		zunit = unit;
	}

 	/** Returns the default length unit (e.g. "micron", "inch"). */
 	public String getUnit() {
 		return unit;
 	}
 	
 	/** Returns the X length unit. */
 	public String getXUnit() {
 		return unit;
 	}

 	/** Returns the Y length unit, or the default unit if 'yunit' is null. */
 	public String getYUnit() {
 		return yunit!=null?yunit:unit;
 	}

 	/** Returns the Z length unit, or the default unit if 'zunit' is null. */
 	public String getZUnit() {
 		return zunit!=null?zunit:unit;
 	}

	/** Returns the plural form of the length unit (e.g. "microns", "inches"). */
 	public String getUnits() {
 		if (units==null) {
  			if (unit.equals("pixel"))
 				units = "pixels";
 			else if (unit.equals("micron"))
 				units = "microns";
  			else if (unit.equals("inch"))
 				units = "inches";
			else
 				units = unit;
 		}
 		return units;
 	}
 	
   	/** Sets the time unit (e.g. "sec", "msec"). */
 	public void setTimeUnit(String unit) {
 		if (unit==null || unit.equals(""))
 			timeUnit = "sec";
 		else
 			timeUnit = unit;
 	}

 	/** Returns the distance unit (e.g. "sec", "msec"). */
 	public String getTimeUnit() {
 		return timeUnit;
 	}

 	/** Converts a x-coodinate in pixels to physical units (e.g. mm). */
 	public double getX(double x) {
 		return (x-xOrigin)*pixelWidth;
 	}
 	
  	/** Converts a y-coodinate in pixels to physical units (e.g. mm). */
 	public double getY(double y) {
 		return (y-yOrigin)*pixelHeight;
 	}
 	
 	/** Converts a y-coodinate in pixels to physical units (e.g. mm),
 		taking into account the invertY and global "Invert Y Coordinates" flags. */
 	public double getY(double y, int imageHeight) {
 		if (invertY || (Analyzer.getMeasurements()&Measurements.INVERT_Y)!=0) {
			if (yOrigin!=0.0)
				return (yOrigin-y)*pixelHeight;
			else
				return (imageHeight-y-1)*pixelHeight;
		} else
   			return (y-yOrigin)*pixelHeight;
	}

  	/** Converts a z-coodinate in pixels to physical units (e.g. mm). */
 	public double getZ(double z) {
 		return (z-zOrigin)*pixelDepth;
 	}
 	
 	//public double getX(int x) {return getX((double)x);}
 	//public double getY(int y) {return getY((double)y);}
 	//public double getZ(int z) {return getZ((double)z);}
 	
  	/** Sets the calibration function,  coefficient table and unit (e.g. "OD"). */
 	public void setFunction(int function, double[] coefficients, String unit) {
 		setFunction(function, coefficients, unit, false);
 	}
 	
 	public void setFunction(int function, double[] coefficients, String unit, boolean zeroClip) {
 		if (function==NONE)
 			{disableDensityCalibration(); return;}
 		if (coefficients==null && function>=STRAIGHT_LINE && function<=RODBARD2)
 			return;
 		this.function = function;
 		this.coefficients = coefficients;
 		this.zeroClip = zeroClip;
 		if (unit!=null)
 			valueUnit = unit;
 		cTable = null;
 	}

 	/** Disables the density calibation if the specified image has a differenent bit depth. */
 	public void setImage(ImagePlus imp) {
 		if (imp==null)
 			return;
 		int type = imp.getType();
 		int newBitDepth = imp.getBitDepth();
 		if (newBitDepth==16 && imp.getLocalCalibration().isSigned16Bit()) {
			double[] coeff = new double[2]; coeff[0] = -32768.0; coeff[1] = 1.0;
 			setFunction(Calibration.STRAIGHT_LINE, coeff, DEFAULT_VALUE_UNIT);
		} else if (newBitDepth!=bitDepth || type==ImagePlus.GRAY32 || type==ImagePlus.COLOR_RGB) {
			String saveUnit = valueUnit;
			disableDensityCalibration();
			if (type==ImagePlus.GRAY32) valueUnit = saveUnit;
		}
 		bitDepth = newBitDepth;
 	}
 	
 	public void disableDensityCalibration() {
		function = NONE;
		coefficients = null;
		cTable = null;
		valueUnit = DEFAULT_VALUE_UNIT;
 	}
 	
	/** Returns the value unit. */
 	public String getValueUnit() {
 		return valueUnit;
 	}
 	
	/** Sets the value unit. */
 	public void setValueUnit(String unit) {
 		if (unit!=null)
 			valueUnit = unit;
 	}

 	/** Returns the calibration function coefficients. */
 	public double[] getCoefficients() {
 		return coefficients;
 	}

 	/** Returns true if this image is density calibrated. */
	public boolean calibrated() {
		return function!=NONE;
	}
	
	/** Returns the calibration function ID. */
 	public int getFunction() {
 		return function;
 	}
 	
	/** Returns the calibration table. With 8-bit images,
		the table has a length of 256. With 16-bit images,
		the length is 65536. */
 	public float[] getCTable() {
 		if (cTable==null)
 			makeCTable();
 		return cTable;
 	}
 	
	/** Sets the calibration table. With 8-bit images, the table must 
		have a length of 256. With 16-bit images, it must be 65536. */
 	public void setCTable(float[] table, String unit) {
 		if (table==null)
 			{disableDensityCalibration(); return;}
 		if (bitDepth==16 && table.length!=65536)
 			throw new IllegalArgumentException("Table.length!=65536");
 		cTable = table;
 		function = CUSTOM;
 		coefficients = null;
 		zeroClip = false;
 		if (unit!=null) valueUnit = unit;
 	}

 	void makeCTable() {
 		if (bitDepth==16)
 			{make16BitCTable(); return;}
 		if (bitDepth!=8)
 			return;
 		if (function==UNCALIBRATED_OD) {
 			cTable = new float[256];
			for (int i=0; i<256; i++)
				cTable[i] = (float)od(i);
		} else if (function>=STRAIGHT_LINE && function<=RODBARD2 && coefficients!=null) {
 			cTable = new float[256];
 			double value;
 			for (int i=0; i<256; i++) {
				value = CurveFitter.f(function, coefficients, i);
				if (zeroClip && value<0.0)
					cTable[i] = 0f;
				else
					cTable[i] = (float)value;
			}
		} else
 			cTable = null;
  	}

 	void make16BitCTable() {
		if (function>=STRAIGHT_LINE && function<=RODBARD2 && coefficients!=null) {
 			cTable = new float[65536];
 			for (int i=0; i<65536; i++)
				cTable[i] = (float)CurveFitter.f(function, coefficients, i);
		} else
 			cTable = null;
  	}

	double od(double v) {
		if (invertedLut) {
			if (v==255.0) v = 254.5;
			return 0.434294481*Math.log(255.0/(255.0-v));
		} else {
			if (v==0.0) v = 0.5;
			return 0.434294481*Math.log(255.0/v);
		}
	}
	
  	/** Converts a raw pixel value to a density calibrated value. */
 	public double getCValue(int value) {
		if (function==NONE)
			return value;
		if (function>=STRAIGHT_LINE && function<=RODBARD2 && coefficients!=null) {
			double v = CurveFitter.f(function, coefficients, value);
			if (zeroClip && v<0.0)
				return 0.0;
			else
				return v;
		}
		if (cTable==null)
			makeCTable();
 		if (cTable!=null && value>=0 && value<cTable.length)
 			return cTable[value];
 		else
 			return value;
 	}
 	 	
  	/** Converts a raw pixel value to a density calibrated value. */
 	public double getCValue(double value) {
		if (function==NONE)
			return value;
		else {
			if (function>=STRAIGHT_LINE && function<=RODBARD2 && coefficients!=null) {
				double 	v = CurveFitter.f(function, coefficients, value);
				if (zeroClip && v<0.0)
					return 0.0;
				else
					return v;
			} else
				return getCValue((int)value);
		}
 	}
 	
  	/** Converts a density calibrated value into a raw pixel value. */
 	public double getRawValue(double value) {
		if (function==NONE)
			return value;
		if (function==STRAIGHT_LINE && coefficients!=null && coefficients.length==2 && coefficients[1]!=0.0)
			return (value-coefficients[0])/coefficients[1];
		if (cTable==null)
			makeCTable();
		float fvalue = (float)value;
		float smallestDiff = Float.MAX_VALUE;
		float diff;
		int index = 0;
		for (int i=0; i<cTable.length; i++) {
			diff = fvalue - cTable[i];
			if (diff<0f) diff = -diff;
			if (diff<smallestDiff) {
				smallestDiff = diff;
				index = i;
			}
		}
 		return index;
 	}
 	 	
	/** Returns a clone of this object. */
	public Calibration copy() {
		return (Calibration)clone();
	}
	
	public synchronized Object clone() {
		try {return super.clone();}
		catch (CloneNotSupportedException e) {return null;}
	}

	/** Compares two Calibration objects for equality. */
 	public boolean equals(Calibration cal) {
 		if (cal==null)
 			return false;
 		boolean equal = true;
 		if (cal.pixelWidth!=pixelWidth || cal.pixelHeight!=pixelHeight || cal.pixelDepth!=pixelDepth)
 			equal = false;
 		if (!cal.unit.equals(unit))
 			equal = false;
 		if (!cal.valueUnit.equals(valueUnit) || cal.function!=function)
 			equal = false;
 		return equal;
 	}
 	
  	/** Returns true if this is a signed 16-bit image. */
 	public boolean isSigned16Bit() {
		return (bitDepth==16 && function>=STRAIGHT_LINE && function<=RODBARD2 && coefficients!=null
			&& coefficients[0]==-32768.0 && coefficients[1]==1.0);
 	}
 	
 	/** Sets up a calibration function that subtracts 32,768 from pixel values. */
 	public void setSigned16BitCalibration() {
		double[] coeff = new double[2];
		coeff[0] = -32768.0;
		coeff[1] = 1.0;
		setFunction(STRAIGHT_LINE, coeff, "Gray Value");
 	}

 	/** Returns true if zero clipping is enabled. */
 	public boolean zeroClip() {
 		return zeroClip;
 	}
 	
 	/** Sets the 'invertY' flag. */
 	public void setInvertY(boolean invertYCoordinates) {
 		invertY = invertYCoordinates;
 	}
 	
    public String toString() {
    	return
    		"w=" + pixelWidth
			+ ", h=" + pixelHeight
			+ ", d=" + pixelDepth
			+ ", unit=" + unit
			+ ", f=" + function
 			+ ", nc=" + (coefficients!=null?""+coefficients.length:"null")
 			+ ", table=" + (cTable!=null?""+cTable.length:"null")
			+ ", vunit=" + valueUnit;
   }
	/** Generic calibration equality test for unit tests */
// BDZ - BEGIN ADDITIONS
	// tests more internals of a Calibration than equals() does. Made to not break existing code.
	public boolean isSameAs(Calibration other)
	{
		if (this == other)
			return true;

		// I'm going to continue testing floating point for equality - usually a no-no but its precedent
		if ((pixelWidth != other.pixelWidth) ||
				(pixelHeight != other.pixelHeight) ||
				(pixelDepth != other.pixelDepth) ||
				(frameInterval != other.frameInterval) ||
				(fps != other.fps) ||
				(loop != other.loop) ||
				(xOrigin != other.xOrigin) ||
				(yOrigin != other.yOrigin) ||
				(zOrigin != other.zOrigin) ||
				((info == null && other.info != null) ||
						((info != null) && (!info.equals(other.info)))) ||
				((coefficients == null && other.coefficients != null) ||
						((coefficients != null) && (!coefficients.equals(other.coefficients)))) ||
				((unit == null && other.unit != null) ||
						((unit != null) && (!unit.equals(other.unit))) ||
				((yunit == null && other.yunit != null) ||
						((yunit != null) && !yunit.equals(other.yunit)))) ||
				((zunit == null && other.zunit != null) ||
						((zunit != null) && (!zunit.equals(other.zunit)))) ||
				((units == null && other.units != null) ||
						((units != null) && (!units.equals(other.units)))) ||
				((valueUnit == null && other.valueUnit != null) ||
						((valueUnit != null) && (!valueUnit.equals(other.valueUnit)))) ||
				((timeUnit == null && other.timeUnit != null) ||
						((timeUnit != null) && (!timeUnit.equals(other.timeUnit)))) ||
				(function != other.function) ||
				((cTable == null && other.cTable != null) ||
						((cTable != null) && (!cTable.equals(other.cTable)))) ||
				(invertedLut != other.invertedLut) ||
				(bitDepth != other.bitDepth) ||
				(zeroClip != other.zeroClip) ||
				(invertY != other.invertY)
				)
			return false;
		
		return true;
	}
// BDZ - END ADDITIONS
}

