/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.workflowpipes.modules;

import imagej.workflow.plugin.AbstractPlugin;
import imagej.workflow.plugin.IPlugin;
import imagej.workflow.plugin.ItemWrapper;
import imagej.workflow.plugin.annotations.Item;
import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;

/**
 * Plugin that takes in two strings and interleaves them as the output.
 * 
 * @author Aivar
 */
@Input({
    @Item(name=InterleavePlugin.FIRST, type=Item.Type.STRING),
    @Item(name=InterleavePlugin.SECOND, type=Item.Type.STRING)

})
@Output({
    @Item(name=Output.DEFAULT, type=Item.Type.STRING)
})
public class InterleavePlugin extends AbstractPlugin implements IPlugin {
    public static final String FIRST = "First string";
    public static final String SECOND = "Second string";

    public void process() {
        System.out.println("In InterleavePlugin");
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

