package imagej.function;

/** LongFunction represents a function that takes a number of longs as input and returns a long as output */
public interface LongFunction
{
	/** the number of input parameters the function can handle. A value of -1 implies any number of parameters can be handled by the function. */
	int getParameterCount();

	/** computes the value of the function with the given inputs */
	long compute(long[] inputs);
}
