/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.ijpluginsloader;

import imagej.ImageJ;
import imagej.envisaje.utils.output.DialogUtil;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginManager;
import imagej.plugin.ui.ShadowMenu;

import java.util.List;

import org.openide.modules.ModuleInstall;
import org.openide.windows.IOProvider;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        //	private void createMenuBar() {
        //final List<PluginEntry<?>> entries = ImageJPluginFinder.findPlugins();
        //DialogUtil.info("Discovered " + entries.size() + " plugins");
        DialogUtil.plain("In Installed of Plugins...");
        ImageJ.initialize();
        createMenus();
//		final ShadowMenu rootMenu = new ShadowMenu(entries);
//		final JMenuBar menuBar = new JMenuBar();
//		new JMenuBarCreator().createMenus(rootMenu, menuBar);

//		frame.setJMenuBar(menuBar);
        // }
    }

    private void createMenus() {
        final PluginManager pluginManager = ImageJ.get(PluginManager.class);
        final List<PluginEntry<?>> entries = pluginManager.getPlugins();
        final ShadowMenu rootMenu = new ShadowMenu(entries);
        NBMenuCreator creator = new NBMenuCreator();
        creator.createMenus(rootMenu);
        // dumpShadow(rootMenu, 0);
        
        
        //final JMenuBar menuBar = new JMenuBar();
        //new NBMenuCreator().createMenus(rootMenu);
        //frame.setJMenuBar(menuBar);
        //Events.publish(new AppMenusCreatedEvent(menuBar));



    }

    private void dumpShadow(ShadowMenu shadow, int level) {
        double lastWeight = Double.NaN;
        level++;
        for (final ShadowMenu child : shadow.getChildren()) {
            final double weight = child.getMenuEntry().getWeight();
            final double difference = Math.abs(weight - lastWeight);
            lastWeight = weight;
            if (child.isLeaf()) {
                out("{" + level + "} " + child.getMenuEntry().getName());
            } else {
                out("Menu {" + level + "} " + child.getMenuEntry().getName());
                dumpShadow(child, level);
            }
        }
    }

    private void out(String s) {
        IOProvider.getDefault().getIO("IJPlugins", false).getOut().println(s);
    }
}
