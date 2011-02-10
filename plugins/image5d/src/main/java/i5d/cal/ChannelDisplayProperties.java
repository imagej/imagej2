package i5d.cal;
import java.awt.*;
import java.awt.image.*;

import ij.process.*;

/*
 * Created on 08.04.2005
 */

/** "Struct" for storing the display properties of a color channel (e.g. min, max )
 * @author Joachim Walter
 *
 */
public class ChannelDisplayProperties {
	private ColorModel colorModel;
	private double minValue;
	private double maxValue;
	private double minThreshold;
	private double maxThreshold;
	private int lutUpdateMode;
	private boolean displayedGray;
	private boolean displayedInOverlay;
//	private String label;
//    
//    // Fields for calibration function. Documented in ij.measure.Calibration.
//    private String valueUnit = "Gray Value";
//    private int function = Calibration.NONE;
//    private double[] coefficients;
//    private boolean zeroClip;
	
	public ChannelDisplayProperties() {
	
		byte[] lut = new byte[256];
		for (int i=0; i<256; i++) {
			lut[i] = (byte)i;
		}		
		colorModel = new IndexColorModel(8, 256, lut, lut, lut);
		minValue = 0d;
		maxValue = 255d;
		minThreshold = ImageProcessor.NO_THRESHOLD;
		maxThreshold = ImageProcessor.NO_THRESHOLD;
		displayedGray = false;
		displayedInOverlay = true;
		lutUpdateMode = ImageProcessor.RED_LUT;
//		label = "";
//        
//        disableDensityCalibration();
	}
	
	
	public ColorModel getColorModel() {
		return colorModel;
	}
	public void setColorModel(ColorModel colorModel) {
		this.colorModel = colorModel;
	}
	public double getMaxThreshold() {
		return maxThreshold;
	}
	public void setMaxThreshold(double maxThreshold) {
		this.maxThreshold = maxThreshold;
	}
	public double getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}
	public double getMinThreshold() {
		return minThreshold;
	}
	public void setMinThreshold(double minThreshold) {
		this.minThreshold = minThreshold;
	}
	public double getMinValue() {
		return minValue;
	}
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}
//	public String getLabel() {
//		return label;
//	}
//	public void setLabel(String label) {
//		this.label = label;
//	}
	public boolean isDisplayedGray() {
		return displayedGray;
	}
	public void setDisplayedGray(boolean displayGray) {
		this.displayedGray = displayGray;
	}
	public boolean isDisplayedInOverlay() {
		return displayedInOverlay;
	}
	public void setDisplayedInOverlay(boolean displayedInOverlay) {
		this.displayedInOverlay = displayedInOverlay;
	}	
    public int getLutUpdateMode() {
        return lutUpdateMode;
    }
    public void setLutUpdateMode(int lutUpdateMode) {
        this.lutUpdateMode = lutUpdateMode;
    }
	

	public static IndexColorModel createModelFromColor(Color color) {
		byte[] rLut = new byte[256];
		byte[] gLut = new byte[256];
		byte[] bLut = new byte[256];
		
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();

		double rIncr = ((double)red)/255d;
		double gIncr = ((double)green)/255d;
		double bIncr = ((double)blue)/255d;
		
		for (int i=0; i<256; ++i) {
			rLut[i] = (byte) (i*rIncr);
			gLut[i] = (byte) (i*gIncr);
			bLut[i] = (byte) (i*bIncr);
		}
		
		return new IndexColorModel(8, 256, rLut, gLut, bLut);
	}
 
//// Functions to handle density calibration follow.
//    public void setFunction(int function, double[] coefficients, String unit, boolean zeroClip) {
//        if (function==Calibration.NONE)
//            {disableDensityCalibration(); return;}
//        if (coefficients==null && function>=Calibration.STRAIGHT_LINE && function<=Calibration.LOG2)
//            return;
//        this.function = function;
//        this.coefficients = coefficients;
//        this.zeroClip = zeroClip;
//        if (unit!=null)
//            valueUnit = unit;
//    }
//    
//    public void disableDensityCalibration() {
//        function = Calibration.NONE;
//        coefficients = null;
//        valueUnit = "Gray Value";
//        zeroClip = false;
//    }   
//    
//    /** Returns the calibration function ID. */
//    public int getFunction() {
//        return function;
//    }
//    /** Returns the calibration function coefficients. */
//    public double[] getCoefficients() {
//        return coefficients;
//    }
//    /** Returns the value unit. */
//    public String getValueUnit() {
//        return valueUnit;
//    }
//    /** Returns true if zero clipping is enabled. */
//    public boolean isZeroClip() {
//        return zeroClip;
//    }   
    
    /** Returns a copy of the colorChannelProperties object.
     * The copy is a deep copy except for the colorModel, where only
     * the reference is copied. 
     * @return
     */
    public ChannelDisplayProperties copy() {
        ChannelDisplayProperties ccp = new ChannelDisplayProperties();

        ccp.setColorModel(getColorModel());
        ccp.setMinValue(getMinValue());
        ccp.setMaxValue(getMaxValue());
        ccp.setMinThreshold(getMinThreshold());
        ccp.setMaxThreshold(getMaxThreshold());
        ccp.setLutUpdateMode(getLutUpdateMode());
        ccp.setDisplayedGray(isDisplayedGray());
        ccp.setDisplayedInOverlay(isDisplayedInOverlay());
           
        return ccp;
    }
    
}
