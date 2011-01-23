/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow.debug;

/**
 * Contains preview information for one workflow pipe transaction.
 *
 * @author Aivar Grislis
 */
public class PreviewInfo {
    private final String m_instanceId;
    private final String m_desc;
    private final String m_html;

    /**
     * Creates preview information instance.
     *
     * @param instanceId unique identifier for associated workflow component
     * @param desc description of this pipe transaction
     * @param html HTML page for preview
     */
    public PreviewInfo(String instanceId, String desc, String html) {
        m_instanceId = instanceId;
        m_desc = desc;
        m_html = html;
    }

    /**
     * Get unique identifier for associated workflow componenet instance.
     *
     * @return instance identifier
     */
    public String getInstanceId() {
        return m_instanceId;
    }

    /**
     * Get description of this pipe transaction.
     *
     * @return description
     */
    public String getDesc() {
        return m_desc;
    }

    /**
     * Get HTML page for preview.
     *
     * @return HTML page
     */
    public String getHtml() {
        return m_html;
    }
}
