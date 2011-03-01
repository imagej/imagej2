package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;

import imglib.ops.function.RealFunction;
import imglib.ops.operation.AssignOperation;

import java.util.ArrayList;
import java.util.List;

import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

// TODO - class was abstract - not sure that was necessary - removed for now

/**
 * TODO
 *
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class NAryOperation implements ImageJPlugin
{
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
	private RealFunction function;

	// ***************  public/protected interface ***************************************************************

	/** this constructor required for plugins that create a function that uses one of it's @Parameter values in construction */
	public NAryOperation()
	{
		this.function = null;
	}
	
	/** preferred constructor */
	public NAryOperation(RealFunction function)
	{
		this.function = function;
	}

	/** helper method that allows the function of an operation to be changed */
	public void setFunction(RealFunction function)
	{
		this.function = function;
	}
	
	/** runs the plugin applying the operation's function to the input and assigning it to the output */
	@Override
	public void run()
	{
		if (in == null)  // TODO - temporary code to test these until IJ2 plugins can correctly fill a List<Dataset> @Parameter
		{
			Image<UnsignedShortType> junkImage = Dataset.createPlanarImage("", new UnsignedShortType(), new int[]{200,200});
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
		final Image[] inputs = new Image[in.size()];
		
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] = imageFromDataset(in.get(i));
		}
		
		final Image output;
		if (out != null)
			output = imageFromDataset(out);
		else
			output = zeroDataImageWithSameAttributes(inputs[0]);  // TODO - must be given at least one input image or this will be unhappy
		
		final AssignOperation operation = new AssignOperation(inputs, output, function);

		operation.execute();
		
		out = datasetFromImage(output);
	}

	// ***************  private interface ***************************************************************

	/** make an image that has same type and dimensions as Dataset */
	private Image imageFromDataset(Dataset dataset)
	{
		//@SuppressWarnings("unchecked")
		return dataset.getImage();
	}

	/** make an image that has same type, container, and dimensions as refImage */
	private Image zeroDataImageWithSameAttributes(Image refImage)
	{
		return refImage.createNewImage(refImage.getDimensions());
	}

	/** make a Dataset from an Image */
	private Dataset datasetFromImage(Image image)
	{
		return new Dataset(image);
	}

}
