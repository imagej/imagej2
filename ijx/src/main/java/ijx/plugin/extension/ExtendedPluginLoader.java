package ijx.plugin.extension;

import ij.plugin.PlugIn;
import javax.swing.Action;

/**
 *
 * @author GBH
 */
public class ExtendedPluginLoader {
    public static void main(String[] args) {
        PlugIn testP = new MyExtendedPlugin();
        if (testP instanceof MyExtendedPlugin) {
            String label = ((MyExtendedPlugin) testP).getLabel();
            String name = ((MyExtendedPlugin) testP).getName();
            String URL = ((MyExtendedPlugin) testP).getURL();
            String description = ((MyExtendedPlugin) testP).getDescription();
            String help = ((MyExtendedPlugin) testP).getHelp();
            Action[] actions = ((MyExtendedPlugin) testP).getActions();
            System.out.println(label + ", " + name + ", " + description + ", " + URL + " ");
        }
    }
}
