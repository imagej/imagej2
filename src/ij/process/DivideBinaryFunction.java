package ij.process;

import ij.Prefs;
import mpicbg.imglib.type.numeric.RealType;

public class DivideBinaryFunction implements BinaryFunction {

	private static float divideByZeroValue;
	
	static {
		divideByZeroValue = (float)Prefs.getDouble(Prefs.DIV_BY_ZERO_VALUE, Float.POSITIVE_INFINITY);
		if (divideByZeroValue==Float.MAX_VALUE)
			divideByZeroValue = Float.POSITIVE_INFINITY;
	}

	private boolean useDBZValue;
	private boolean dataIsIntegral;
	private double max;
	
	DivideBinaryFunction(boolean isIntegral, double max)
	{
		this.dataIsIntegral = isIntegral;
		this.max = max;
		this.useDBZValue = !Float.isInfinite(divideByZeroValue);
	}

	@Override
	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		double value = input1.getRealDouble();
		if (value == 0)
		{
			if (this.dataIsIntegral)
				value = this.max;
			else // float
			{
				if (this.useDBZValue)
					value = divideByZeroValue;
				else
					value = input2.getRealDouble() / value;  // just do the division!!! thats what IJ does.
			}
		}
		else
			value = input2.getRealDouble() / value;
		if (this.dataIsIntegral)
			value = Math.floor(value);
		result.setReal( value );
	}

}
