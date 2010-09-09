package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class BlurFilterOperation<K extends RealType<K>> extends Filter3x3Operation<K>
{
	public BlurFilterOperation(Image<K> image, int[] origin, int[] span, ImgLibProcessor<K> ip)
	{
		super(image, origin, span, ip, new double[9]);
	}

	public double calcSampleValue(RealType<?> sample)
	{
		double val = (v[0]+v[1]+v[2]+v[3]+v[4]+v[5]+v[6]+v[7]+v[8])/9.0;
		
		if (this.dataIsIntegral)
		{
			val = Math.round(val);
			val = TypeManager.boundValueToType(sample, val);
		}
		
		return val;
	}
}

