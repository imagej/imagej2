//
// XMLTag.java
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

package imagej.workflow.util.xmllight;

/**
 * Tag data structure returned when parsing.
 *
 * @author Aivar Grislis
 */
public class XMLTag {
    public static final String EMPTY_STRING = "";
    private String m_name;
    private String m_content;
    private String m_remainder;

    /**
     * Creates an empty tag.
     */
    public XMLTag() {
        m_name = EMPTY_STRING;
        m_content = EMPTY_STRING;
        m_remainder = EMPTY_STRING;
    }

    /**
     * Creates a given tag.
     *
     * @param name
     * @param content
     * @param remainder
     */
    public XMLTag(String name, String content, String remainder) {
        m_name = name;
        m_content = content;
        m_remainder = remainder;
    }

    /**
     * Get the name of the tag.
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets the content of the tag.
     *
     * @return
     */
    public String getContent() {
        return m_content;
    }

    /**
     * Gets the remainder of the XML string after the tag.
     *
     * @return
     */
    public String getRemainder() {
        return m_remainder;
    }
}
