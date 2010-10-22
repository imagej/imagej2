package imagej.process.query;

/** HistogramQuery will calculate the histogram of an image. If you want a histogram of a subset of an image
 *  attach a SelectionFunction to the QueryOperation before executing it. */
public class HistogramQuery implements InfoCollector
{
	/** the actual histogram data */
	private int[] histogram;
	
	/** constructs this collector based upon the range of values it expects to see in the query. Note that binning
	 * is not directly supported here. Enhance as needed. */ 
	public HistogramQuery(int numValuesPossible)
	{
		this.histogram = new int[numValuesPossible];
	}
	
	/** nothing to intialize */
	public void init()
	{
	}

	/** updates the histogram for this position/sample combo */
	public void collectInfo(int[] position, double value)
	{
		this.histogram[(int)value]++;
	}

	/** nothing to complete */
	public void done()
	{
	}

	/** an accessor to get the histogram that was collected after the QueryOperation finishes */
	public int[] getHistogram()
	{
		return this.histogram;
	}
}
