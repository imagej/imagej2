package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;

import imglib.ops.function.RealFunction;
import imglib.ops.operation.AssignOperation;

import java.util.List;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

// TODO - was abstract - not sure that was necessary - removed for now

public class NAryOperation<T extends RealType<T>> implements ImageJPlugin
{

	// TODO - SwingInputHarvester must prompt user for list of images
	//  + need N-list widget with plus and minus buttons
	//  + need Dataset support -- JComboBox

	// TODO - eventually, need to resolve raw type warnings

	@Parameter
	private List<Dataset> in;

	@Parameter(output=true)
	private Dataset out;

	/** The imglib-ops function to execute. */
	private RealFunction<T> function;

	protected NAryOperation(RealFunction<T> function)
	{
		this.function = function;
	}

	@Override
	public void run()
	{
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
