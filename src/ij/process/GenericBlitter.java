package ij.process;

import java.awt.Color;

import mpicbg.imglib.type.numeric.RealType;

// TODO - purposely not extending Blitter as it uses ImageProcessors rather than ImgLibProcessors: rethink?
public class GenericBlitter<T extends RealType<T>>
{
	ImgLibProcessor<T> ip;
	double transparentColor;
	
	GenericBlitter(ImgLibProcessor<T> ip)
	{
		this.ip = ip;
		if (TypeManager.isIntegralType(this.ip.getType()))
			this.transparentColor = this.ip.getMaxAllowedValue();
	}
	
	public void copyBits(ImgLibProcessor<T> other, int xloc, int yloc, int mode)
	{
		BlitterOperation<T> blitOp = new BlitterOperation<T>(this.ip, other, xloc, yloc, mode, this.transparentColor);
		
		//if (mode == Blitter.DIVIDE)
		//	System.out.println("Here is my breakpoint anchor");
			
		Operation.apply(blitOp);
	}
	
	public void setTransparentColor(Color color)
	{
		if (TypeManager.isIntegralType(this.ip.getType()))
			this.transparentColor = this.ip.getBestIndex(color);
	}
	
}

