/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.display.zoomview;

import java.util.UUID;

/**
 *
 * @author Aivar Grislis
 */
public class Tile {
    private final String m_id;
    private final int m_index[];
    private int m_ARGB[];
    private boolean m_dirty;

    public Tile(int index[]) {
        m_id = UUID.randomUUID().toString();
        m_index = index;
        m_dirty = true;
    }

    public String getId() {
        return m_id;
    }

    public int[] getIndex() {
        return m_index;
    }

    public void setARGB(int ARGB[]) {
        m_dirty = true;
        m_ARGB = ARGB;
    }

    public int[] getARGB() {
        return m_ARGB;
    }

    public void setDirty(boolean dirty) {
        m_dirty = dirty;
    }

    public boolean isDirty() {
        return m_dirty;
    }
}
