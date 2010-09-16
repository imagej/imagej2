package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;

public interface NAryFunction {
	void compute(RealType<?> result, RealType<?>[] inputs);
}
