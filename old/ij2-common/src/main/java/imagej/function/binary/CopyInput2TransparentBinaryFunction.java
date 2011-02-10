package imagej.function.binary;

import imagej.function.BinaryFunction;

public class CopyInput2TransparentBinaryFunction implements BinaryFunction {

	private static final double TOL = 0.00000001;
	
	private boolean dataIsIntegral;
	private double transparentValue;
	
	public CopyInput2TransparentBinaryFunction(boolean isIntegral, double maxValue)
	{
		this.dataIsIntegral = isIntegral;
		this.transparentValue = maxValue;
	}
	
	public double compute(double input1, double input2)
	{
		double value = input1;
		if (this.dataIsIntegral)
			if ( Math.abs( input2 - this.transparentValue ) > TOL )
				value = input2;
		return value;
	}
	
	public void setTransparentValue(double value)
	{
		if (this.dataIsIntegral)
			this.transparentValue = value;
	}
}
