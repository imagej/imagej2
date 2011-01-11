package imagej.function;

public interface LongFunction {
	int getValueCount();
	long compute(long[] inputs);
}
