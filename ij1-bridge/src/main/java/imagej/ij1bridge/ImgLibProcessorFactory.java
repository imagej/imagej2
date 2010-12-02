package imagej.ij1bridge;

import ij.process.ImageProcessor;
import imagej.UserType;
import imagej.ij1bridge.process.ImgLibProcessor;
import imagej.imglib.TypeManager;
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
	private UserType type;
	
	public ImgLibProcessorFactory(Image<?> image)
	{
		this.image = image;
		RealType<?> realType = ImageUtils.getType(this.image);
		this.type = TypeManager.getUserType(realType);
	}
	
	@SuppressWarnings("unchecked")
	public ImageProcessor makeProcessor(int[] planePos)
	{
		switch (this.type)
		{
		case BIT:
			return new ImgLibProcessor<BitType>((Image<BitType>)image, planePos);
		case BYTE:
			return new ImgLibProcessor<ByteType>((Image<ByteType>)image, planePos);
		case UBYTE:
			return new ImgLibProcessor<UnsignedByteType>((Image<UnsignedByteType>)image, planePos);
		case UINT12:
			return new ImgLibProcessor<Unsigned12BitType>((Image<Unsigned12BitType>)image, planePos);
		case SHORT:
			return new ImgLibProcessor<ShortType>((Image<ShortType>)image, planePos);
		case USHORT:
			return new ImgLibProcessor<UnsignedShortType>((Image<UnsignedShortType>)image, planePos);
		case INT:
			return new ImgLibProcessor<IntType>((Image<IntType>)image, planePos);
		case UINT:
			return new ImgLibProcessor<UnsignedIntType>((Image<UnsignedIntType>)image, planePos);
		case FLOAT:
			return new ImgLibProcessor<FloatType>((Image<FloatType>)image, planePos);
		case LONG:
			return new ImgLibProcessor<LongType>((Image<LongType>)image, planePos);
		case DOUBLE:
			return new ImgLibProcessor<DoubleType>((Image<DoubleType>)image, planePos);
		default:
			throw new IllegalStateException("Unknown sample type "+this.type);	
		}
	}
}
