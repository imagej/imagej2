package imagej.process.function.binary;

import ij.process.ImageProcessor;
import imagej.process.TypeManager;

import java.awt.Color;

import mpicbg.imglib.type.numeric.RealType;

public class CopyInput2TransparentBinaryFunction implements BinaryFunction {

	private static final double TOL = 0.00000001;
	
	private ImageProcessor ip;
	private boolean dataIsIntegral;
	private double transparentColor;
	
	public CopyInput2TransparentBinaryFunction(RealType<?> targetType, ImageProcessor ip)
	{
		this.ip = ip;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
		this.transparentColor = targetType.getMaxValue();
	}
	
	public double compute(double input1, double input2)
	{
		double value = input1;
		if (this.dataIsIntegral)
			if ( Math.abs( input2 - this.transparentColor ) > TOL )
				value = input2;
		return value;
	}
	
	public void setTransparentColor(Color color)
	{
		if (this.dataIsIntegral)
			this.transparentColor = this.ip.getBestIndex(color);
	}
}
