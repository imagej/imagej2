package imagej.process.query;

public interface InfoCollector
{
	void init();
	void collectInfo(int[] position, double value);
	void done();
}
