package imagej2.function.binary;

import imagej2.function.BinaryFunction;

public class DivideBinaryFunction implements BinaryFunction {

	private boolean useDBZValue;
	private float divideByZeroValue;
	private boolean dataIsIntegral;
	private double max;
	
	public DivideBinaryFunction(boolean isIntegral, double maxValue, float divideByZeroValue)
	{
		this.dataIsIntegral = isIntegral;
		this.max = maxValue;
		this.useDBZValue = !Float.isInfinite(divideByZeroValue);
		this.divideByZeroValue = divideByZeroValue;
	}

	public double compute(double input1, double input2)
	{
		double value;
		double denom = input2;
		if (denom == 0)
		{
			if (this.dataIsIntegral)
				value = this.max;
			else // float
			{
				if (this.useDBZValue)
					value = divideByZeroValue;
				else
					value = input1 / denom;  // just do the division!!! thats what IJ does.
			}
		}
		else
			value = input1 / denom;
		
		if (this.dataIsIntegral)
			value = Math.floor(value);
		
		return value;
	}

}
