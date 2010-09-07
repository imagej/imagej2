package ij.process;

import mpicbg.imglib.type.numeric.RealType;

// TODO - purposely not extending Blitter as it uses ImageProcessors rather than ImgLibProcessors: rethink?
public class GenericBlitter<T extends RealType<T>>
{
	ImgLibProcessor<T> ip;
	
	GenericBlitter(ImgLibProcessor<T> ip)
	{
		this.ip = ip;
	}
	
	public void copyBits(ImgLibProcessor<T> other, int xloc, int yloc, BinaryFunction function)
	{
		BlitterOperation<T> blitOp = new BlitterOperation<T>(this.ip, other, xloc, yloc, function);
		
		Operation.apply(blitOp);
	}
}
