package imagej.imglib;

import imagej.dataset.Dataset;
import imagej.process.Index;
import mpicbg.imglib.container.Container;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.container.basictypecontainer.array.ByteArray;
import mpicbg.imglib.container.basictypecontainer.array.CharArray;
import mpicbg.imglib.container.basictypecontainer.array.DoubleArray;
import mpicbg.imglib.container.basictypecontainer.array.FloatArray;
import mpicbg.imglib.container.basictypecontainer.array.IntArray;
import mpicbg.imglib.container.basictypecontainer.array.LongArray;
import mpicbg.imglib.container.basictypecontainer.array.ShortArray;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.RealType;

/** this class designed to hold functionality that could be migrated to imglib */
public class ImageUtils
{

	/** gets the imglib type of an imglib image */
	public static RealType<?> getType(Image<?> image)
	{
		Cursor<?> cursor = image.createCursor();
		RealType<?> type = (RealType<?>) cursor.getType();
		cursor.close();
		return type;
	}

	/** creates an ImgLib Image whose type and shape match an input Dataset. It is called a shadow image because the data planes are
	 * shared between the Dataset and the created ImgLib Image.
	 */
	public static Image<?> createShadowImage(Dataset dataset)
	{
		PlanarContainerFactory factory = new PlanarContainerFactory();
		
		int[] dimensions = dataset.getDimensions();

		RealType<?> realType = TypeManager.getRealType(dataset.getType());
		
		Image<?> shadowImage = ImageUtils.createImage(realType, factory, dimensions);

		int subDimensionLength = dimensions.length-2;
		
		int[] position = Index.create(subDimensionLength);
		
		int[] origin = Index.create(subDimensionLength);

		int[] span = new int[subDimensionLength];
		for (int i = 0; i < subDimensionLength; i++)
			span[i] = dimensions[i];
		
		PlanarAccess<ArrayDataAccess<?>> access = ImageUtils.getPlanarAccess(shadowImage);

		int planeNum = 0;
		
		while (Index.isValid(position, origin, span))
		{
			Dataset plane = dataset.getSubset(position);
			Object array = plane.getData();
			if (array == null)
				throw new IllegalArgumentException("cannot create shadow image: dataset is not organized in a planar fashion");
			access.setPlane(planeNum, ImageUtils.makeArray(array));
			Index.increment(position, origin, span);
		}
		
		return shadowImage;
	}

	/** Obtains planar access instance backing the given image, if any. */
	@SuppressWarnings("unchecked")
	public static PlanarAccess<ArrayDataAccess<?>> getPlanarAccess(Image<?> im) {
		PlanarAccess<ArrayDataAccess<?>> planarAccess = null;
		final Container<?> container = im.getContainer();
		if (container instanceof PlanarAccess<?>) {
			planarAccess = (PlanarAccess<ArrayDataAccess<?>>) container;
		}
		return planarAccess;
	}

	/** Wraps raw primitive array in imglib Array object. */
	public static ArrayDataAccess<?> makeArray(Object array) {
		final ArrayDataAccess<?> access;
		if (array instanceof byte[]) {
			access = new ByteArray((byte[]) array);
		}
		else if (array instanceof short[]) {
			access = new ShortArray((short[]) array);
		}
		else if (array instanceof int[]) {
			access = new IntArray((int[]) array);
		}
		else if (array instanceof float[]) {
			access = new FloatArray((float[]) array);
		}
		else if (array instanceof long[]) {
			access = new LongArray((long[]) array);
		}
		else if (array instanceof double[]) {
			access = new DoubleArray((double[]) array);
		}
		else if (array instanceof char[]) {
			access = new CharArray((char[]) array);
		}
		else
			access = null;
		return access;
	}

	@SuppressWarnings({"unchecked"})
	public static <K extends RealType<K>> Image<K> createImage(RealType<K> type, ContainerFactory cFact, int[] dimensions)
	{
		ImageFactory<K> factory = new ImageFactory<K>((K)type, cFact);
		return factory.createImage(dimensions);
	}

}
