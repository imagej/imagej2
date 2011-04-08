/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.api.toolcustomizers;

import java.awt.Font;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public enum FontStyle {
    PLAIN, BOLD, ITALIC, BOLD_ITALIC;
    
    public int toFontConstant() {
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
                throw new AssertionError (toString());
        }
    }
    
    public FontStyle fromFontConstant(int val) {
        switch (val) {
            case Font.BOLD : return BOLD;
            case Font.ITALIC : return ITALIC;
            case Font.BOLD | Font.ITALIC : return BOLD_ITALIC;
            default :
                return PLAIN;
        }
    }
    
    @Override
    public String toString() {
        return NbBundle.getMessage (FontStyle.class, name());
    }
}
