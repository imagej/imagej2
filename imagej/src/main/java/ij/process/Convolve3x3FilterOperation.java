package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class Convolve3x3FilterOperation<K extends RealType<K>> extends Filter3x3Operation<K>
{
	private double[] k;
	private double[] v;
	private double scale;
	
	public Convolve3x3FilterOperation(Image<K> image, int[] origin, int[] span, ImgLibProcessor<K> ip, int[] kernel)
	{
		super(image, origin, span, ip, new double[9]);

		this.k = new double[kernel.length];
		this.v = getValueArray();
		for (int i = 0; i < this.k.length; i++)
			this.k[i] = kernel[i];

		this.scale = 0;
		for (int i = 0; i < 9; i++)
			this.scale += kernel[i];
		if (this.scale == 0)
			this.scale = 1;
	}

	protected double calcSampleValue(RealType<?> sample)
	{
		double sum = k[0]*v[0] + k[1]*v[1] + k[2]*v[2]
		             + k[3]*v[3] + k[4]*v[4] + k[5]*v[5]
		             + k[6]*v[6] + k[7]*v[7] + k[8]*v[8];
		
		sum /= this.scale;

		if (this.dataIsIntegral)
		{
			sum = Math.round(sum);
			sum = TypeManager.boundValueToType(sample, sum);
		}

		return sum;
	}
	
}

