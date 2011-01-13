/*
 * BooleanCustomizer.java
 *
 * Created on September 30, 2006, 12:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.toolcustomizers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import org.imagejdev.imagine.api.toolcustomizers.AbstractCustomizer;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tim Boudreau
 */
public class BooleanCustomizer extends AbstractCustomizer <Boolean> implements ActionListener {
    public BooleanCustomizer(String name) {
        super (name);
    }

    public Boolean getValue() {
        return Boolean.valueOf(((JCheckBox) getComponents()[0]).isSelected());
    }

    public boolean isValue() {
        return getValue().booleanValue();
    }

    protected JComponent[] createComponents() {
        JCheckBox cbox = new JCheckBox (getName());
        cbox.setSelected(NbPreferences.forModule(getClass()).getBoolean(getName(), true));
        cbox.addActionListener (this);
        return new JComponent[] { cbox };
    }

    public void actionPerformed(ActionEvent e) {
        change();
    }

    @Override
    protected void saveValue(Boolean value) {
        NbPreferences.forModule(getClass()).putBoolean(getName(), value);
    }
}
