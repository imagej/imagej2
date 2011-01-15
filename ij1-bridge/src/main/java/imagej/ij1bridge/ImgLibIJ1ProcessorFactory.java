package imagej.ij1bridge;

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
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import imagej.ij1bridge.process.ImgLibProcessor;
import imagej.imglib.TypeManager;
import imagej.imglib.dataset.LegacyImgLibDataset;

public class ImgLibIJ1ProcessorFactory implements ProcessorFactory
{
	private LegacyImgLibDataset dataset;
	private boolean strictlyCompatible;
	private RealType<?> realType;

	public ImgLibIJ1ProcessorFactory(LegacyImgLibDataset dataset, boolean strictlyCompatible)
	{
		this.dataset = dataset;
		this.strictlyCompatible = strictlyCompatible;
		this.realType = TypeManager.getRealType(dataset.getType());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ImageProcessor makeProcessor(int[] planePos)
	{
		Object plane = this.dataset.getSubset(planePos).getData();
		
		if (plane == null)
			throw new IllegalArgumentException("IJ1 ImageProcessors require a plane of data to work with. Given null plane.");
		
		Image<?> image = this.dataset.getImage();
		
		int[] dimensions = this.dataset.getDimensions();
		
		int xDim = dimensions[0];
		
		int yDim = dimensions[1];
		
		if (realType instanceof BitType)
			if ( ! this.strictlyCompatible )
				return new ImgLibProcessor<BitType>((Image<BitType>)image, planePos);

		if (realType instanceof ByteType)
			if ( ! this.strictlyCompatible )
				return new ImgLibProcessor<ByteType>((Image<ByteType>)image, planePos);

		if (realType instanceof UnsignedByteType)
			return new ByteProcessor(xDim, yDim, (byte[])plane, null);

		if (realType instanceof Unsigned12BitType)
			if ( ! this.strictlyCompatible )
				return new ImgLibProcessor<Unsigned12BitType>((Image<Unsigned12BitType>)image, planePos);

		if (realType instanceof ShortType)
			if ( ! this.strictlyCompatible )
				return new ImgLibProcessor<ShortType>((Image<ShortType>)image, planePos);

		if (realType instanceof UnsignedShortType )
			return new ShortProcessor(xDim, yDim, (short[])plane, null);

		if (realType instanceof IntType)
			if ( ! this.strictlyCompatible )
				return new ImgLibProcessor<IntType>((Image<IntType>)image, planePos);

		if (realType instanceof UnsignedIntType)
			if ( ! this.strictlyCompatible )
				return new ImgLibProcessor<UnsignedIntType>((Image<UnsignedIntType>)image, planePos);

		if (realType instanceof FloatType )
			return new FloatProcessor(xDim, yDim, (float[])plane, null);

		if (realType instanceof LongType)
			if ( ! this.strictlyCompatible )
				return new ImgLibProcessor<LongType>((Image<LongType>)image, planePos);

		if (realType instanceof DoubleType)
			if ( ! this.strictlyCompatible )
				return new ImgLibProcessor<DoubleType>((Image<DoubleType>)image, planePos);

		throw new IllegalArgumentException("cannot find satisfactory processor type for data type "+this.dataset.getType().getName()+
											" (require IJ1 processors only = "+this.strictlyCompatible+")");	
	}
}
