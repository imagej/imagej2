package imagej.process;

import imagej.process.function.binary.BinaryFunction;
import imagej.process.operation.BinaryTransformOperation;
import mpicbg.imglib.type.numeric.RealType;

// TODO - purposely not extending Blitter as it uses ImageProcessors rather than ImgLibProcessors: rethink?
public class GenericBlitter<T extends RealType<T>>
{
	private ImgLibProcessor<T> ip;
	
	public GenericBlitter(ImgLibProcessor<T> ip)
	{
		this.ip = ip;
	}
	
	public void copyBits(ImgLibProcessor<T> other, int xloc, int yloc, BinaryFunction function)
	{
		BinaryTransformOperation<T> blitterOp =
			new BinaryTransformOperation<T>(
					ip.getImage(),
					Index.create(xloc, yloc, ip.getPlanePosition()),
					Span.singlePlane(other.getWidth(), other.getHeight(), ip.getImage().getNumDimensions()),
					other.getImage(),
					Index.create(2),
					Span.singlePlane(other.getWidth(), other.getHeight(), 2),
					function);
		
		blitterOp.addObserver(new ProgressTracker(ip, other.getTotalSamples(), 20*ip.getWidth()));
		
		blitterOp.execute();
	}
}
