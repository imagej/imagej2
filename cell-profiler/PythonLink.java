//
// PythonLink.java
//

import java.util.Map;
import java.util.Hashtable;

/** A gateway that communicates with CellProfiler from Java, via JNI. */
public class PythonLink {

  static {
    System.loadLibrary("PythonLink");
    initialize();
  }

  // -- Native methods --

  private static native void initialize();

  public static native void runSimpleString(String command);
  public static native void runString(String command,
    Map<String, Object> locals);

}
