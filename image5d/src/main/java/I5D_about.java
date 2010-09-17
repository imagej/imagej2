import ij.IJ;
import ij.plugin.PlugIn;

import i5d.*;


public class I5D_about implements PlugIn {

    public void run(String arg) {
        IJ.showMessage("Image5D "+Image5D.VERSION,
            "Viewing and handling 5D (x/y/channel/z/time) image-data.\n" +
            "Author: Joachim Walter");
        
    }

}
