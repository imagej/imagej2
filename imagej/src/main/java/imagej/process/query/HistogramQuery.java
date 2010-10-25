package imagej.process.query;

/** HistogramQuery will calculate the histogram of an image. If you want a histogram of a subset of an image
 *  attach a SelectionFunction to the QueryOperation before executing it. */
public class HistogramQuery implements InfoCollector
{
	/** the limit below which any value will be counted in the first bin */
	private double minValue;
	
	/** the limit above which any value will be counted in the last bin */
	private double maxValue;
	
	/** the actual histogram data */
	private int[] histogram;
	
	/** constructs this collector based upon the range of values it expects to see in the query. */ 
	public HistogramQuery(int numBins, double minValue, double maxValue)
	{
		this.histogram = new int[numBins];
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	/** nothing to initialize */
	public void init()
	{
	}

	/** updates the histogram for this position/sample combo */
	public void collectInfo(int[] position, double value)
	{
		int binNumber = calcBinNumber(value);
		
		this.histogram[binNumber]++;
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
	
	private int calcBinNumber(double value)
	{
		if (value < this.minValue)
			value = this.minValue;
		
		if (value > this.maxValue)
			value = this.maxValue;
		
		double relativePosition = (value - this.minValue) / (this.maxValue - this.minValue);
		
		return (int) (relativePosition * (this.histogram.length-1)); // TODO: round value first???
	}
}
