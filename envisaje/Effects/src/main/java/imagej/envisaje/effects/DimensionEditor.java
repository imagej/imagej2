/*
 * DimensionEditor.java
 *
 * Created on August 3, 2006, 10:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.effects;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Tim Boudreau
 */
public class DimensionEditor extends JComponent implements ChangeListener {
    private JSpinner wid;
    private JSpinner hi;
    private ChangeListener listener;
    public DimensionEditor() {
        SpinnerNumberModel wmdl = new SpinnerNumberModel(3, 1, 120, 1);
        SpinnerNumberModel hmdl = new SpinnerNumberModel(3, 1, 120, 1);
        wid = new JSpinner (wmdl);
        hi = new JSpinner (hmdl);
        wid.addChangeListener(this);
        hi.addChangeListener (this);

        add (wid);
        add (hi);
        setOpaque(true);
        setBackground (Color.YELLOW);
        setMinimumSize (new Dimension(100, 20));
    }

    public void doLayout() {
        Dimension hd = min (hi.getPreferredSize());
        Dimension wd = min (hi.getPreferredSize());
        int y = (getHeight() / 2) - (hd.width / 2);
        int x = getInsets().left;
        wid.setBounds (x, y, wd.width, wd.height);
        hi.setBounds (x + hd.width + 5, y, hd.width, hd.height);
    }

    private Dimension min (Dimension d) {
        d.width = Math.max (d.width, 40);
        d.height = Math.max (d.height, 40);
        return d;
    }

    public Dimension getPreferredSize() {
        Insets ins = getInsets();
        int woff = ins.left + ins.right;
        int hoff = ins.top + ins.bottom;
        Dimension hd = min (hi.getPreferredSize());
        Dimension wd = min (hi.getPreferredSize());
        Dimension result = new Dimension (
                wd.width + woff + hd.width,
                hd.height + hoff + wd.height);
        return result;
    }

    public void stateChanged(ChangeEvent e) {
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    public Dimension getDimension() {
        int w = ((Integer) wid.getValue()).intValue();
        int h = ((Integer) hi.getValue()).intValue();
        return new Dimension (w, h);
    }

    public void setChangeListener (ChangeListener l) {
        this.listener = l;
    }
}
