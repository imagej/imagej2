//
// SwingMenuCreator.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
 * Neither the names of the ImageJDev.org developers nor the
names of its contributors may be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
 */
package imagej.envisaje.ijpluginsloader;

import imagej.ImageJ;
import imagej.envisaje.utils.output.DialogUtil;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginException;
import imagej.plugin.PluginManager;
import imagej.plugin.RunnablePlugin;
import imagej.plugin.ui.ShadowMenu;
import imagej.util.Log;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.openide.windows.IOProvider;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public class NBMenuCreator {

    public NBMenuCreator() {
    }

    public void createMenus(final ShadowMenu root) { //, final JMenu target) {
        double lastWeight = Double.NaN;
        for (final ShadowMenu child : root.getChildren()) {
            final double weight = child.getMenuEntry().getWeight();
            final double difference = Math.abs(weight - lastWeight);
            if (difference > 1) {
                // addSeparatorToTop(target);
            }
            lastWeight = weight;
            if (child.isLeaf()) {
                out("Top isLeaf: " + child.toString() + " NOT GOOD!");
                // addLeafToTop(child, target);
            } else {
                if (ActionInstaller.checkTopLevelMenu(child.toString())) {
                    out("TopLevelMenu: " + child.toString());
                } else {
                    DialogUtil.error("FAILED to find TopLevelMenu: " + child.toString());
                }
                String topMenu = child.toString();
                populateMenu(child, topMenu);
            }
        }
    }

    private void populateMenu(final ShadowMenu shadow, final String target) {
        double lastWeight = Double.NaN;
        for (final ShadowMenu child : shadow.getChildren()) {
            final double weight = child.getMenuEntry().getWeight();
            final double difference = Math.abs(weight - lastWeight);
            if (difference > 1) {
                addSeparatorToMenu(target);
            }
            lastWeight = weight;
            if (child.isLeaf()) {
                addLeafToMenu(child, target);
            } else {
                //final JMenu nonLeaf = 
                final String nonLeaf = addNonLeafToMenu(child, target);
                populateMenu(child, nonLeaf);
            }
        }
    }
//    private void populateMenu(final ShadowMenu shadow, final JMenu target) {
//        double lastWeight = Double.NaN;
//        for (final ShadowMenu child : shadow.getChildren()) {
//            final double weight = child.getMenuEntry().getWeight();
//            final double difference = Math.abs(weight - lastWeight);
//            if (difference > 1) {
//                addSeparatorToMenu(target);
//            }
//            lastWeight = weight;
//            if (child.isLeaf()) {
//                addLeafToMenu(child, target);
//            } else {
//                final JMenu nonLeaf = addNonLeafToMenu(child, target);
//                populateMenu(child, nonLeaf);
//            }
//        }
//    }

    private void out(String s) {
        IOProvider.getDefault().getIO("IJPlugins", false).getOut().println(s);
    }

    protected String addNonLeafToMenu(final ShadowMenu shadow, final String target) {
        return target + "/" + shadow.toString();
    }

    protected void addLeafToMenu(final ShadowMenu shadow, final String target) {
        out("addLeafToMenu: " + target + " : " + shadow.toString());
        Action action = createAction(target, shadow);
        assignProperties(action, shadow);
        ActionInstaller.registerDynamicAction(
                shadow.getMenuEntry().getName(),
                action, target, "");
        //String name, Action theAction, String menuPath, String shortcut) 
    }

    protected void addSeparatorToMenu(final String target) {
        out("addSeparatorToMenu: " + target);
    }

//    protected void addSeparatorToMenu(final JMenu target) {
//        target.addSeparator();
//    }
    /////////////////////
//    protected JMenuItem createLeaf(final ShadowMenu shadow) {
//        final JMenuItem menuItem = new JMenuItem(shadow.getMenuEntry().getName());
//        linkAction(shadow.getPluginEntry(), menuItem);
//        assignProperties(menuItem, shadow);
//        return menuItem;
//    }

//    protected JMenu createNonLeaf(final ShadowMenu shadow) {
//        final JMenu menu = new JMenu();
//        //final JMenu menu = new JMenu(shadow.getMenuEntry().getName());
//        assignProperties((JMenuItem) menu, shadow);
//        return menu;
//    }

    // -- Helper methods --
    private KeyStroke getKeyStroke(final ShadowMenu shadow) {
        String accelerator = shadow.getMenuEntry().getAccelerator();
        if (accelerator != null) {
            // allow use of ^X to represent control X in keyboard accel parameters
            // NB: extra space REQUIRED
            accelerator = accelerator.replaceAll(Pattern.quote("^"), "control ");
            // on Mac, use Command instead of Control for keyboard shortcuts
            if (isMac() && accelerator.indexOf("meta") < 0) {
                // only if meta not already in use
                accelerator = accelerator.replaceAll("control", "meta");
            }
        }
        return KeyStroke.getKeyStroke(accelerator);
    }

    private ImageIcon loadIcon(final ShadowMenu shadow) {
        final String iconPath = shadow.getMenuEntry().getIcon();
        if (iconPath == null || iconPath.isEmpty()) {
            return null;
        }
        try {
            final Class<?> c = shadow.getPluginEntry().loadClass();
            final URL iconURL = c.getResource(iconPath);
            if (iconURL == null) {
                return null;
            }
            return new ImageIcon(iconURL);
        } catch (final PluginException e) {
            Log.error("Could not load icon: " + iconPath, e);
        }
        return null;
    }

    private void assignProperties(final Action action, final ShadowMenu shadow) {
                //action.putValue(Action.NAME, );
        final char mnemonic = shadow.getMenuEntry().getMnemonic();
        if (mnemonic != '\0') {
             action.putValue(Action.MNEMONIC_KEY, mnemonic);
        }
        final KeyStroke keyStroke = getKeyStroke(shadow);
        if (keyStroke != null) {
             action.putValue(Action.ACCELERATOR_KEY, keyStroke);
        }

        final ImageIcon icon = loadIcon(shadow);
        if (icon != null) {
            action.putValue(Action.SMALL_ICON, icon);
        }
        // action.putValue(Action.SHORT_DESCRIPTION, item.annotation().tip());
    }

            

    protected Action createAction(String target, ShadowMenu shadow) {
        final PluginEntry<?> entry = shadow.getPluginEntry();
        Action action = new AbstractAction(shadow.getMenuEntry().getName()) {

            @Override
            public void actionPerformed(final ActionEvent e) {
                // TODO - find better solution for typing here
                @SuppressWarnings("unchecked")
                final PluginEntry<? extends RunnablePlugin> runnableEntry =
                        (PluginEntry<? extends RunnablePlugin>) entry;
                final PluginManager pluginManager = ImageJ.get(PluginManager.class);
                pluginManager.run(runnableEntry);
            }
        };
        return action;
    }

    private boolean isMac() {
        return System.getProperty("os.name").startsWith("Mac");
    }
}
