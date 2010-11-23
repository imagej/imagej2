package ijx.macro;

public interface MacroExtension {
  public static final int ARG_STRING = 0x01;
  public static final int ARG_NUMBER = 0x02;
  public static final int ARG_ARRAY  = 0x04;
  
  public static final int ARG_OUTPUT = 0x10;
  public static final int ARG_OPTIONAL = 0x20;

  public String handleExtension(String name, Object[] args);
  
  public ExtensionDescriptor[] getExtensionFunctions();
}
