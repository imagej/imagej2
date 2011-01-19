/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modules;

import loci.workflow.plugin.AbstractPlugin;
import loci.workflow.plugin.IPlugin;
import loci.workflow.plugin.ItemWrapper;
import loci.plugin.annotations.Img;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

/**
 *
 * @author aivar
 */
@Input({ @Img(TestPlugin2.FIRST), @Img(TestPlugin2.SECOND) } )
@Output
public class TestPlugin2 extends AbstractPlugin implements IPlugin {
    static final String FIRST = "FIRST";
    static final String SECOND = "SECOND";

    public void process() {
        System.out.println("In TestPlugin2");
        ItemWrapper item1 = get(FIRST);
        ItemWrapper item2 = get(SECOND);
        String combinedString = interleave((String) item1.getItem(), (String) item2.getItem());
        ItemWrapper item3 = new ItemWrapper(combinedString);
        put(item3);
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

