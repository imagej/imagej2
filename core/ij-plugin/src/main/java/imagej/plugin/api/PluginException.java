package imagej.plugin.api;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class PluginException extends Exception {

  public PluginException() { super(); }
  public PluginException(String s) { super(s); }
  public PluginException(String s, Throwable cause) { super(s, cause); }
  public PluginException(Throwable cause) { super(cause); }

}
