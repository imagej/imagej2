/*
 * FontCustomizer.java
 *
 * Created on September 30, 2006, 2:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.toolcustomizers;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.imagejdev.imagine.api.toolcustomizers.AbstractCustomizer;
import org.imagejdev.misccomponents.SharedLayoutPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tim Boudreau
 */
public final class FontCustomizer extends AbstractCustomizer <Font> implements ActionListener {
    
    private enum FontStyle {
        PLAIN, BOLD, ITALIC, BOLD_ITALIC;
        @Override
        public String toString() {
            return NbBundle.getMessage(FontCustomizer.class, name());
        }
        
        int toFontConstant() {
            switch (this) {
                case PLAIN :
                    return Font.PLAIN;
                case BOLD :
                    return Font.BOLD;
                case ITALIC :
                    return Font.ITALIC;
                case BOLD_ITALIC :
                    return Font.BOLD | Font.ITALIC;
                default : 
                    throw new AssertionError();
            }
        }
        
        static FontStyle fromFontConstant(int val) {
            switch (val) {
                case Font.PLAIN : return PLAIN;
                case Font.BOLD : return BOLD;
                case Font.ITALIC : return ITALIC;
                case Font.BOLD | Font.ITALIC : return BOLD_ITALIC;
                default : return PLAIN;
            }
        }
    }
    
    /** Creates a new instance of FontCustomizer */
    public FontCustomizer(String name) {
        super (name);
    }

    public Font getValue() {
        Font f = (Font) fontSelectBox.getSelectedItem();
        FontStyle style = (FontStyle) styleBox.getSelectedItem();
        if (style != FontStyle.PLAIN) {
            f = f.deriveFont(style.toFontConstant());
        }
        return f;
    }

    JComboBox styleBox;
    JLabel styleLabel;
    JComboBox fontSelectBox;
    protected JComponent[] createComponents() {
        fontSelectBox = new JComboBox();
        fontSelectBox.addActionListener(this);
        Font[] f = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        DefaultComboBoxModel fontsModel = new DefaultComboBoxModel();
        for (int i = 0; i < f.length; i++) {
            Font ff = f[i].deriveFont(Font.PLAIN, 12F);
            if (i == 0) {
                fontSelectBox.setPrototypeDisplayValue(ff);
            }
            fontsModel.addElement(ff);
        }
        JLabel fontLabel = new JLabel (NbBundle.getMessage(FontCustomizer.class, 
                "FONT_FACE")); //NOI18N
        fontSelectBox.setModel (fontsModel);
        fontSelectBox.setSelectedItem(loadValue());
        fontSelectBox.setRenderer(new FontRenderer());
        if (styleBox == null) {
            DefaultComboBoxModel styleModel = new DefaultComboBoxModel (FontStyle.values());
            styleBox = new JComboBox(styleModel);
            styleLabel = new JLabel (NbBundle.getMessage(FontCustomizer.class, "FONT_STYLE")); //NOI18N
            Font ff = getValue();
            FontStyle style = f == null ? FontStyle.PLAIN : FontStyle.fromFontConstant(ff.getStyle());
            styleBox.setSelectedItem(style);
            styleBox.addActionListener(this);
        }
        JPanel font = new SharedLayoutPanel();
        font.add (fontLabel, fontSelectBox);
        JPanel style = new SharedLayoutPanel();
        style.add (styleLabel, styleBox);
        return new JComponent[] { font, style };
    }
    
    public void actionPerformed(ActionEvent e) {
        change();
    }
    
    
    private static final class FontRenderer extends DefaultListCellRenderer {
        public void propertyChange (String s, Object a, Object b) {
            //performance - do nothing
        }
        
        @Override
        public void repaint(int x, int y, int w, int h) {}
        @Override
        public void validate() {}
        @Override
        public void invalidate() {}
        @Override
        public void revalidate() {}

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component retValue = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                Font f = (Font) value;
                retValue.setFont (f);
                setText (f.getName());
            } else {
                setText ("");
            }
            return retValue;
        }
    }

    @Override
    protected void saveValue(Font value) {
        if (value == null) {
            return;
        }
        String family = value.getFamily();
        float size = value.getSize2D();
        int style = value.getStyle();
        AffineTransform xform = value.getTransform();
        double[] matrix = new double[6];
        xform.getMatrix(matrix);
        Preferences p = NbPreferences.forModule(getClass());
        p.put(getName() + ".family", family);
        p.putFloat (getName() + ".size", size);
        p.putInt(getName() + ".style", style);
        for (int i=0; i < matrix.length; i++) {
            p.putDouble(getName() + ".xform." + i, matrix[i]);
        }
    }
    
    private Font loadValue() {
        Preferences p = NbPreferences.forModule(getClass());
        String family = p.get(getName() + ".family", "Serif");
        float size = p.getFloat(getName() + ".size", 24);
        int style = p.getInt (getName() + ".style", Font.PLAIN);
        double[] matrix = new double[6];
        boolean hasXform = false;
        for (int i=0; i < matrix.length; i++) {
            matrix[i] = p.getDouble(getName() + ".xform." + i, 0);
            hasXform |= matrix[i] != 0D;
        }
        AffineTransform xform = new AffineTransform (matrix);
        Font result = new Font (family, style, (int) size);
        if (hasXform) {
            result = result.deriveFont(xform);
        }
        return result;
    }
}
