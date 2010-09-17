import ij.*;
import ij.plugin.*;

/*
 * Created on 29.05.2005
 */

/** Opens a series of images or image stacks and converts it to an Image5D. 
 * Calls first the HyperVolumeOpener plugin, then the Stack_to_Image5D plugin.
 * 
 * @author Joachim Walter
 */
public class Open_Series_As_Image5D implements PlugIn {

	public void run(String arg) {
	    if (IJ.versionLessThan("1.34p")) return;
        
        ImagePlus imp1 = WindowManager.getCurrentImage();
        int id=0;
        if (imp1!=null)
            id = imp1.getID();
	    
        Hypervolume_Opener h = new Hypervolume_Opener();
	    h.run("");
	    
        // If no new image opened, return.
        ImagePlus imp2 = WindowManager.getCurrentImage();
        if (imp2==null || imp2.getID()==id)
            return;
        
	    Stack_to_Image5D s = new Stack_to_Image5D();
	    s.run("");
	}
}