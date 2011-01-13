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
 * NamedColor.java
 *
 * Created on 1. prosinec 2003, 16:49
 */

package net.java.dev.colorchooser;

import java.awt.Color;

/** An abstract class representing a color which has a name and may provide
 * custom code for instantiation. Implements comparable in order to appear
 * in an ordered way in palettes.  Note that this class is internal to the color
 * chooser.  It is not acceptable
 * for the color chooser to provide instances of NamedColor from its getColor
 * method, since they may be serialized and will not be deserializable if their
 * implementation is not on the classpath.
 *
 * @author  Tim Boudreau
 */
abstract class NamedColor extends Color implements Comparable {
    /**
     * Creates a new instance of NamedColor
     * @param name
     * @param r red
     * @param g green
     * @param b blue
     */
    protected NamedColor(String name, int r, int g, int b) {
        super (r, g, b);
    }
    /**
     * Get a localized display name for this color if possible.  For
     * some colors, such as named system colors, a localized variant is not
     * a reasonable option.
     * @return the localized (or not) display name
     */
    public abstract String getDisplayName();
    /** Get the programmatic name, if any, for this color, such as a 
     * Swing UIDefaults key or an SVG constant name.*/
    public abstract String getName();
    /**
     * Fetch a java code snippet for instantiating this color.  For cases such
     * as named defaults from the Swing UIManager, this method might return
     * something such as <code>UIManager.getColor(&quot;control&quot;)</code>.
     * Useful when implementing a property editor.
     * @return a string that could be pasted into Java code to instantiate a
     * color with these rgb values
     */
    public String getInstantiationCode() {
        return toString();
    }
    
    static NamedColor create (Color c, String name) {
        return new DefaultNamedColor(c, name);
    }
    
    private static final class DefaultNamedColor extends NamedColor {
        private String name;
        public DefaultNamedColor(Color c, String name) {
            super (name, c.getRed(), c.getGreen(), c.getBlue());
            this.name = name;
        }

        public String getDisplayName() {
            return name;
        }

        public String getName() {
            return name;
        }

        public int compareTo(Object o) {
            if (o instanceof NamedColor) {
                NamedColor nc = (NamedColor) o;
                String nm = nc.getDisplayName();
                if (nm == null && getDisplayName() == null) {
                    return 0;
                } else {
                    return nm != null && getDisplayName() != null ?
                        getDisplayName().compareTo(nm) : -1;
                }
            } else {
                return -1;
            }
        }
    }
}
