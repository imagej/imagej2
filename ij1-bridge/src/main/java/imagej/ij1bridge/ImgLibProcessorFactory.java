package imagej.ij1bridge;

import ij.process.ImageProcessor;
import imagej.ij1bridge.process.ImgLibProcessor;
import imagej.imglib.process.ImageUtils;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.logic.BitType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

public class ImgLibProcessorFactory implements ProcessorFactory
{
	private Image<?> image;
	private RealType<?> realType;
	
	public ImgLibProcessorFactory(Image<?> image)
	{
		this.image = image;
		this.realType = ImageUtils.getType(this.image);
	}
	
	@SuppressWarnings("unchecked")
	public ImageProcessor makeProcessor(int[] planePos)
	{
		if (realType instanceof BitType)
			return new ImgLibProcessor<BitType>((Image<BitType>)image, planePos);

		if (realType instanceof ByteType)
			return new ImgLibProcessor<ByteType>((Image<ByteType>)image, planePos);

		if (realType instanceof UnsignedByteType)
			return new ImgLibProcessor<UnsignedByteType>((Image<UnsignedByteType>)image, planePos);

		if (realType instanceof Unsigned12BitType)
			return new ImgLibProcessor<Unsigned12BitType>((Image<Unsigned12BitType>)image, planePos);

		if (realType instanceof ShortType)
			return new ImgLibProcessor<ShortType>((Image<ShortType>)image, planePos);

		if (realType instanceof UnsignedShortType)
			return new ImgLibProcessor<UnsignedShortType>((Image<UnsignedShortType>)image, planePos);

		if (realType instanceof IntType)
			return new ImgLibProcessor<IntType>((Image<IntType>)image, planePos);

		if (realType instanceof UnsignedIntType)
			return new ImgLibProcessor<UnsignedIntType>((Image<UnsignedIntType>)image, planePos);

		if (realType instanceof FloatType)
			return new ImgLibProcessor<FloatType>((Image<FloatType>)image, planePos);

		if (realType instanceof LongType)
			return new ImgLibProcessor<LongType>((Image<LongType>)image, planePos);

		if (realType instanceof DoubleType)
			return new ImgLibProcessor<DoubleType>((Image<DoubleType>)image, planePos);

		throw new IllegalStateException("Unknown sample type "+this.realType.getClass());	
	}
}
