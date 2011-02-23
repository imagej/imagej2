package imagej.model;

import mpicbg.imglib.container.Container;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.container.basictypecontainer.array.ByteArray;
import mpicbg.imglib.container.basictypecontainer.array.DoubleArray;
import mpicbg.imglib.container.basictypecontainer.array.FloatArray;
import mpicbg.imglib.container.basictypecontainer.array.IntArray;
import mpicbg.imglib.container.basictypecontainer.array.LongArray;
import mpicbg.imglib.container.basictypecontainer.array.ShortArray;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.RealType;

public class Dataset {

	private final Image<?> image;
	private final Metadata metadata;

	public Dataset(final Image<?> image, final Metadata metadata) {
		this.image = image;
		this.metadata = metadata;
	}

	public Image<?> getImage() {
		return image;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public Object getPlane(final int no) {
		final Container<?> container = image.getContainer();
		if (!(container instanceof PlanarAccess)) return null;
		final Object plane = ((PlanarAccess<?>) container).getPlane(no);
		if (!(plane instanceof ArrayDataAccess)) return null;
		return ((ArrayDataAccess<?>) plane).getCurrentStorageArray();
	}

	@SuppressWarnings({"rawtypes","unchecked"})
	
	public void setPlane(final int no, final Object plane) {
		final Container<?> container = image.getContainer();
		if (!(container instanceof PlanarAccess)) return;
		final PlanarAccess planarAccess = (PlanarAccess) container;
		ArrayDataAccess<?> array = null;
		if (plane instanceof byte[]) {
			array = new ByteArray((byte[]) plane);
		}
		else if (plane instanceof double[] ) {
			array = new DoubleArray((double[]) plane);
		}
		else if (plane instanceof float[] ) {
			array = new FloatArray((float[]) plane);
		}
		else if (plane instanceof int[] ) {
			array = new IntArray((int[]) plane);
		}
		else if (plane instanceof long[] ) {
			array = new LongArray((long[]) plane);
		}
		else if (plane instanceof short[] ) {
			array = new ShortArray((short[]) plane);
		}
		planarAccess.setPlane(no, array);
	}

	// TEMP
	public boolean isSigned() {
		// HACK - imglib needs a way to query RealTypes for signedness
		final String typeName = image.createType().getClass().getName();
		return !typeName.startsWith("mpicbg.imglib.type.numeric.integer.Unsigned");
	}

	// TEMP
	public boolean isFloat() {
		// HACK - imglib needs a way to query RealTypes for integer vs. float
		final String typeName = image.createType().getClass().getName();
		return typeName.equals("mpicbg.imglib.type.numeric.real.FloatType")
			|| typeName.equals("mpicbg.imglib.type.numeric.real.DoubleType");
	}

	public static <T extends RealType<T>> Dataset create(final String name,
		final T type, final int[] dims, final AxisLabel[] axes)
	{
		final PlanarContainerFactory pcf = new PlanarContainerFactory();
		final ImageFactory<T> imageFactory = new ImageFactory<T>(type, pcf);
		final Image<T> image = imageFactory.createImage(dims, name);
		final Metadata metadata = new Metadata();
		metadata.setName(name);
		metadata.setAxes(axes);
		final Dataset dataset = new Dataset(image, metadata);
		return dataset;
	}

}
