package imagej.process.operation;

import ij.process.ImgLibProcessor;
import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class Convolve3x3FilterOperation<K extends RealType<K>> extends Filter3x3Operation<K>
{
	private boolean dataIsIntegral;
	private RealType<?> dataType;
	private double[] k;
	private double scale;
	
	public Convolve3x3FilterOperation(ImgLibProcessor<K> ip, int[] origin, int[] span, int[] kernel)
	{
		super(ip, origin, span);

		this.dataType = ip.getType();
		this.dataIsIntegral = TypeManager.isIntegralType(this.dataType);

		this.k = new double[kernel.length];
		for (int i = 0; i < this.k.length; i++)
			this.k[i] = kernel[i];

		this.scale = 0;
		for (int i = 0; i < 9; i++)
			this.scale += kernel[i];
		if (this.scale == 0)
			this.scale = 1;
	}

	protected double calcSampleValue(final double[] neighborhood)
	{
		double sum = k[0]*neighborhood[0] + k[1]*neighborhood[1] + k[2]*neighborhood[2]
		             + k[3]*neighborhood[3] + k[4]*neighborhood[4] + k[5]*neighborhood[5]
		             + k[6]*neighborhood[6] + k[7]*neighborhood[7] + k[8]*neighborhood[8];
		
		sum /= this.scale;

		if (this.dataIsIntegral)
		{
			sum = Math.round(sum);
			sum = TypeManager.boundValueToType(this.dataType, sum);
		}

		return sum;
	}
	
}

