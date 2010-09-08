package ij.process;

import java.awt.Color;

import mpicbg.imglib.type.numeric.RealType;

public class CopyInput1TransparentBinaryFunction implements BinaryFunction {

	private static final double TOL = 0.00000001;
	
	private ImageProcessor ip;
	private boolean dataIsIntegral;
	private double transparentColor;
	
	public CopyInput1TransparentBinaryFunction(ImageProcessor ip, boolean isIntegral, double transparentColor)
	{
		this.ip = ip;
		this.dataIsIntegral = isIntegral;
		this.transparentColor = transparentColor;
	}
	
	@Override
	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		if (this.dataIsIntegral)
			if ( Math.abs( input1.getRealDouble() - this.transparentColor ) > TOL )
				result.setReal( input1.getRealDouble() );
	}
	
	public void setTransparentColor(Color color)
	{
		if (this.dataIsIntegral)
			this.transparentColor = this.ip.getBestIndex(color);
	}
}
