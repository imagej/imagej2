/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.util.xmllight;

/**
 * XML-light Writer.
 *
 * @author Aivar Grislis
 */
public class XMLWriter {
    StringBuilder m_string;
    int m_indent = 0;

    /**
     * Constructs a helper for writing to given
     * string.
     *
     * @param string
     */
    public XMLWriter(StringBuilder string) {
        m_string = string;
    }

    /**
     * Starts a new tag.
     *
     * @param name
     */
    public void addTag(String name) {
        doIndent();
        m_string.append('<');
        m_string.append(name);
        m_string.append('>');
        m_string.append('\n');
        ++m_indent;
    }

    /**
     * Ends a tag.
     *
     * @param name
     */
    public void addEndTag(String name) {
        --m_indent;
        doIndent();
        m_string.append('<');
        m_string.append('/');
        m_string.append(name);
        m_string.append('>');
        m_string.append('\n');
    }

    /**
     * Adds a tag with some content on a single line.
     *
     * @param name
     * @param content
     */
    public void addTagWithContent(String name, String content) {
        doIndent();
        m_string.append('<');
        m_string.append(name);
        m_string.append('>');
        m_string.append(content);
        m_string.append('<');
        m_string.append('/');
        m_string.append(name);
        m_string.append('>');
        m_string.append('\n');
    }

    /**
     * Adds an embedded XML string with proper indent.
     *
     * @param output
     */
    public void add(String output) {
        String lines[] = output.split("\n");
        for (String line: lines) {
            doIndent();
            m_string.append(line);
            m_string.append('\n');
        }
    }

    /**
     * Does indentation.
     */
    private void doIndent() {
        for (int i = 0; i < 2 * m_indent; ++i) {
            m_string.append(' ');
        }
    }
}
