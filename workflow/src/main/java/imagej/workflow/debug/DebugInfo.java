/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow.debug;

import imagej.workflow.plugin.ItemWrapper;

/**
 * Contains debugging information for one workflow pipe transaction.
 *
 * @author Aivar Grislis
 */
public class DebugInfo {
    private final String m_instanceId;
    private final String m_desc;
    private final ItemWrapper m_itemWrapper;

    /**
     * Creates debugging information instance.
     *
     * @param instanceId unique identifier for associated workflow component
     * @param desc description of this pipe transaction
     * @param itemWrapper item being piped
     */
    public DebugInfo(String instanceId, String desc, ItemWrapper itemWrapper) {
        m_instanceId = instanceId;
        m_desc = desc;
        m_itemWrapper = itemWrapper;
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
     * Get item being piped.
     *
     * @return item
     */
    public ItemWrapper getItemWrapper() {
        return m_itemWrapper;
    }
}
