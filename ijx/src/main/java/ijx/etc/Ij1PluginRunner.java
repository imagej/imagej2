package ijx.etc;

import ij.IJ;
import ij.ImageJ;

/**
 *
 * @author GBH
 */
public class Ij1PluginRunner {

    public static void main(String[] args) {
        runPlugin("PluginClass");
    }
    public static boolean runPlugin(String plugin) {
        if (IJ.getInstance() == null) {
            new ImageJ(ImageJ.NO_SHOW);
            if (IJ.getInstance() == null) {
                System.err.println("ImageJ failed to start.");
                return false;
            } else {
                IJ.showMessage("OK -- ImageJ Started.");
            }
        }
        // create the ImagePlus
        // ?? How do we handle the need for multiple image selections ??
        //    We would need to mirror the open images in IJ2 to the IJ1 WindowManager, no?
        
        // Set as the current image in ImageJ
        IJ.getImage();
        return true;
    }
}
