/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.util.xmllight;

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
