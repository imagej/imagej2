package imagej.plugin;

// TEMP - test plugin - should be moved to another package elsewhere

@Plugin(
  menu={
    @Menu(label="File", weight=0, mnemonic='f'),
    @Menu(label="IJ2 New...", weight=0, mnemonic='n')
  },
  accelerator="^N"
)
public class NewImage implements Runnable {

  @Parameter
  private String pixelType = "8-bit unsigned";

  @Override
  public void run() {
  	System.out.println("NewImage: pixelType = " + pixelType);//TEMP
  }

}
