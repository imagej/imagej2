package imagej.process;

import imagej.process.function.binary.BinaryFunction;
import imagej.process.operation.BinaryTransformOperation;
import mpicbg.imglib.type.numeric.RealType;

// TODO - eliminate as a class and do inline in ImgLibProcessor?

/** GenericBlitter is used by ImgLibProcessor::copyBits()
 * 
 * @author bdezonia
 *
 */
public class GenericBlitter<T extends RealType<T>>
{
	private ImgLibProcessor<T> ip;
	
	/** GenericBlitter constructor
	 * @param ip - an ImgLibProcessor that will be changed by a copyBits() call
	 */
	public GenericBlitter(ImgLibProcessor<T> ip)
	{
		this.ip = ip;
	}
	
	/** sets the pixels in the target ip using another ImgLibProcessor and a BinaryFunction. Note that the "other" image
	 * is used to determine the width and height of area to be modified in the target ip. The "other" image is two
	 * dimensional.
	 * 
	 * @param other - the other ImgLibProcessor that contains the data that will be combined with that of the target ip
	 * @param xloc - the x location within the the target ip where the combination should begin 
	 * @param yloc - the y location within the the target ip where the combination should begin
	 * @param function - the BinaryFunction that determines how the values of the two ImageProcessors are combined
	 */
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
		
		blitterOp.addObserver(new ProgressTracker(ip, other.getTotalSamples(), 20L*ip.getWidth()));
		
		blitterOp.execute();
	}
}
