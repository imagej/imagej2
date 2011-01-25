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
//TODO old style: @Input({ @Img(DummyPlugin2.FIRST), @Img(DummyPlugin2.SECOND) } )
@Input({
    @Item(name = DummyPlugin2.FIRST, type = Item.Type.ITEM),
    @Item(name = DummyPlugin2.SECOND, type = Item.Type.ITEM)
})
@Output
public class DummyPlugin2 extends AbstractPlugin implements IPlugin {
    static final String FIRST = "FIRST";
    static final String SECOND = "SECOND";

    public void process() {
        System.out.println("In TestPlugin2");
        String item1 = (String) get(FIRST);
        String item2 = (String) get(SECOND);
        String combinedString = interleave(item1, item2);
        put(combinedString);
        System.out.println("OUTPUT IS " + combinedString);
    }

    public String interleave(String string1, String string2) {
        String returnValue = "";
        int maxLength = Math.max(string1.length(), string2.length());
        for (int i = 0; i < maxLength; ++i) {
            if (i < string1.length()) {
                returnValue += string1.charAt(i);
            }
            if (i < string2.length()) {
                returnValue += string2.charAt(i);
            }
        }
        return returnValue;
    }
}

