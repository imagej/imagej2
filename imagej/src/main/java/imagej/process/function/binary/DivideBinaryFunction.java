package imagej.process.function.binary;

import mpicbg.imglib.type.numeric.RealType;
import ij.Prefs;
import imagej.process.TypeManager;

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
	
	public DivideBinaryFunction(RealType<?> targetType)
	{
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
		this.max = targetType.getMaxValue();
		this.useDBZValue = !Float.isInfinite(divideByZeroValue);
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
