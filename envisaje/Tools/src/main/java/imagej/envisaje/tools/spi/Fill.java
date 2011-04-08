/*
 * Fill.java
 *
 * Created on October 15, 2005, 6:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.tools.spi;

import java.awt.Component;
import java.awt.Paint;

/**
 *
 * @author Timothy Boudreau
 */
public interface Fill {
    public Paint getPaint();
    public Component getCustomizer();
}
