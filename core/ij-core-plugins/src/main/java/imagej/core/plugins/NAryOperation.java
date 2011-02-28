package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;

import imglib.ops.function.RealFunction;
import imglib.ops.operation.AssignOperation;

import java.util.ArrayList;
import java.util.List;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

// TODO - class was abstract - not sure that was necessary - removed for now

/**
 * TODO
 *
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class NAryOperation<T extends RealType<T>> implements ImageJPlugin {

	// TODO - SwingInputHarvester must prompt user for list of images
	//  + need N-list widget with plus and minus buttons
	//  + need Dataset support -- JComboBox

	// TODO - eventually, need to resolve raw type warnings

	// ***************  instance variables ***************************************************************
	
	@Parameter
	protected List<Dataset> in;

	@Parameter(output=true)
	protected Dataset out;

	/** The imglib-ops function to execute. */
	private RealFunction<T> function;

	// ***************  public/protected interface ***************************************************************

	/** this constructor required for plugins that create a function that uses one of it's @Parameter values in construction */
	protected NAryOperation()
	{
		this.function = null;
	}
	
	/** preferred constructor */
	protected NAryOperation(RealFunction<T> function)
	{
		setFunc(function);
	}

	/** helper method that allows the function of an operation to be changed */
	public void setFunction(RealFunction<T> function)
	{
		setFunc(function);
	}
	
	/** runs the plugin applying the operation's function to the input and assigning it to the output */
	@Override
	public void run()
	{
		if (in == null)  // TODO - temporary code to test these until IJ2 plugins can correctly fill a List<Dataset> @Parameter
		{
			Image<UnsignedShortType> junkImage = createImage(new UnsignedShortType(), new int[]{200,200});
			Cursor<UnsignedShortType> cursor = junkImage.createCursor();
			int index = 0;
			for (UnsignedShortType pixRef : cursor)
				pixRef.set(index++);
			cursor.close();
			in = new ArrayList<Dataset>();
			in.add(new Dataset(junkImage));
		}
		
		if (function == null)
			throw new IllegalStateException("function reference is null: function must be set via constructor or setFunction() before calling NAryOperation::run()");
			
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

	// ***************  private interface ***************************************************************

	/** this exists so that anyone overriding setFunction() does not break NAryOperation(function) constructor */
	private void setFunc(RealFunction<T> func)
	{
		this.function = func;
	}
	
	/** create an image of given type and dimensions using specified container type */
	private static <K extends RealType<K>> Image<K> createImage(RealType<K> type, int[] dimensions)
	{
		PlanarContainerFactory cFact = new PlanarContainerFactory();
		ImageFactory<K> factory = new ImageFactory<K>((K)type, cFact);
		return factory.createImage(dimensions);
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

	/** make a Dataset from an Image */
	private Dataset datasetFromImage(Image<T> image)
	{
		return new Dataset(image);
	}

}
