package imagej.process.query;

public class HistogramQuery implements Gatherer
{
	private int[] histogram;
	
	public HistogramQuery(int numValuesPossible)
	{
		this.histogram = new int[numValuesPossible];
	}
	
	public void init()
	{
	}

	public void collectInfo(int[] position, double value)
	{
		this.histogram[(int)value]++;
	}

	public void done()
	{
	}

	public int[] getHistogram()
	{
		return this.histogram;
	}
}
