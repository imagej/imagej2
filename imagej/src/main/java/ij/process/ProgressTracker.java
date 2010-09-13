package ij.process;

public class ProgressTracker
{
	private long numOperations, operationsSoFar, updateFrequency;
	private ImageProcessor proc;
	
	public ProgressTracker(ImageProcessor proc, long numOperations, long updateFrequency)
	{
		if ((updateFrequency < 1) || (updateFrequency > numOperations))
		{
			if (numOperations < 4)
				updateFrequency = 1;
			else
				updateFrequency = numOperations / 4;
		}
		
		this.proc = proc;
		this.numOperations = numOperations;
		this.updateFrequency = updateFrequency;
		
		init();
	}
	
	public void init()
	{
		this.operationsSoFar = 0;
	}
	
	public void didOneMore()
	{
		this.operationsSoFar++;
		if ((this.operationsSoFar % this.updateFrequency) == 0)
		{
			double percentDone = ((double) this.operationsSoFar) / this.numOperations;
			proc.showProgress(percentDone);
		}
	}
	
	public void done()
	{
		proc.showProgress(1.0);
	}
}
