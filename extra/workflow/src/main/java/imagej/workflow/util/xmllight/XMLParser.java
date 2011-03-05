//
// XMLParser.java
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
 * XML-light Parser.
 *
 * @author Aivar Grislis
 */
public class XMLParser {

    /**
     * Parses the XML string and returns the XMLTag data structure for the first
     * tag it encounters.
     *
     * For "<one>abc</one><two>def</two>", XMLTag.name is "one", XMLTag.content
     * is "abc", and XMLTag.remainder is "<two>def</two>".
     *
     * @param inclusive
     * @param xml
     * @return
     * @throws XMLException
     */
    public XMLTag getNextTag(String xml) throws XMLException {
        xml = xml.trim();
        if (xml.isEmpty()) {
            return new XMLTag();
        }
        if (!xml.startsWith("<") || !xml.endsWith(">")) {
            throw new XMLException("Mismatched '<' '>'");
        }
        try {
            int endBracketIndex = xml.indexOf('>');
            int startContentIndex = endBracketIndex + 1;

            String name = xml.substring(1, endBracketIndex);

            String endTag = "</" + name + ">";
            int endTagIndex = xml.indexOf(endTag);
            if (-1 == endTagIndex) {
                throw new XMLException("Missing " + endTag);
            }
            int remainderIndex = endTagIndex + endTag.length();

            String content = xml.substring(startContentIndex, endTagIndex);

            String remainder = xml.substring(remainderIndex, xml.length());

            return new XMLTag(name.trim(), content.trim(), remainder.trim());
        }
        catch (IndexOutOfBoundsException e) {
            throw new XMLException("Improper XML");
        }
    }
}
