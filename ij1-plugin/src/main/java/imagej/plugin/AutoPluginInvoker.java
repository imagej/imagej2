//
// AutoPluginInvoker.java
//

/*
Matches and runs automatic plugins.
Copyright (C) 2010, UW-Madison LOCI

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package imagej.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilterRunner;

import imagej.ij1bridge.ImgLibImageStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mpicbg.imglib.image.Image;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;


/**
 * This class automatically invokes plugins when an image is loaded.
 *
 * Plugins should implement the IAutoPlugin interface and be labelled
 * with the "@Dimensions" annotation.  They are matched on the basis of
 * these declared dimension names.
 *
 * The IAutoDisplayPlugin interface indicates an automatic plugin also
 * displays a representation of the image.  There is no need to display
 * the default IJ representation of the image.
 *
 * @author Aivar Grislis
 */
public class AutoPluginInvoker {

    /**
     * Checks image dimensions against list of automatic plugins.
     * If there is a match, runs plugin.  If there is more than one
     * match, pops up a dialog box to choose plugin, runs selection.
     *
     * @param img image just opened
     * @return whether a plugin was automatically run
     */
   public boolean matchPlugin(ImagePlus imp) {
        boolean handled = false;
   
        // Get imglib Image from ImagePlus
        Image<?> image = null;
        ImageStack stack = imp.getStack();
        // CTR: FIXME: eliminate dependency on ij1-bridge
        if (stack instanceof ImgLibImageStack) {
            image = ((ImgLibImageStack) stack).getStorage();
        }

        // if successful
        if (null != image) {
            List<IndexItem> plugins = new ArrayList<IndexItem>();
            // get set of dimension names
            Set<String> imageSet = dimensionSet(image.getName());

            // look for matches
            for (final IndexItem<Dimensions, IAutoPlugin> item :
                    Index.load(Dimensions.class, IAutoPlugin.class, IJ.getClassLoader())) {

                // extract set of required and optional dimension names from annotation
                Set<String> requiredSet = getSet(item.annotation().required());
                Set<String> optionalSet = getSet(item.annotation().optional());

                // look for appropriate matches
                if (isAppropriate(imageSet, requiredSet, optionalSet)) {
                    plugins.add(item);
                }
            }

            // select a plugin
            IndexItem<Dimensions, IAutoPlugin> selectedPlugin = null;
            if (plugins.size() > 0) {
                if (1 == plugins.size()) {
                    // one and only match, so run it
                    selectedPlugin = plugins.get(0);
                }
                else {
                    // allow user to choose
                    String choices[] = new String[plugins.size()];
                    for (int i = 0; i < plugins.size(); ++i) {
                        choices[i] = getPluginNameFromClassName(plugins.get(i).className());
                    }
                    // throw up a dialog box
                    int index = selectPluginDialog(choices);
                    if (-1 != index) {
                        selectedPlugin = plugins.get(index);
                    }
                }
            }

            // run selected plugin
            if (null != selectedPlugin) {
                // create an instance
                IAutoPlugin instance = null;
                try {
                    instance = selectedPlugin.instance();
                }
                catch (InstantiationException e) {
                    System.out.println("Error instantiating plugin " + e.getMessage());
                }

                if (null != instance) {
                    // show the newly-loaded image if necessary
                    if (!(instance instanceof IAutoDisplayPlugin)) {
                        imp.show();
                    }

                    // run the plugin
                    ImagePlus temp = WindowManager.getTempCurrentImage();
                    WindowManager.setTempCurrentImage(imp);
                    new PlugInFilterRunner(instance, "", "");
                    WindowManager.setTempCurrentImage(temp);
;
                    handled = true;
                }
            }
        }
        return handled;
    }

    /*
     * This method parses a string of the format:
     * "Name [X Y Timebins]" and builds a set with
     * the dimensions 'X', 'Y', and 'Timebins'.
     *
     * Temporary kludge.
     */
    private Set<String> dimensionSet(String name) {
        Set<String> set = new HashSet<String>();
        int startIndex = name.indexOf('[') + 1;
        int endIndex = name.indexOf(']');
        String coded = name.substring(startIndex, endIndex);
        String dimensions[] = coded.split(" ");
        for (String dimension : dimensions) {
            set.add(dimension);
        }
        return set;
    }

    /**
     * Builds a set of strings from comma separated values
     * in an input string.
     *
     * @param commaSeparated
     * @return
     */
    private Set<String> getSet(String commaSeparated) {
        Set<String> set = new HashSet<String>();
        String[] elements = commaSeparated.split(",");
        for (String element: elements) {
            set.add(element);
        }
        return set;
    }

    /**
     * Sees whether the image's set of dimension names is appropriate
     * for the plugin's required and optional sets of dimension names.
     *
     * @param imageSet
     * @param requiredSet changed as side effect
     * @param optionalSet
     * @return whether appropriate
     */
    private boolean isAppropriate(Set<String> imageSet, Set<String> requiredSet, Set<String> optionalSet) {
        boolean match = false;
        // image must have all required dimensions
        if (imageSet.containsAll(requiredSet)) {
            requiredSet.addAll(optionalSet);
            // any additional dimensions must be optional dimensions
            if (requiredSet.containsAll(imageSet)) {
                match = true;
            }
        }
        return match;
    }

    /**
     * Given a class name builds the plugin name.
     * For example "loci.whatever.What_Ever" becomes "What Ever".
     *
     * @param class name
     * @returns plugin name
     */
    private String getPluginNameFromClassName(String className) {
        return className.substring(className.lastIndexOf('.') + 1).replace('_', ' ');
    }

    /**
     * Shows a dialog of appropriate plugins so that the user
     * can select one.
     *
     * @param choices array of plugin names
     * @return index of choice or -1 for no choice
     */

    private int selectPluginDialog(String[] choices) {
        GenericDialog dialog = new GenericDialog("Select a Plugin");
        dialog.addChoice(
            "Plugin",
            choices,
            choices[0]);

        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return -1;
        }
        return dialog.getNextChoiceIndex();
    }
}
