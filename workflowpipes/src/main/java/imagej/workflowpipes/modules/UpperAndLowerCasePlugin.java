/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflowpipes.modules;

import imagej.workflow.plugin.AbstractPlugin;
import imagej.workflow.plugin.IPlugin;
import imagej.workflow.plugin.ItemWrapper;
import imagej.workflow.plugin.annotations.Img;
import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;

/**
 * Dummy plugin that takes in a string and creates upper and lower case
 * versions as outputs.
 * 
 * @author aivar
 */
@Input
@Output({ @Img(UpperAndLowerCasePlugin.UPPER), @Img(UpperAndLowerCasePlugin.LOWER) })
public class UpperAndLowerCasePlugin extends AbstractPlugin implements IPlugin {
    static final String UPPER = "UPPER";
    static final String LOWER = "LOWER";

    public void process() {
        System.out.println("in UpperAndLowerCasePlugin");
        ItemWrapper item1 = get();
        String string1 = (String) item1.getItem();
        String string2 = string1.toUpperCase();
        String string3 = string1.toLowerCase();
        ItemWrapper item2 = new ItemWrapper(string2);
        ItemWrapper item3 = new ItemWrapper(string3);
        System.out.println("UPPER is " + item2);
        put(UPPER, item2);
        System.out.println("LOWER is " + item3);
        put(LOWER, item3);
    }
}

