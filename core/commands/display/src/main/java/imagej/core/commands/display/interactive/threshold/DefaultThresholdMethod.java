package imagej.core.commands.display.interactive.threshold;

import imagej.plugin.Plugin;

@Plugin(type = AutoThresholdMethod.class, name = "Default")
public class DefaultThresholdMethod implements AutoThresholdMethod {

	@Override
	public int getThreshold(long[] histogram) {
		// Original IJ implementation for compatibility.
		int level;
		int maxValue = histogram.length - 1;
		double result, sum1, sum2, sum3, sum4;

		int min = 0;
		while ((histogram[min] == 0) && (min < maxValue))
			min++;
		int max = maxValue;
		while ((histogram[max] == 0) && (max > 0))
			max--;
		if (min >= max) {
			level = histogram.length / 2;
			return level;
		}

		int movingIndex = min;
		do {
			sum1 = sum2 = sum3 = sum4 = 0.0;
			for (int i = min; i <= movingIndex; i++) {
				sum1 += i * histogram[i];
				sum2 += histogram[i];
			}
			for (int i = (movingIndex + 1); i <= max; i++) {
				sum3 += i * histogram[i];
				sum4 += histogram[i];
			}
			result = (sum1 / sum2 + sum3 / sum4) / 2.0;
			movingIndex++;
		}
		while ((movingIndex + 1) <= result && movingIndex < max - 1);

		level = (int) Math.round(result);
		return level;
	}


}
