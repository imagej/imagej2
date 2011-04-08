/*
 * AggregateCustomizer.java
 *
 * Created on September 30, 2006, 2:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.toolcustomizers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import imagej.envisaje.spi.tools.Customizer;

/**
 *
 * @author Tim Boudreau
 */
public class AggregateCustomizer <T extends Object> implements Customizer<T> {
    protected final Customizer[] merge;
    private final String name;
    /** Creates a new instance of AggregateCustomizer */
    public AggregateCustomizer(String name, Customizer... merge) {
        this.merge = merge;
        this.name = name;
    }

    public JComponent getComponent() {
        JPanel jp = new JPanel();
        jp.setLayout (new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        for (Customizer c : merge) {
            if (c != null) {
                JComponent comp = c.getComponent();
                if (comp != null) {
                    gbc.gridy++;
                    jp.add (comp, gbc);
                }
            }
        }
        return jp;
    }

    public final String getName() {
        return name;
    }

    public T get() {
        return null;
    }
}
