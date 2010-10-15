package imagej.process.function.unary;

import imagej.process.TypeManager;

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
	
	public double compute(double input)
	{
		double result, ran;
		boolean inRange = false;
		do {
			ran = rnd.nextGaussian() * range;
			if (this.dataIsIntegral)
				ran = Math.round(ran);
			result = input + ran;
			inRange = TypeManager.validValue(this.type, result);
		} while (!inRange);
		return result;
	}
}
