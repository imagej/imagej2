import ij.IJ;
import ij.plugin.PlugIn;
/*
 * APlugIn.java
 */


/**
 *
 * @author GBH
 */
public class APlugIn_ implements ij.plugin.PlugIn {
   
   /** Creates a new instance of APlugIn */
   public APlugIn_() {
   }
   
   public void run(String arg) {
      IJ.log("This is APlugIn_ sending a log message.");
   }
   
}
