package ijx.plugin;

import java.util.ArrayList;
import java.util.List;

import imagej.plugin.PluginEntry;
import org.openide.util.Lookup;

/* PluginFinder
 
This is an InjectableSingleton...

Alternative Implementation of IjxPluginFinder:

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=IjxPluginFinder.class)
public class AlternateFinder extends IjxPluginFinder {
  public AlternateFinder() {}
   @Override
   public void void findPlugins(List<PluginEntry> plugins) {
    System.out.println("AlternateMyService doing something...");
  }
}
*/


public abstract class IjxPluginFinder {

  public static IjxPluginFinder getDefault() {
    IjxPluginFinder result = Lookup.getDefault().lookup(IjxPluginFinder.class);
    return result != null ? result : new DefaultImplementation();
  }

  public abstract void findPlugins(List<PluginEntry> plugins);

  //
  private static class DefaultImplementation extends IjxPluginFinder {

    public DefaultImplementation() {
    }

    @Override
    public void findPlugins(List<PluginEntry> plugins) {
      //
      // TODO -- add real implementation here
      //
      String pluginClass = "ijx.plugin.FooBar";
      String parentMenu = "Foo";
      String label = "Bar";
      PluginEntry entry = new PluginEntry(pluginClass, parentMenu, label);
      plugins.add(entry);
    }
  }

  /**
   * Tests the IJX plugin discovery mechanism,
   * printing a list of all discovered plugins.
   */
  public static void main(String[] args) {
    System.out.println("Finding plugins...");
    List<PluginEntry> plugins = new ArrayList<PluginEntry>();
    IjxPluginFinder.getDefault().findPlugins(plugins);
    System.out.println("Discovered plugins:");
    for (PluginEntry plugin : plugins) {
      System.out.println("\t" + plugin);
    }
    System.exit(0);
  }
}
