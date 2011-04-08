/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.misccomponents;

import java.awt.Component;

/**
 *
 * @author tim
 */
public interface LayoutDataProvider {
    public int getColumnPosition (int col);
    public boolean isExpanded();
    public void doSetExpanded (boolean val);
}
