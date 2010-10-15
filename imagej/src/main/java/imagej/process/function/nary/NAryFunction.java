package imagej.process.function.nary;

public interface NAryFunction {
	int getValueCount();
	double compute(double[] inputs);
}
