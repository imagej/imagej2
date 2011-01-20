/*
 * TextCustomizer.java
 *
 * Created on September 30, 2006, 2:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.toolcustomizers;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import imagej.envisaje.api.toolcustomizers.AbstractCustomizer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tim Boudreau
 */
public final class TextCustomizer extends AbstractCustomizer <String> implements DocumentListener {
    public TextCustomizer(String name) {
        super (name);
    }

    public String getValue() {
        JTextField field = (JTextField) getComponents()[1];
        return field.getText();
    }

    public void insertUpdate(DocumentEvent e) {
        change();
    }

    public void removeUpdate(DocumentEvent e) {
        change();
    }

    public void changedUpdate(DocumentEvent e) {
        change();
    }

    protected JComponent[] createComponents() {
        String txt = NbPreferences.forModule(getClass()).get(getName(), getName());
        JTextField f = new JTextField (txt);
        f.setColumns(20);
        JLabel lbl = new JLabel (NbBundle.getMessage(TextCustomizer.class, "TEXT")); //NOI18N
        f.setColumns (20);
        f.getDocument().addDocumentListener (this);
        return new JComponent[] { lbl, f };
    }

    @Override
    protected void saveValue(String value) {
        NbPreferences.forModule(getClass()).put(getName(), value);
    }
}
