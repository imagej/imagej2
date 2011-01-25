/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import imagej.workflow.plugin.AbstractPlugin;
import imagej.workflow.plugin.IPlugin;
import imagej.workflow.plugin.annotations.Item;
import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;

/**
 *
 * @author aivar
 */
@Input
@Output({
    @Item(name = DummyPlugin.UPPER, type=Item.Type.ITEM),
    @Item(name = DummyPlugin.LOWER, type=Item.Type.ITEM)
})
public class DummyPlugin extends AbstractPlugin implements IPlugin {
    static final String UPPER = "UPPER";
    static final String LOWER = "LOWER";

    public void process() {
        System.out.println("in TestPlugin");
        String item1 = (String) get();
        String string1 = item1;
        String string2 = string1.toUpperCase();
        String string3 = string1.toLowerCase();
        put(UPPER, string2);
        put(LOWER, string3);
    }
}

