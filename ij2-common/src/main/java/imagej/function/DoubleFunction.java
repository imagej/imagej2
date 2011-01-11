package imagej.function;

public interface DoubleFunction {
	int getValueCount();
	double compute(double[] inputs);
}
