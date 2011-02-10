package i5d.cal;
import ij.measure.*;

/*
 * Created on 15.10.2005
 */

/** "Struct" for storing the calibration function and parameters of a color channel
 * @author Joachim Walter
 *
 */
public class ChannelCalibration {

    private String label;
    
    // Fields for calibration function. Documented in ij.measure.Calibration.
    private String valueUnit = "Gray Value";
    private int function = Calibration.NONE;
    private double[] coefficients;
    private boolean zeroClip;
    
    
    public ChannelCalibration() {
        label = "";
        disableDensityCalibration();
    }
    

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    
//  Functions to handle density calibration follow.
    public void setFunction(int function, double[] coefficients, String unit, boolean zeroClip) {
        if (function==Calibration.NONE)
            {disableDensityCalibration(); return;}
        if (coefficients==null && function>=Calibration.STRAIGHT_LINE && function<=Calibration.LOG2)
            return;
        this.function = function;
        this.coefficients = coefficients;
        this.zeroClip = zeroClip;
        if (unit!=null)
            valueUnit = unit;
    }
    
    public void disableDensityCalibration() {
        function = Calibration.NONE;
        coefficients = null;
        valueUnit = "Gray Value";
        zeroClip = false;
    }   
    
    /** Returns the calibration function ID. */
    public int getFunction() {
        return function;
    }
    /** Returns the calibration function coefficients. */
    public double[] getCoefficients() {
        return coefficients;
    }
    /** Returns the value unit. */
    public String getValueUnit() {
        return valueUnit;
    }
    /** Returns true if zero clipping is enabled. */
    public boolean isZeroClip() {
        return zeroClip;
    }  
    
    /** Returns a copy of the ChannelCalibration object.
     * The copy is a deep copy.
     * @return
     */
    public ChannelCalibration copy() {
        ChannelCalibration cc = new ChannelCalibration();
        cc.setLabel(getLabel());
        cc.setFunction(getFunction(), getCoefficients(), getValueUnit(), isZeroClip());          
        return cc;
    }    
}
