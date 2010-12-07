package imagej.data;

public interface DataAccessor
{
	double getReal(int index);
	void setReal(int index, double value);
	long getIntegral(int index);
	void setIntegral(int index, long value);
}
