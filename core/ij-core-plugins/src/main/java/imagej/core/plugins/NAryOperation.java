package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;

import imglib.ops.function.RealFunction;
import imglib.ops.operation.AssignOperation;

import java.util.ArrayList;
import java.util.List;

import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

// TODO - was abstract - not sure that was necessary - removed for now

public class NAryOperation<T extends RealType<T>> implements ImageJPlugin
{

	// TODO - SwingInputHarvester must prompt user for list of images
	//  + need N-list widget with plus and minus buttons
	//  + need Dataset support -- JComboBox

	// TODO - eventually, need to resolve raw type warnings

//	@Parameter
	protected List<Dataset> in;

	@Parameter(output=true)
	protected Dataset out;

	/** The imglib-ops function to execute. */
	private RealFunction<T> function;

	protected NAryOperation(RealFunction<T> function)
	{
		this.function = function;
	}

	private static <K extends RealType<K>> Image<K> createImage(RealType<K> type, ContainerFactory cFact, int[] dimensions)
	{
		ImageFactory<K> factory = new ImageFactory<K>((K)type, cFact);
		return factory.createImage(dimensions);
	}
	
	@Override
	public void run()
	{
		PlanarContainerFactory factory = new PlanarContainerFactory();
		Image<UnsignedShortType> junkImage = createImage(new UnsignedShortType(), factory, new int[]{200,200});
		Cursor<UnsignedShortType> cursor = junkImage.createCursor();
		int index = 0;
		for (UnsignedShortType val : cursor)
			cursor.getType().set(index++);
		cursor.close();
		in = new ArrayList<Dataset>();
		in.add(new Dataset(junkImage,null));
		
		//@SuppressWarnings("unchecked")
		final Image<T>[] inputs = new Image[in.size()];
		
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] = imageFromDataset(in.get(i));
		}
		
		final Image<T> output;
		if (out != null)
			output = imageFromDataset(out);
		else
			output = zeroDataImageWithSameAttributes(inputs[0]);  // TODO - must be given at least one input image or this will be unhappy
		
		final AssignOperation<T> operation = new AssignOperation<T>(inputs, output, function);

		operation.execute();
		
		out = datasetFromImage(output);
	}

	/** make an image that has same type and dimensions as Dataset (but in a planar container) */
	private Image<T> imageFromDataset(Dataset dataset)
	{
		//@SuppressWarnings("unchecked")
		return (Image<T>) dataset.getImage();
	}

	/** make an image that has same type, container, and dimensions as refImage */
	private Image<T> zeroDataImageWithSameAttributes(Image<T> refImage)
	{
		return refImage.createNewImage(refImage.getDimensions());
	}
	
	private Dataset datasetFromImage(Image<T> image)
	{
		return new Dataset(image,null);  // TODO - simplify Dataset ctor to not take Metadata. Have ctor figure metadata from Image (soon to be Img)
										// And then default AxisLabels if needed.
	}

}
