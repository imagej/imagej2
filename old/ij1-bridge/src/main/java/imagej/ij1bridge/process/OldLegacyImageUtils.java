package imagej.ij1bridge.process;

import ij.ImagePlus;
import ij.io.FileInfo;
import imagej.data.Type;
import imagej.data.Types;
import imagej.ij1bridge.BridgeStack;
import imagej.ij1bridge.IJ1ProcessorFactory;
import imagej.ij1bridge.ImgLibProcessorFactory;
import imagej.ij1bridge.ProcessorFactory;
import imagej.imglib.ImageUtils;
import imagej.imglib.TypeManager;
import imagej.imglib.dataset.LegacyImgLibDataset;
import imagej.imglib.process.OldImageUtils;

import java.io.File;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
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

// TODO
//   createImagePlus() calls imp.setDimensions(z,c,t). But we may have other dims too. Change when
//     we can call ImagePlus::setDimensions(int[] dims)
//   Split this class into a separate project, imglib-utils, to avoid ij dependencies with other project (e.g., bf-imglib).

/** this class designed to hold functionality that could be migrated to imglib */
public class OldLegacyImageUtils
{

	// ***************** public methods  **************************************************


	/** creates an ImgLibProcessor populated with given pixel data. Note that this method creates an imglib Image<?>
	 * that contains the pixel data and only the returned ImgLibProcessor has access to this Image<?>.
	 *
	 * @param width - desired width of image
	 * @param height - desired height of image
	 * @param pixels - pixel data in the form of a primitive array whose size is width*height
	 * @param type - the IJ value type of the input data (BYTE, USHORT, etc.)
	 */
	public static ImgLibProcessor<?> createProcessor(int width, int height, Object pixels, Type type)
	{
		Types.verifyCompatibility(type, pixels);
		
		PlanarContainerFactory containerFactory = new PlanarContainerFactory();

		int[] dimensions = new int[]{width, height, 1};

		ImgLibProcessor<?> proc = null;

		RealType<?> imglibType = TypeManager.getRealType(type);
		
		if (imglibType instanceof BitType)
		{
			Image<BitType> hatchedImage = ImageUtils.createImage(new BitType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<BitType>(hatchedImage, 0);
		}
		else if (imglibType instanceof ByteType)
		{
			Image<ByteType> hatchedImage = ImageUtils.createImage(new ByteType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<ByteType>(hatchedImage, 0);
		}
		else if (imglibType instanceof UnsignedByteType)
		{
			Image<UnsignedByteType> hatchedImage = ImageUtils.createImage(new UnsignedByteType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<UnsignedByteType>(hatchedImage, 0);
		}
		else if (imglibType instanceof Unsigned12BitType)
		{
			Image<Unsigned12BitType> hatchedImage = ImageUtils.createImage(new Unsigned12BitType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<Unsigned12BitType>(hatchedImage, 0);
		}
		else if (imglibType instanceof ShortType)
		{
			Image<ShortType> hatchedImage = ImageUtils.createImage(new ShortType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<ShortType>(hatchedImage, 0);
		}
		else if (imglibType instanceof UnsignedShortType)
		{
			Image<UnsignedShortType> hatchedImage = ImageUtils.createImage(new UnsignedShortType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<UnsignedShortType>(hatchedImage, 0);
		}
		else if (imglibType instanceof IntType)
		{
			Image<IntType> hatchedImage = ImageUtils.createImage(new IntType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<IntType>(hatchedImage, 0);
		}
		else if (imglibType instanceof UnsignedIntType)
		{
			Image<UnsignedIntType> hatchedImage = ImageUtils.createImage(new UnsignedIntType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<UnsignedIntType>(hatchedImage, 0);
		}
		else if (imglibType instanceof LongType)
		{
			Image<LongType> hatchedImage = ImageUtils.createImage(new LongType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<LongType>(hatchedImage, 0);
		}
		else if (imglibType instanceof FloatType)
		{
			Image<FloatType> hatchedImage = ImageUtils.createImage(new FloatType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<FloatType>(hatchedImage, 0);
		}
		else if (imglibType instanceof DoubleType)
		{
			Image<DoubleType> hatchedImage = ImageUtils.createImage(new DoubleType(), containerFactory, dimensions);
			proc = new ImgLibProcessor<DoubleType>(hatchedImage, 0);
		}
		else
			throw new IllegalArgumentException("unsupported type specified "+type.getName());

		proc.setPixels(pixels);

		return proc;
	}


	/** creates an ImagePlus from an imglib Image<?> */
	public static ImagePlus createImagePlus(final Image<?> img)
	{
		return createImagePlus(img, null);
	}

	/** creates an ImagePlus from an imglib Image<?> and a string.
	 * @param img - the imglib Image<?> that will back the ImagePlus
	 * @param id - a string representing either a filename or a URL (used to populate the ImagePlus' FileInfo
	 */
	public static ImagePlus createImagePlus(final Image<?> img, final String id)
	{
		final int sizeX = OldImageUtils.getWidth(img);
		final int sizeY = OldImageUtils.getHeight(img);
		final int sizeC = OldImageUtils.getNChannels(img);
		final int sizeZ = OldImageUtils.getNSlices(img);
		final int sizeT = OldImageUtils.getNFrames(img);

		LegacyImgLibDataset dataset = new LegacyImgLibDataset(img);
		
		ProcessorFactory imglibFactory = new ImgLibProcessorFactory(img);
		
		ProcessorFactory factory = new IJ1ProcessorFactory(dataset, imglibFactory);
		
		BridgeStack stack = new BridgeStack(dataset, factory);
		
		final ImagePlus imp = new ImagePlus(img.getName(), stack);

		if (id != null)
		{
			final FileInfo fi = new FileInfo();
			fi.width = sizeX;
			fi.height = sizeY;
			final File file = new File(id);
			if (file.exists())
			{
				fi.fileName = file.getName();
				fi.directory = file.getParent();
				imp.setTitle(fi.fileName);
			}
			else
				fi.url = id;
			imp.setFileInfo(fi);
		}

		// let ImageJ know what dimensions we have
		imp.setDimensions(sizeC, sizeZ, sizeT);
		imp.setOpenAsHyperStack(true);

		return imp;
	}
}
