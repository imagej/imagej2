package imagej.function;

public interface NAryFunction {
	int getValueCount();
	double compute(double[] inputs);
}
