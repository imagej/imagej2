/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
