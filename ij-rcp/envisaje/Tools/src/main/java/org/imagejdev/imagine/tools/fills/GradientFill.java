/*
 * GradientFill.java
 *
 * Created on October 15, 2005, 9:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.tools.fills;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.java.dev.colorchooser.ColorChooser;
import org.openide.util.NbBundle;
/**
 *
 * @author Timothy Boudreau
 */
public class GradientFill extends BaseFill {
    
    private ColorChooser ch = null;
    @Override
    public JComponent getCustomizer() {
        JPanel result = (JPanel) super.getCustomizer();
        if (ch == null) {
            ch = new ColorChooser();
            //so it's not the same color as the other one
            ch.setColor (Color.ORANGE);
            JLabel lbl = new JLabel (NbBundle.getMessage (GradientFill.class, 
                    "LBL_GradientSecond"));
            ch.addActionListener (this);
            result.add (lbl);
            result.add (ch);
            ch.setPreferredSize(new Dimension (16, 16));
        }
        return result;
    }

    @Override
    protected String getChooserCaption() {
        return NbBundle.getMessage (GradientFill.class, "LBL_GradientFirst");
    }

    @Override
    public java.awt.Paint getPaint() {
        Color first = (Color) super.getPaint();
        Color second = ch == null ? Color.BLACK : ch.getColor();
        return new GradientPaint (0, 0, first, 200, 200, second, true);
    }
}
