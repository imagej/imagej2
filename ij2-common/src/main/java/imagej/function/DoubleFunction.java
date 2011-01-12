package imagej.function;

/** DoubleFunction represents a function that takes a number of doubles as input and returns a double as output */
public interface DoubleFunction
{
	/** the number of input parameters the function can handle. A value of -1 implies any number of parameters can be handled by the function. */
	int getParameterCount();

	/** computes the value of the function with the given inputs */
	double compute(double[] inputs);
}
