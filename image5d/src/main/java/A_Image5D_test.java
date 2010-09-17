/*
 * Created on 26.03.2005
 */

/**
 * @author Joachim Walter
 *
 * Tests the Image5D class, which extends 
 * ImagePlus to 5 dimensions instead of 3.
 */

import i5d.Image5D;
import ij.*;
import ij.plugin.*;
import ij.process.ByteProcessor;

import java.awt.*;

public class A_Image5D_test implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
//		byte[]pix = new byte[40000];
//		ImageStack is = NewImage.createFloatImage("test", 200, 200, 10, NewImage.FILL_RAMP).getStack();
//		is.deleteLastSlice();
//		is.addSlice("", (Object)pix);
		//ByteProcessor bp = (ByteProcessor)NewImage.createByteImage("test", 200, 200, 1, NewImage.FILL_RAMP).getProcessor();
		
//		ImagePlus imgP = new ImagePlus("test img plus", is);
//		imgP.setSlice(3);
//		imgP.setProcessor("", new ShortProcessor(200,200));
//		imgP.show();
		

//		Image5D img = new Image5D("test", is);
		
//		Image5D img = new Image5D("test", ImagePlus.GRAY8, 500, 200, 1, 1, 1, false);
		
//		Image5D img = new Image5D("test", ImagePlus.GRAY8, new int[] {200,200,2,2,2}, true);
//
//		for (int i=0; i<2; ++i) {
//			img.getChannelCalibration(i+1).setLabel("Ch-"+(i+1));
//			for (int j=0; j<2; ++j) {
//				for (int k=0; k<2; ++k) {
//					Polygon p = new Polygon(new int[] {10+j*50, 100+i*100, 100+k*100, 10}, new int[] {10, 10, 200, 200}, 4);
//					img.setCurrentPosition(0,0,i, j, k);
//					img.getProcessor().setValue(127);
//					img.getProcessor().fillPolygon(p);
//				}
//			}
//		}


        ImageStack stack = new ImageStack(200, 200);
        for (int i=0; i<2; ++i) {
            for (int j=0; j<2; ++j) {
                for (int k=0; k<2; ++k) {
                    Polygon p = new Polygon(new int[] {10+j*50, 100+i*100, 100+k*100, 10}, new int[] {10, 10, 200, 200}, 4);
                    ByteProcessor proc = new ByteProcessor(200,200);
                    proc.setValue(127);
                    proc.fillPolygon(p);
                    stack.addSlice("ch"+k+" z"+j+" t"+i, proc);
                }
            }
        }

        Image5D img = new Image5D("test", stack, 2, 2, 2);
        
		img.setCurrentPosition(0,0,0,0,0);
		img.show();
        

		
	}

}
