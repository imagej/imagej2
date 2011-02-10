package imagej.ij1bridge.process.operation;

import imagej.ij1bridge.process.ImgLibProcessor;
import imagej.imglib.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class BlurFilterOperation<K extends RealType<K>> extends Filter3x3Operation<K>
{
	private RealType<?> dataType;
	private boolean dataIsIntegral;
	
	public BlurFilterOperation(ImgLibProcessor<K> ip, int[] origin, int[] span)
	{
		super(ip, origin, span);
		this.dataType = ip.getType();
		this.dataIsIntegral = TypeManager.isIntegralType(this.dataType);
	}

	protected double calcSampleValue(final double[] neighborhood)
	{
		double val = 0;
		
		for (int i = 0; i < 9; i++)
			val += neighborhood[i];
		
		val /= 9.0;
		
		if (this.dataIsIntegral)
		{
			val = Math.round(val);
			val = TypeManager.boundValueToType(this.dataType, val);
		}
		
		return val;
	}
}

