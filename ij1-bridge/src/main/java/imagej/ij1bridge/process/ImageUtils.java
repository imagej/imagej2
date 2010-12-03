package imagej.ij1bridge.process;

import ij.ImagePlus;
import ij.io.FileInfo;
import imagej.EncodingManager;
import imagej.DataType;
import imagej.ij1bridge.ImgLibImageStack;

import java.io.File;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.logic.BitType;
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
public class ImageUtils
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
	public static ImgLibProcessor<?> createProcessor(int width, int height, Object pixels, DataType type)
	{
		EncodingManager.verifyTypeCompatibility(pixels, type);
		
		PlanarContainerFactory containerFactory = new PlanarContainerFactory();

		ImgLibProcessor<?> proc = null;

		int[] dimensions = new int[]{width, height, 1};

		switch (type)
		{
			case BIT:
			{
				Image<BitType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new BitType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<BitType>(hatchedImage, 0);
			}
			break;
			case BYTE:
			{
				Image<ByteType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new ByteType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<ByteType>(hatchedImage, 0);
			}
			break;
			
			case UBYTE:
			{
				Image<UnsignedByteType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new UnsignedByteType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<UnsignedByteType>(hatchedImage, 0);
			}
			break;
			
			case UINT12:
			{
				Image<Unsigned12BitType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new Unsigned12BitType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<Unsigned12BitType>(hatchedImage, 0);
			}
			break;
			
			case SHORT:
			{
				Image<ShortType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new ShortType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<ShortType>(hatchedImage, 0);
			}
			break;
			
			case USHORT:
			{
				Image<UnsignedShortType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new UnsignedShortType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<UnsignedShortType>(hatchedImage, 0);
			}
			break;
			
			case INT:
			{
				Image<IntType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new IntType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<IntType>(hatchedImage, 0);
			}
			break;
			
			case UINT:
			{
				Image<UnsignedIntType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new UnsignedIntType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<UnsignedIntType>(hatchedImage, 0);
			}
			break;
			
			case LONG:
			{
				Image<LongType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new LongType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<LongType>(hatchedImage, 0);
			}
			break;
			
			case FLOAT:
			{
				Image<FloatType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new FloatType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<FloatType>(hatchedImage, 0);
			}
			break;
			
			case DOUBLE:
			{
				Image<DoubleType> hatchedImage = imagej.imglib.process.ImageUtils.createImage(new DoubleType(), containerFactory, dimensions);
				proc = new ImgLibProcessor<DoubleType>(hatchedImage, 0);
			}
			break;
			
			default:
				throw new IllegalArgumentException("unsupprted type specified "+type);
		}

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
		final int sizeX = imagej.imglib.process.ImageUtils.getWidth(img);
		final int sizeY = imagej.imglib.process.ImageUtils.getHeight(img);
		final int sizeC = imagej.imglib.process.ImageUtils.getNChannels(img);
		final int sizeZ = imagej.imglib.process.ImageUtils.getNSlices(img);
		final int sizeT = imagej.imglib.process.ImageUtils.getNFrames(img);

		final ImgLibImageStack stack = new ImgLibImageStack(img);

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
