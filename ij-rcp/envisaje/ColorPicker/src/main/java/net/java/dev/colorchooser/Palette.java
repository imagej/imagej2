/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2000-2008 Tim Boudreau. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 */
/*
 * Palette.java
 *
 * Created on 29. November 2003, 17:15
 */

package net.java.dev.colorchooser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/** Model for a palette that can be painted, and from which
 * colors may be selected.  An array of palettes to use may be supplied to
 * a ColorChooser via the setPalettes method.
 *
 * @author  Tim Boudreau */
public abstract class Palette {
    
    /**
     * Returns the color at the specified point, or null if the point is
     * beyond the bounds of the palette or in an area that does not indicate a
     * color
     *
     * @param x - an horizontal coordinate in the coordinate space of the palette
     * @param y - a vertical coordinate in the coordinate space of the palette
     * @return - a color or null
     */
    public abstract Color getColorAt(int x, int y);
    /**
     * Returns a string description of the color at the point.  May be a
     * name or a set of RGB values, but should not be longer than 30 characters.
     * Returns null if the position is outside the bounds of the palette or has no
     * description associated with it.  Generally getNameAt() should 
     * return null from the same coordinates that getColorAt() would.
     * 
     * @param x  an horizontal coordinate in the coordinate space of the palette
     * @param y  a vertical coordinate in the coordinate space of the palette
     * @return a string describing the color at this coordinate or null
     * @see #getColorAt
     */
    public abstract String getNameAt(int x, int y);
    /**
     * Paint this palette to a graphics context.
     * @param g - a graphics context to paint into
     */
    public abstract void paintTo(Graphics g);
    /**Get the on-screen size of this palette
     * @return the size of this palette - corresponding to the screen space 
     * required to display it and defining the coordinate space of this palette.
     */
    public abstract Dimension getSize();
    /**
     * Get a localized name for this palette or null if a display name is
     * not warranted
     * @return the display name
     */
    public abstract String getDisplayName();
    
    /**
     * Get the default set of 8 palettes used by the color chooser.  If 
     * continuousFirst is true, the first four will be continuous palettes
     * and the second four swatches with named colors, system colors, etc.
     */
    public static final Palette[] getDefaultPalettes(boolean continuousFirst) {
        Palette[] result = new Palette[8];
        Palette[] first = continuousFirst ? 
            ContinuousPalette.createDefaultPalettes() : 
            PredefinedPalette.createDefaultPalettes();
        Palette[] second = !continuousFirst ? 
            ContinuousPalette.createDefaultPalettes() : 
            PredefinedPalette.createDefaultPalettes();

        result = new Palette[second.length + first.length];
        System.arraycopy(first, 0, result, 0, 4);
        System.arraycopy(second, 0, result, 4, 4);
        return result;
    }
    
    public static final Palette createContinuousPalette (String name, Dimension size, float saturation) {
        if (size.width <= 0) throw new IllegalArgumentException("width less than or equal 0");
        if (size.height <= 0) throw new IllegalArgumentException("height less than or equal 0");
        return new ContinuousPalette(name, size.width, size.height, saturation);
    }
    
    public static final Palette createPredefinedPalette (String name, Color[] colors, String[] names) {
        NamedColor[] cc = new NamedColor[colors.length];
        for (int i=0; i < colors.length; i++) {
            cc[i] = NamedColor.create(colors[i], names[i]);
        }
        return new PredefinedPalette(name, cc);
    }
}
