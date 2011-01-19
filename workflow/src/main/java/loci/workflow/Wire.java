/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 * Data structure that describes a chained connection.
 *
 * @author Aivar Grislis
 */
public class Wire {
    final IModule m_source;
    final String m_sourceName;
    final IModule m_dest;
    final String m_destName;

    Wire(IModule source, String sourceName, IModule dest, String destName) {
        m_source = source;
        m_sourceName = sourceName;
        m_dest = dest;
        m_destName = destName;
    }

    IModule getSource() {
        return m_source;
    }

    String getSourceName() {
        return m_sourceName;
    }

    IModule getDest() {
        return m_dest;
    }

    String getDestName() {
        return m_destName;
    }
}

