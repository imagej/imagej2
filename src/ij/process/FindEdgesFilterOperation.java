package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class FindEdgesFilterOperation<K extends RealType<K>> extends Filter3x3Operation<K>
{
	public FindEdgesFilterOperation(Image<K> image, int[] origin, int[] span, ImgLibProcessor<K> ip)
	{
		super(image, origin, span, ip, new double[9]);
	}

	protected double calcSampleValue(RealType<?> sample)
	{
		double sum1 = (v[0] + 2*v[1] + v[2] - v[6] - 2*v[7] - v[8]);
		double sum2 = (v[0] + 2*v[3] + v[6] - v[2] - 2*v[5] - v[8]);

		double value;
		
		if (this.dataIsIntegral)
		{
			value = Math.sqrt(sum1*sum1 + sum2*sum2) + 0.5;
			value = Math.floor(value);
		}
		else
			value = Math.sqrt(sum1*sum1 + sum2*sum2);

		return value;
	}
}

