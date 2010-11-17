package imagej2.function.nary;

import imagej2.function.NAryFunction;

public class AverageNAryFunction implements NAryFunction {

	private int numValues;
	
	/** create an AverageNAryFunction. Must specify how many values it can handle at a time beforehand. */
	public AverageNAryFunction(int numValues)
	{
		this.numValues = numValues;
	}
	
	/** the number of input parameters this function can handle as input */
	public int getValueCount()
	{
		return this.numValues;
	}

	public double compute(double[] inputs)
	{
		double total = 0;
		
		int inputLen = inputs.length;
		
		for (int i = 0; i < inputLen; i++)
			total += inputs[i];
		
		if (inputLen > 1)
			total /= inputLen;
		
		return total;
	}

}
