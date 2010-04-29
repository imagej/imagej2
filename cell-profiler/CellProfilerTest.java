//
// CellProfilerTest.java
//

import java.util.Hashtable;
import java.util.Map;

/** A simple test of CellProfiler-Java connectivity. */
public class CellProfilerTest {

  // -- Main method --

  public static void main(String[] args) {
    // build command from arguments
    StringBuilder sb = new StringBuilder();
    for (String arg : args) {
      sb.append(arg);
      sb.append(" ");
    }
    //sb.append(System.getProperty("line.separator"));
    String cmd = sb.toString();

    System.out.println("Executing command: " + cmd);

    PythonLink link = new PythonLink();

    link.runSimpleString(cmd);

    Map locals = new Hashtable();
    Map x = new Hashtable();
    x.put("Hello","There");
    locals.put("x", x);
    cmd = "print 'Hello'";
    link.runString(cmd, locals);
    sb = new StringBuilder();
    sb.append("print 'Running script...'\n");
    sb.append("import traceback\n");
    sb.append("try:\n");
    sb.append("  import cellprofiler.utilities.jutil as J\n");
    sb.append("  d = J.get_dictionary_wrapper(x)\n");
    sb.append("  hello = d.get('Hello')\n");
    sb.append("  print J.to_string(hello)\n");
    sb.append("  d.put('Hello','World')\n");
    sb.append("except:\n");
    sb.append("  print 'so sorry, caught exception'\n");
    sb.append("  traceback.print_exc()\n");
    cmd = sb.toString();

    System.out.println("Executing command: \n" + cmd);
    link.runString(cmd, locals);
    System.out.println("x.get(Hello) = " + x.get("Hello"));
    System.out.println("Did not segfault! Done!");
  }

}
