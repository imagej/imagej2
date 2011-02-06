package imagej.imglib.examples.function.observer;

public class ProgressTracker implements Observer
{
	private long expectedOperations;
	private long updateFrequency;
	private long operationsSoFar;

	public ProgressTracker(long expctedOperations, long updateFrequency)
	{
		this.expectedOperations = expctedOperations;
		this.updateFrequency = updateFrequency;
	}
	
	@Override
	public void init()
	{
		operationsSoFar = 0;
		// TODO - setup progress indicator and init to start value
	}

	@Override
	public void update(int[] position, double value, boolean accepted)
	{
		operationsSoFar++;
		if ((operationsSoFar % updateFrequency) == 0)
		{
			double percentDone = (double)operationsSoFar / (double)expectedOperations;
			// TODO - update progress indicator
		}
	}

	@Override
	public void done(boolean wasAborted)
	{
		// TODO - update progress indicator to show 100% done
	}

}
