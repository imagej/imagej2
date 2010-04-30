//
// PythonLink.java
//

import java.util.Hashtable;
import java.util.Map;

/**
 * A gateway that communicates with CellProfiler from Java, via JNI.
 * Call {@link #put} and {@link #get} to import and export variables
 * to and from the Python universe.
 */
@SuppressWarnings("serial")
public class PythonLink extends Hashtable<String, Object> {

	static {
		System.loadLibrary("PythonLink");
		initialize();
	}

	// -- Native methods --

	private static native void initialize();

	public static native void runSimpleString(String command);
	public static native void runString(String command,
		Map<String, Object> locals);

	// -- PythonLink methods --

	public void runString(String command) {
		runString(command, this);
	}

}
