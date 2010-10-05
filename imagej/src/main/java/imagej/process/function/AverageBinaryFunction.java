package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;
import imagej.process.TypeManager;

public class AverageBinaryFunction implements BinaryFunction {

	private boolean dataIsIntegral;
	
	public AverageBinaryFunction(RealType<?> targetType)
	{
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}

	public double compute(double input1, double input2)
	{
		if (this.dataIsIntegral)
			return ( ((long)input1 + (long)input2) / 2 );
		else
			return ( (input1 + input2) / 2.0 );
	}

}
