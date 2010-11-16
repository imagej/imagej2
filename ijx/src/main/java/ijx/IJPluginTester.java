package ijx;


import ij.IJ;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/*
 * A way to test plugins quickly...
 *
 * @author GBH
 */
public class IJPluginTester {

    // Put the test images to load in this list (array of Strings)
    final static String[] imagesToLoad = {
        "http://rsb.info.nih.gov/ij/images/blobs.gif",
        "http://rsb.info.nih.gov/ij/images/Cell_Colony.jpg"
    };
    // Put the name of the Plugin here...
    final static String className =
            //"algorithmLauncher.AlgorithmLauncher";
            "imagej.plugin.GaussianConvolutionFilter";

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ImageJX.main(null);  // launch ImageJ
                // Load Image(s)...
                for (int i = 0; i < imagesToLoad.length; i++) {
                    String img = imagesToLoad[i];
                    IjxImagePlus imp = IJ.openImage(img);
                    imp.show();
                }
                runUserPlugIn("Test", className, "", false);

//                final String arg = "";
//                Object thePlugIn = null;
//                Class c;
//                try {
//                    c = Class.forName(className);
//                    try {
//                        thePlugIn = c.newInstance();
//                    } catch (InstantiationException ex) {
//                        Logger.getLogger(IJPluginTester.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (IllegalAccessException ex) {
//                        Logger.getLogger(IJPluginTester.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    if (thePlugIn instanceof PlugIn) {
//                        ((PlugIn) thePlugIn).run(arg);
//                    }
//                } catch (ClassNotFoundException ex) {
//                    Logger.getLogger(IJPluginTester.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        });
    }

    static Object runUserPlugIn(String commandName, String className, String arg, boolean createNewLoader) {

        ClassLoader loader = IJ.class.getClassLoader();
        Object thePlugIn = null;
        try {
            thePlugIn = (loader.loadClass(className)).newInstance();
            if (thePlugIn instanceof PlugIn) {
                ((PlugIn) thePlugIn).run(arg);
            } else if (thePlugIn instanceof PlugInFilter) {
                new PlugInFilterRunner(thePlugIn, commandName, arg);
            }
        } catch (ClassNotFoundException e) {
            //	if (className.indexOf('_')!=-1 && !suppressPluginNotFoundError)
            //error("Plugin or class not found: \"" + className + "\"\n(" + e+")");
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            int dotIndex = className.indexOf('.');
            if (dotIndex >= 0) {
                return runUserPlugIn(commandName, className.substring(dotIndex + 1), arg, createNewLoader);
            }
//			if (className.indexOf('_')!=-1 && !suppressPluginNotFoundError)
//				error("Plugin or class not found: \"" + className + "\"\n(" + e+")");
        } catch (InstantiationException e) {
            //error("Unable to load plugin (ins)");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            //error("Unable to load plugin, possibly \nbecause it is not public.");
        }
//		if (redirectErrorMessages && !"HandleExtraFileTypes".equals(className))
// 			redirectErrorMessages = false;
//		suppressPluginNotFoundError = false;
        return thePlugIn;
    }
}
