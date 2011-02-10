package imagej.ij1bridge;

import ij.process.ImageProcessor;

public interface ProcessorFactory
{
	ImageProcessor makeProcessor(int[] planePos);
}
	
