package ij.plugin;
import ij.*;
import java.awt.*;

/** Implements the Plugins/Utilities/Capture Screen command. */
public class ScreenGrabber implements PlugIn {
    
    public void run(String arg) {
        try {
            Robot robot = new Robot();
             Dimension dimension = IJ.getScreenSize();
            Rectangle r = new Rectangle(dimension);
            Image img = robot.createScreenCapture(r);
            if (img!=null)
                    IJ.getFactory().newImagePlus("Screen", img).show();
        } catch(Exception e) {}
    }

}

