package imagej2.ij1bridge.process;

import ij.process.ImageProcessor;
import imagej2.process.Observer;

/** ProgressTracker updates the progress bar associated with an ImageProcessor.
 */
public class ProgressTracker implements Observer
{
	private long numOperations, operationsSoFar, updateFrequency;
	private ImageProcessor proc;
	
	/** ProgressTracker constructor
	 * @param proc - the ImageProcessor upon which an operation if being done
	 * @param numOperations - the expected number of operations to be done
	 * @param updateFrequency - the number of operations between updates to the progress bar
	 */
	public ProgressTracker(ImageProcessor proc, long numOperations, long updateFrequency)
	{
		if ((updateFrequency < 1) || (updateFrequency > numOperations))
		{
			updateFrequency = numOperations / 6;
			if (updateFrequency == 0)
				updateFrequency = 1;
		}
		
		this.proc = proc;
		this.numOperations = numOperations;
		this.updateFrequency = updateFrequency;
	}

	/** always called first by the observed process */
	public void init()
	{
		this.operationsSoFar = 0;
	}

	/** called every time an operation is performed. Note that this update() function does not imply
	 * that meaningful work was done. Only that the operation iteration has move forward one step.
	 * For more information of how an Observer is used see SingleCursorRoiOperation.
	 */
	public void update()
	{
		this.operationsSoFar++;
		if ((this.operationsSoFar % this.updateFrequency) == 0)
		{
			double percentDone = ((double) this.operationsSoFar) / this.numOperations;
			proc.showProgress(percentDone);
		}
	}
	
	/** always called last by the observed process */
	public void done()
	{
		proc.showProgress(1.0);
	}
}
