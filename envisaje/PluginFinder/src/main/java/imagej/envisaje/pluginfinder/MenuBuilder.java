package imagej.envisaje.pluginfinder;

import imagej.legacy.plugin.LegacyPluginFinder;
import imagej.plugin.PluginEntry;
import imagej.plugin.gui.ShadowMenu;
import imagej.plugin.gui.swing.JMenuCreator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;

import org.openide.windows.IOProvider;

public class MenuBuilder {

  public void addIJ1Menus(JMenu topMenu) {
    //final List<PluginEntry> entries = PluginUtils.findPlugins();
    List<PluginEntry<?>> entries = new ArrayList<PluginEntry<?>>();
    new LegacyPluginFinder().findPlugins(entries);
    StringBuilder sb = new StringBuilder();
    sb.append("");
    IOProvider.getDefault().getIO("IJ1 Plugins", false).getOut().println(
      "Discovered " + entries.size() + " plugins");
    new JMenuCreator().createMenus(new ShadowMenu(entries), topMenu);
  }

}
