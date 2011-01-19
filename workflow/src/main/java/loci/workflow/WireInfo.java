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
public class WireInfo {
    final String m_sourceModuleName;
    final String m_sourceName;
    final String m_destModuleName;
    final String m_destName;

    WireInfo(String sourceModuleName, String sourceName, String destModuleName, String destName) {
        m_sourceModuleName = sourceModuleName;
        m_sourceName = sourceName;
        m_destModuleName = destModuleName;
        m_destName = destName;
    }

    String getSourceModuleName() {
        return m_sourceModuleName;
    }

    String getSourceName() {
        return m_sourceName;
    }

    String getDestModuleName() {
        return m_destModuleName;
    }

    String getDestName() {
        return m_destName;
    }
}
