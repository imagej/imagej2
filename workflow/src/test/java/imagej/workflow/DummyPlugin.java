/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import imagej.workflow.plugin.AbstractPlugin;
import imagej.workflow.plugin.IPlugin;
import imagej.workflow.plugin.ItemWrapper;
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
        ItemWrapper item1 = get();
        String string1 = (String) item1.getItem();
        String string2 = string1.toUpperCase();
        String string3 = string1.toLowerCase();
        ItemWrapper item2 = new ItemWrapper(string2);
        ItemWrapper item3 = new ItemWrapper(string3);
        put(UPPER, item2);
        put(LOWER, item3);
    }
}

