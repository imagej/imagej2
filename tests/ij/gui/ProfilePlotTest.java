package ij.gui;

import static org.junit.Assert.*;

import java.awt.Dimension;

import ij.Assert;
import ij.IJInfo;
import ij.ImagePlus;
import ij.process.ByteProcessor;

import org.junit.Test;

public class ProfilePlotTest {

	ProfilePlot p;

	private ProfilePlot newPlot(boolean avgHorz)
	{
		ImagePlus imp = new ImagePlus("fred",new ByteProcessor(1,2,new byte[]{1,2},null));
		Roi roi = new Roi(0,0,1,2);
		imp.setRoi(roi);
		return new ProfilePlot(imp,avgHorz);
	}
	
	@Test
	public void testProfilePlot() {
		p = new ProfilePlot();
		assertNotNull(p);
	}

	@Test
	public void testProfilePlotImagePlus() {
		ImagePlus imp = new ImagePlus("fred",new ByteProcessor(1,2,new byte[]{1,2},null));
		Roi roi = new Roi(0,0,1,2);
		imp.setRoi(roi);
		p = new ProfilePlot(imp);
		assertNotNull(p);
	}

	@Test
	public void testProfilePlotImagePlusBoolean() {
		ImagePlus imp;
		Roi roi;
		
		// false case
		imp = new ImagePlus("fred",new ByteProcessor(1,2,new byte[]{1,2},null));
		roi = new Roi(0,0,1,2);
		imp.setRoi(roi);
		p = new ProfilePlot(imp,false);
		assertNotNull(p);
		
		// true case
		imp = new ImagePlus("fred",new ByteProcessor(1,2,new byte[]{1,2},null));
		roi = new Roi(0,0,1,2);
		imp.setRoi(roi);
		p = new ProfilePlot(imp,true);
		assertNotNull(p);
	}

	@Test
	public void testGetPlotSize() {
		p = newPlot(true);
		Dimension d = p.getPlotSize();
		assertEquals(175,d.height);
		assertEquals(350,d.width);
	}

	@Test
	public void testCreateWindow() {
		p = newPlot(false);
		p.createWindow();
	}

	@Test
	public void testGetProfile() {
		p = newPlot(false);
		double[] profile = p.getProfile();
		assertEquals(1,profile.length);
		assertEquals(1.5,profile[0],Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetMinAndMax() {
		p = newPlot(true);
		assertEquals(1,p.getMin(),Assert.DOUBLE_TOL);
		assertEquals(2,p.getMax(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testFixedMinAndMaxStuff() {
		assertEquals(0,ProfilePlot.getFixedMin(),Assert.DOUBLE_TOL);
		assertEquals(0,ProfilePlot.getFixedMax(),Assert.DOUBLE_TOL);
		ProfilePlot.setMinAndMax(14,96);
		assertEquals(14,ProfilePlot.getFixedMin(),Assert.DOUBLE_TOL);
		assertEquals(96,ProfilePlot.getFixedMax(),Assert.DOUBLE_TOL);
	}
}
