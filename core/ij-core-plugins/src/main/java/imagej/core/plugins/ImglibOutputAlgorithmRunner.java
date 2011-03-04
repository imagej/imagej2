package imagej.core.plugins;

import mpicbg.imglib.algorithm.OutputAlgorithm;
import imagej.model.Dataset;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
public class ImglibOutputAlgorithmRunner
{
	// ********** instance variables ****************************************************************
	
	private OutputAlgorithm algorithm;
	
	// ***************  constructor ***************************************************************

	/** preferred constructor */
	public ImglibOutputAlgorithmRunner(final OutputAlgorithm algorithm)
	{
		this.algorithm = algorithm;
	}

	// ***************  public interface ***************************************************************

	/** run the plugin and assign output */
	public Dataset run()
	{
		if (this.algorithm == null)
			throw new IllegalStateException("algorithm reference is null");
		
		if (!algorithm.checkInput() || !algorithm.process())
			throw new IllegalStateException(algorithm.getErrorMessage());
		
		return new Dataset(algorithm.getResult());
	}
}
