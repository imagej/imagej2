package imagej.process.query;

public interface Gatherer
{
	void init();
	void collectInfo(int[] position, double value);
	void done();
}
