/*
 * ColorCustomizer.java
 *
 * Created on September 30, 2006, 2:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.toolcustomizers;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JLabel;
import net.java.dev.colorchooser.ColorChooser;
import imagej.envisaje.api.toolcustomizers.AbstractCustomizer;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tim Boudreau
 */
public class ColorCustomizer extends AbstractCustomizer <Color> implements ActionListener {

    public ColorCustomizer(String name) {
        super (name);
    }

    protected JComponent[] createComponents() {
        JLabel lbl = new JLabel (getName());
        ColorChooser chooser = new ColorChooser();
        Preferences p = NbPreferences.forModule(ColorCustomizer.class);
        int red = p.getInt(getName() + ".red", 0); //NOI18N
        int green = p.getInt(getName() + ".green", 0); //NOI18N
        int blue = p.getInt(getName() + ".blue", 0); //NOI18N
        int alpha = p.getInt(getName() + ".alpha", 0); //NOI18N
        Color color = new Color (red, green, blue, alpha);
        chooser.setColor (color);
        chooser.addActionListener(this);
        lbl.setLabelFor(chooser);
        return new JComponent[] { lbl, chooser };
    }

    public void actionPerformed(ActionEvent e) {
        change();
    }

    public Color getValue() {
        return ((ColorChooser) getComponents()[1]).getColor();
    }
    
    @Override
    protected void saveValue(Color value) {
        int red = value.getRed();
        int green = value.getGreen();
        int blue = value.getBlue();
        int alpha = value.getAlpha();
        Preferences p = NbPreferences.forModule(ColorCustomizer.class);
        p.putInt(getName() + ".red", red); //NOI18N
        p.putInt(getName() + ".green", green); //NOI18N
        p.putInt(getName() + ".blue", blue); //NOI18N
        p.putInt(getName() + ".alpha", alpha); //NOI18N
    }
    
}
