package imagej2.function;

public interface NAryFunction {
	int getValueCount();
	double compute(double[] inputs);
}
