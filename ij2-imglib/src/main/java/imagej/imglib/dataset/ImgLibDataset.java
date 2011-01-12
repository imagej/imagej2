package imagej.imglib.dataset;

import imagej.AxisLabel;
import imagej.MetaData;
import imagej.data.Type;
import imagej.dataset.Dataset;
import imagej.imglib.TypeManager;
import imagej.imglib.process.ImageUtils;
import mpicbg.imglib.container.Container;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** A dataset that draws its image data from an ImgLib image. */
public class ImgLibDataset<T extends RealType<T>> implements Dataset {

	private Image<T> img;
	private MetaData metadata;

	/** Used to open one cursor on the image per calling thread. */
	private ThreadLocal<LocalizableByDimCursor<T>> activeCursor =
		new ThreadLocal<LocalizableByDimCursor<T>>()
	{
		protected synchronized LocalizableByDimCursor<T> initialValue() {
			return img.createLocalizableByDimCursor();
		}
	};

	public ImgLibDataset(Image<T> img) {
		this.img = img;
		final String name = img.getName();
		final String imageName = decodeName(name);
		final String[] imageTypes = decodeTypes(name);
		final AxisLabel[] axisLabels = new AxisLabel[imageTypes.length];
		for (int i=0; i<imageTypes.length; i++) {
			axisLabels[i] = AxisLabel.getAxisLabel(imageTypes[i]);
		}
		metadata = new MetaData();
		metadata.setAxisLabels(axisLabels);
		metadata.setLabel(imageName);
	}

	public Image<T> getImage() {
		return img;
	}

	@Override
	public int[] getDimensions() {
		return img.getDimensions();
	}

	@Override
	public Type getType() {
		return TypeManager.getIJType(ImageUtils.getType(img));
	}

	@Override
	public MetaData getMetaData() {
		return metadata;
	}

	@Override
	public void setMetaData(MetaData metadata) {
		this.metadata = metadata;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

	@Override
	public Dataset getParent() {
		return null;
	}

	@Override
	public void setParent(Dataset dataset) {
		throw new UnsupportedOperationException("Cannot set parent dataset");
	}

	@Override
	public Object getData() {
		final Container<T> container = img.getContainer();
		if (container instanceof ArrayDataAccess) {
			final ArrayDataAccess<?> arrayDataAccess = (ArrayDataAccess<?>) container;
			return arrayDataAccess.getCurrentStorageArray();
		}
		throw new UnsupportedOperationException("No direct backing data array");
	}

	@Override
	public void releaseData() {
		throw new UnsupportedOperationException("Cannot release backing data array");
	}

	@Override
	public void setData(Object data) {
		throw new UnsupportedOperationException("Cannot override backing data array");
	}

	@Override
	public Dataset insertNewSubset(int position) {
		throw new UnsupportedOperationException("Cannot modify image dimensions");
	}

	@Override
	public Dataset removeSubset(int position) {
		throw new UnsupportedOperationException("Cannot modify image dimensions");
	}

	@Override
	public Dataset getSubset(int position) {
		throw new UnsupportedOperationException("Cannot extract image subset");
	}

	@Override
	public Dataset getSubset(int[] index) {
		throw new UnsupportedOperationException("Cannot extract image subset");
	}

	@Override
	public double getDouble(int[] position) {
		final LocalizableByDimCursor<T> cursor = activeCursor.get();
		cursor.setPosition(position);
		return cursor.getType().getRealDouble();
	}

	@Override
	public void setDouble(int[] position, double value) {
		final LocalizableByDimCursor<T> cursor = activeCursor.get();
		cursor.setPosition(position);
		cursor.getType().setReal(value);
	}

	@Override
	public long getLong(int[] position) {
		throw new UnsupportedOperationException("Cannot get long data");
	}

	@Override
	public void setLong(int[] position, long value) {
		throw new UnsupportedOperationException("Cannot set long data");
	}

	// CTR TODO - Code below is duplicated from imglib-io ImageOpener class.
	// This functionality should live in a common utility place somewhere instead.

	/** Converts the given image name back to a list of dimensional axis types. */
	public static String decodeName(String name) {
		final int lBracket = name.lastIndexOf(" [");
		return name.substring(0, lBracket);
	}

	/** Converts the given image name back to a list of dimensional axis types. */
	public static String[] decodeTypes(String name) {
		final int lBracket = name.lastIndexOf(" [");
		if (lBracket < 0) return new String[0];
		final int rBracket = name.lastIndexOf("]");
		if (rBracket < lBracket) return new String[0];
		return name.substring(lBracket + 2, rBracket).split(" ");
	}

}
