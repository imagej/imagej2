package ij.process;

import java.util.Random;

import mpicbg.imglib.type.numeric.RealType;

public class AddNoiseUnaryFunction implements UnaryFunction
{
	RealType<?> type;
	boolean dataIsIntegral;
	double range;
	Random rnd;
	
	public AddNoiseUnaryFunction(RealType<?> type, double range)
	{
		this.type = type;
		this.range = range;
		this.dataIsIntegral = TypeManager.isIntegralType(type);
		rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());  // TODO - this line not present in original code (by design???)
	}
	
	public void compute(RealType<?> result, RealType<?> input)
	{
		double currVal = input.getRealDouble();
		double newVal, ran;
		boolean inRange = false;
		do {
			ran = rnd.nextGaussian() * range;
			if (this.dataIsIntegral)
				ran = Math.round(ran);
			newVal = currVal + ran;
			inRange = TypeManager.validValue(this.type, newVal);
			if (inRange)
				result.setReal(newVal);
		} while (!inRange);
	}
}
