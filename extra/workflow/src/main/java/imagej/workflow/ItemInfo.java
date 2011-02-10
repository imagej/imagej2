/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import java.io.Serializable;

/**
 *
 * @author Aivar Grislis
 */
public class ItemInfo implements IItemInfo, Serializable {
    private final String m_name;
    private final Type m_type;
    private final Object m_default;

    public ItemInfo(String name, Type type, Object value) {
        m_name = name;
        m_type = type;
        m_default = value;
    }

    public String getName() {
        return m_name;
    }

    public Type getType() {
        return m_type;
    }

    public Object getDefault() {
        return m_default;
    }
}
