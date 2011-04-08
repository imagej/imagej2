/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.misccomponents;

import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;

/**
 * Implementation of SharedLayoutData, used by LDPLayout.  Primary use case:
 * Create a panel that implements SharedLayoutData, and delegate to an instance
 * of this class.
 *
 * @author Tim Boudreau
 */
public final class DefaultSharedLayoutData implements SharedLayoutData {
    private Set <LayoutDataProvider> known = new HashSet<LayoutDataProvider>();

    public void register(LayoutDataProvider p) {
        known.add (p);
        for (LayoutDataProvider d : known) {
            if (d instanceof JComponent) {
                ((JComponent) d).invalidate();
                ((JComponent) d).revalidate();
                ((JComponent) d).repaint();
            }
        }
    }
    
    public void unregister (LayoutDataProvider p) {
        known.remove (p);
        for (LayoutDataProvider d : known) {
            if (d instanceof JComponent) {
                ((JComponent) d).invalidate();
                ((JComponent) d).revalidate();
                ((JComponent) d).repaint();
            }
        }
    }

    public int xPosForColumn(int column) {
        int xpos = 0;
        for (LayoutDataProvider l : known) {
            int colpos = l.getColumnPosition(column);
            System.err.println(l + " says " + colpos + " for " + column);
            xpos = Math.max (colpos, xpos);
        }
        System.err.println("XposForColumn " + xpos + " = " + xpos + " :: " + known.size() + " components: \n   " + known);
        return xpos;
    }

    public void expanded(LayoutDataProvider p, boolean state) {
        Set <LayoutDataProvider> iter = new HashSet<LayoutDataProvider>(known);
        if (state) {
            for (LayoutDataProvider d : iter) {
                if (d != p) {
                    if (d.isExpanded()) {
                        d.doSetExpanded(false);
                    }
                } else {
                    if (d instanceof JComponent) {
                        JComponent jc = (JComponent) d;
                        jc.invalidate();
                        jc.revalidate();
                        jc.repaint();
                    }
                }
            }
        }
    }
}
