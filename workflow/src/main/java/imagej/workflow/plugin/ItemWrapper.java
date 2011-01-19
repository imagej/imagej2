//
// ItemWrapper.java
//

/*
Framework for chaining processors together; one input and one output.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
  * Neither the name of the UW-Madison LOCI nor the
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

package imagej.workflow.plugin;

import imagej.workflow.util.properties.IPropertyCollection;
import imagej.workflow.util.properties.PropertyCollection;

/**
 * Wrapper for wired data item.
 *
 * This associates a set of String/Object property pairs with the image.  Also
 * keeps track of width and height.
 *
 * Note that ImagePlus keeps a set Java Properties and lots of ImageJ image
 * classes can give you width and height.  I exposed these things in this
 * interface as they are the most important things about images for this demo
 * implementation.
 *
 * @author Aivar Grislis
 */
public class ItemWrapper
{
    private final PropertyCollection m_properties = new PropertyCollection();
    private final Object m_item;

    /**
     * Creates an ItemWrapper based on an ImageJ ImageProcessor.
     *
     * @param imageProcessor
     */
    public ItemWrapper(Object item)
    {
        m_item = item;
    }

    /**
     * Creates an ItemWrapper based on another ItemWrapper.
     *
     * @param other
     */
    public ItemWrapper(ItemWrapper other) {
        m_item = other.getItem();
        m_properties.setAll(other.getProperties().getAll());
    }

    /**
     * Gets the underlying ImageJ ImageProcessor.
     *
     * @return ImageProcessor
     */
    public Object getItem() {
        return m_item;
    }

    /**
     * Gets the properties associated with this image.
     *
     * @return properties
     */
    public IPropertyCollection getProperties() {
        return m_properties;
    }
}