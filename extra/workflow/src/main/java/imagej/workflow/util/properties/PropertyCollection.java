//
// PropertyCollection.java
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

package imagej.workflow.util.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * Concrete class for the property collection.
 *
 * @author Aivar Grislis
 */
public class PropertyCollection implements IPropertyCollection
{
    Map<String, Object> m_properties = new HashMap();

    /**
     * Gets the entire string/object map.
     *
     * @return map
     */
    public Map<String, Object> getAll() {
        return m_properties;
    }

    /**
     * Gets a value.
     *
     * @param key string
     * @return value object
     */
    public Object get(String key) {
        return m_properties.get(key);
    }

    /**
     * Sets the entire string/object map.
     *
     * @param properties
     */
    public void setAll(Map<String, Object> properties) {
        m_properties.putAll(properties);
    }

    /**
     * Sets a value.
     *
     * @param key string
     * @param value object
     */
    public void set(String key, Object value) {
        m_properties.put(key, value);
    }

    /**
     * Clears all the properties.
     */
    public void clear() {
        try {
            m_properties.clear();
        }
        catch (UnsupportedOperationException e) {
            System.out.println("UNABLE TO CLEAR PROPERTIES");
        }
    }
}
