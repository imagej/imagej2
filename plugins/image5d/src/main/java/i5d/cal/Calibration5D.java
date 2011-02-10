package i5d.cal;
import i5d.Image5D;
import ij.measure.*;
/*
 * Created on 10.04.2005
 */

/** Extension of the Calibration class to 5 Dimensions.
 * 
 * @author Joachim
 */
public class Calibration5D extends Calibration {

	protected int nDimensions = 5;
	protected String[] dimensionLabels;
	/**
	 * @param imp
	 */
	public Calibration5D(Image5D imp) {
		super(imp);
		
		dimensionLabels = new String[nDimensions];
		dimensionLabels[0] = "x";
		dimensionLabels[1] = "y";
		if(nDimensions>=2)
			dimensionLabels[2] = "ch";
		if(nDimensions>=3)
			dimensionLabels[3] = "z";
		if(nDimensions>=4)
			dimensionLabels[4] = "t";
	}

	/**
	 * 
	 */
	public Calibration5D() {
		this(null);
		// TODO Auto-generated constructor stub
	}

	public String getDimensionLabel(int dimension) {
		if (dimension<0 || dimension >= nDimensions)
			throw new IllegalArgumentException("Invalid Dimension: "+dimension);
		return dimensionLabels[dimension];
	}
	
	public Calibration copy() {
		Calibration5D copy = new Calibration5D();
		copy.pixelWidth = pixelWidth;
		copy.pixelHeight = pixelHeight;
		copy.pixelDepth = pixelDepth;
		copy.frameInterval = frameInterval;
		copy.xOrigin = xOrigin;
		copy.yOrigin = yOrigin;
		copy.zOrigin = zOrigin;
		copy.info = info;
		copy.setUnit(getUnit());
//		copy.setUnits(getUnits());
		copy.setValueUnit(getValueUnit());
// TODO: correct copying of calibration function, cal functs have to be one for each color dimension, anyway
//		copy.function = function;
//		copy.coefficients = coefficients;
//		copy.cTable = cTable;
//		copy.invertedLut = invertedLut;
//		copy.bitDepth = bitDepth;
//		copy.zeroClip = zeroClip;
		
		copy.nDimensions = nDimensions;
		copy.dimensionLabels = new String[nDimensions];
		for (int i=0; i<nDimensions; ++i) {
			dimensionLabels[i] = getDimensionLabel(i);
		}
		return copy;
	}
}
