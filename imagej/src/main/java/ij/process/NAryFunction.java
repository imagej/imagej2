package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public interface NAryFunction {
	void compute(RealType<?> result, RealType<?>[] inputs);
}
