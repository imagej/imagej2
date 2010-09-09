package ij.gui;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.*;
import ij.process.*;

public class FreehandRoiTest {
	
	FreehandRoi roi;

	// only one public method - the constructor
	@Test
	public void testFreehandRoi() {
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// the underlying superclass PolygonRoi assumes that an ImageCanvas exists. This next test bombs out as is.
			
			ImagePlus ip = new ImagePlus("Zoops",new ByteProcessor(2,2,new byte[]{1,2,3,4},null));
			roi = new FreehandRoi(2,4,ip);
			assertNotNull(roi);
		}
	}

}
