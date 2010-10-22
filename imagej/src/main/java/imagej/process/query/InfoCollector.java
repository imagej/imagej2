package imagej.process.query;

/** the InfoCollector interface is used to define queries that can be passed to an
 *  imagej.process.operation.QueryOperation.*/
public interface InfoCollector
{
	/** this method called before the actual query takes place allowing the InfoCollector to initialize itself */
	void init();
	
	/** this method is called at each position of the original dataset allowing data to be collected */
	void collectInfo(int[] position, double value);
	
	/** this method is called when the query is done allowing cleanup and tabulation of results */
	void done();
}
