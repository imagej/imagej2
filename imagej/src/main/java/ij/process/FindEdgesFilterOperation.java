package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class FindEdgesFilterOperation<K extends RealType<K>> extends Filter3x3Operation<K>
{
	private boolean dataIsIntegral;
	private RealType<?> dataType;
	
	public FindEdgesFilterOperation(Image<K> image, int[] origin, int[] span, ImgLibProcessor<K> ip)
	{
		super(image, origin, span, ip);
		this.dataIsIntegral = TypeManager.isIntegralType(ImageUtils.getType(image));
		this.dataType = ip.getType();
	}

	protected double calcSampleValue(double[] neighborhood)
	{
		double sum1 = neighborhood[0] + 2*neighborhood[1] + neighborhood[2]
		               - neighborhood[6] - 2*neighborhood[7] - neighborhood[8]
		                                                                    ;
		double sum2 = neighborhood[0] + 2*neighborhood[3] + neighborhood[6]
		               - neighborhood[2] - 2*neighborhood[5] - neighborhood[8];
		double value;
		
		if (this.dataIsIntegral)
		{
			value = Math.sqrt(sum1*sum1 + sum2*sum2) + 0.5;
			value = Math.floor(value);
			value = TypeManager.boundValueToType(this.dataType, value);
		}
		else
			value = Math.sqrt(sum1*sum1 + sum2*sum2);

		return value;
	}
}

