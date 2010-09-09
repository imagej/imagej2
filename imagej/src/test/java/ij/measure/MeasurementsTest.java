package ij.measure;

import static org.junit.Assert.*;

import org.junit.Test;

public class MeasurementsTest {
	@Test
	public void testConstants()
	{
		assertEquals(1,Measurements.AREA);
		assertEquals(2,Measurements.MEAN);
		assertEquals(4,Measurements.STD_DEV);
		assertEquals(8,Measurements.MODE);
		assertEquals(16,Measurements.MIN_MAX);
		assertEquals(32,Measurements.CENTROID);
		assertEquals(64,Measurements.CENTER_OF_MASS);
		assertEquals(128,Measurements.PERIMETER);
		assertEquals(256,Measurements.LIMIT);
		assertEquals(512,Measurements.RECT);
		assertEquals(1024,Measurements.LABELS);
		assertEquals(2048,Measurements.ELLIPSE);
		assertEquals(4096,Measurements.INVERT_Y);
		assertEquals(8192,Measurements.CIRCULARITY);
		assertEquals(8192,Measurements.SHAPE_DESCRIPTORS);
		assertEquals(16384,Measurements.FERET);
		assertEquals(0x8000,Measurements.INTEGRATED_DENSITY);
		assertEquals(0x10000,Measurements.MEDIAN);
		assertEquals(0x20000,Measurements.SKEWNESS);
		assertEquals(0x40000,Measurements.KURTOSIS);
		assertEquals(0x80000,Measurements.AREA_FRACTION); 
		assertEquals(0x100000,Measurements.SLICE);
		assertEquals(0x100000,Measurements.STACK_POSITION);
		assertEquals(0x200000,Measurements.SCIENTIFIC_NOTATION);
		assertEquals(20,Measurements.MAX_STANDARDS);
	}
}
