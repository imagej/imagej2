//
// DummyPlugin2.java
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

