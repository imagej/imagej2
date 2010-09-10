package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class BlurFilterOperation<K extends RealType<K>> extends Filter3x3Operation<K>
{
	private RealType<?> dataType;
	private boolean dataIsIntegral;
	
	public BlurFilterOperation(Image<K> image, int[] origin, int[] span, ImgLibProcessor<K> ip)
	{
		super(image, origin, span, ip);
		this.dataType = ip.getType();
		this.dataIsIntegral = TypeManager.isIntegralType(this.dataType);
	}

	public double calcSampleValue(double[] neighborhood)
	{
		double val = 0;
		
		for (int i = 0; i < 9; i++)
			val += neighborhood[i];
		
		val /= 9;
		
		if (this.dataIsIntegral)
		{
			val = Math.round(val);
			val = TypeManager.boundValueToType(this.dataType, val);
		}
		
		return val;
	}
}

