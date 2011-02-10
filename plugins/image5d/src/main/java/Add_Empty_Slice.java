import i5d.Image5D;
import i5d.gui.Image5DWindow;
import ij.*;
import ij.plugin.*;
/*
 * Created on 09.08.2005
 *
 */

/**
 * @author Joachim Walter
 *
 */
public class Add_Empty_Slice implements PlugIn {
    
     public void run(String arg) {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (! (imp instanceof Image5D)) {
            IJ.error("Current Image is not an Image5D.");
            return;
        }
        IJ.register(Add_Empty_Slice.class);
        
        Image5D i5d = (Image5D)imp;
        Image5DWindow win = ((Image5DWindow)i5d.getWindow());
        int n = i5d.getNSlices();
        
        i5d.expandDimension(3, n+1, true);        
		
        win.updateSliceSelector();
    }

}
