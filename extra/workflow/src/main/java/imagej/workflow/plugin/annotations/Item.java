//
// Item.java
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

package imagej.workflow.plugin.annotations;

import java.lang.annotation.*;

/**
 * Annotation for a plugin input or output item.
 * <p>
 * Example:
 *   @Item(name="First Name", type=Item.STRING)
 *     uses default value
 *   @Item(name="Last Name", type=Item.STRING, string="Jones")
 *     supplies default value of "Jones"
 *
 * @author Aivar Grislis
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Item {
    public enum Type {
        STRING, INTEGER, FLOATING, URL, IMAGE, ITEM
    }
    public String CURRENT_IMAGE = "<current image>";

    //TODO the following would allow us to define an item of Item.Type.ITEM using
    // @Item("Name of item")
    // which might be useful.
    //   String value();
    //   Type type() default Type.ITEM;
    // However we get the spurious "incompatible types" message,
    //   "found: imagej.workflow.plugin.annotations.Item.Type
    //    required: imagej.workflow.plugin.annotations.Item.Type"

    String name();
    Type type();
    String string() default "";
    int integer() default 1;
    double floating() default 1.0;
    String url() default "";
    String image() default CURRENT_IMAGE;
}

/*
 * @Input( {
 *     @Item(
 *         name = Thingy.BLUE,
 *         type = Item.STRING,
 *         string = ""),
 *
 */
