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
 * PredefinedPalette.java
 *
 * Created on 30. listopad 2003, 10:31
 */

package net.java.dev.colorchooser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.util.Arrays;
import javax.swing.UIManager;

/** A palette of swatches with predefined colors 
 *
 * @author  Tim Boudreau
 */
class PredefinedPalette extends Palette {
    NamedColor[] colors;
    private int swatchSize = 12;
    private int gap = 1;
    private static final Rectangle scratch = new Rectangle();
    private String name;
    /** Creates a new instance of PredefinedPalette */
    public PredefinedPalette(String name, NamedColor[] colors) {
        this.colors = colors;
        this.name = name;
        Arrays.sort(colors);
        if (colors.length < 14) {
            swatchSize = 24;
        } else if (colors.length < 40) {
            swatchSize = 16;
        }
    }
    
    public java.awt.Color getColorAt(int x, int y) {
        Color result = null;
        int idx = indexForPoint (x,y);
        if (idx != -1 && idx < colors.length) {
            result = colors[idx];
        }
        return result;
    }
    
    public void paintTo(java.awt.Graphics g) {
        g.setColor(Color.BLACK);
        Dimension size = getSize();
        g.fillRect(0,0,size.width,size.height);
        for (int i=0; i < colors.length; i++) {
            Color c = colors[i];
            rectForIndex(i, scratch);
            g.setColor(c);
            g.fillRect(scratch.x,scratch.y,scratch.width,scratch.height);
            if (Color.BLACK.equals(c)) {
                g.setColor(Color.GRAY);
            } else {
                g.setColor(c.brighter());
            }
            g.drawLine(scratch.x, scratch.y, scratch.x + scratch.width-1, scratch.y);
            g.drawLine(scratch.x, scratch.y, scratch.x, scratch.y + scratch.height-1);
            if (Color.BLACK.equals(c)) {
                g.setColor(Color.GRAY.darker());
            } else {
                g.setColor(c.darker());
            }
            g.drawLine(scratch.x+scratch.width-1, scratch.y + scratch.height-1, scratch.width+scratch.x-1, scratch.y+1);
            g.drawLine(scratch.x+scratch.width-1, scratch.y + scratch.height-1, scratch.x, scratch.y + scratch.height-1);
        }
    }
    
    public String getNameAt(int x, int y) {
        NamedColor nc = (NamedColor)getColorAt(x,y);
        if (nc != null) {
            return nc.getDisplayName();
        } else {
            return null;
        }
    }
    
    protected int getCount() {
        return colors.length;
    }
    
    Dimension calcSize() {
        int count = colors.length;
        //look for the square root of the count and multiply by the size
        double dblWidth = (swatchSize + gap) * Math.sqrt(count);
        int width = Math.round(Math.round(dblWidth));
        int height = width;
        
        //If it's not a perfect square, we'll need another row to hold the
        //remainder
        double flr = Math.floor(Math.sqrt(count));
        if (Math.sqrt(count) - flr != 0) {
            //we don't have a perfect square, the usual case
            height += swatchSize+gap;
        }
        
        //Get rid of rounding errors - the square root * cell width probably
        //gave us an extra decimal portion that means extra left/bottom padding
        int perRow = width / (swatchSize + gap);
        int perCol = height / (swatchSize + gap);
        //Make sure the gap is the same on all sides - rounding errors may
        //make it not quite match, and we want the same right and bottom margins
        width = perRow * (swatchSize + gap) + gap;
        height = perCol * (swatchSize + gap) + gap;
        
        Dimension result = new Dimension(width,height);
        return result;
    }
    
    private Dimension size = null;
    public Dimension getSize() {
        if (size == null) {
            size = calcSize();
        }
        return size;
    }
    
    private int indexForPoint (int x, int y) {
        Dimension d = getSize();
        if (y > d.height || x > d.width || y < 0 || x < 0) {
            return -1;
        }
        int perRow = d.width / (swatchSize + gap);
        
        int col = x / (swatchSize + gap);
        int row = y / (swatchSize + gap);
        return (row * perRow) + col;
    }
    
    private void rectForIndex(int idx, final Rectangle r) {
        int count = colors.length;
        Dimension d = getSize();
        int rectsPerRow = d.width / (swatchSize + gap);
        r.x = gap + ((swatchSize + gap) * (idx % rectsPerRow));
        r.y = gap + ((swatchSize + gap) * (idx / rectsPerRow));
        r.width = swatchSize;
        r.height=swatchSize;
    }
    
    
    private static Palette[] predefined = null;
    public static Palette[] createDefaultPalettes() {
        if (predefined == null) {
            predefined = makePal();
        }
        return predefined;
    }
    
    private static final Palette[] makePal () {
        Palette[] result = new Palette[] {
            new PredefinedPalette("svg", SVGColors),
            new PredefinedPalette("system", getSystemColors()),
            RecentColors.getDefault(),
            new PredefinedPalette("swing", getSwingColors())
        };
        return result;
    }
    
    static class BasicNamedColor extends NamedColor implements Comparable {
        private String name;
        public BasicNamedColor(String name, int r, int g, int b) {
            super(name,r,g,b);
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            return ColorChooser.getString(getName());
        }
        
        public String toString() {
            return "new java.awt.Color(" + getRed() + "," + getGreen() + "," + getBlue() + ")";
        }

        public int compareTo(Object o) {
            Color c = (Color) o;
            //maybe average rgb & compare?
            int result = avgColor(c) - avgColor(this);
            return result;
        }
        
        public String getInstantiationCode() {
            return toString();
        }
        
    }
    
    private static int avgColor (Color c) {
        return (c.getRed() + c.getGreen() + c.getBlue()) / 3;
    }
    
    public String getDisplayName() {
        return ColorChooser.getString(name);
    }
    
    static String getColorName (Color c) {
        for (int i = 0; i < swingColors.length; i++) {
            if (equals(swingColors[i], c)) {
                return swingColors[i].getDisplayName();
            }
        }
        for (int i = 0; i < SVGColors.length; i++) {
            if (equals(SVGColors[i], c)) {
                return SVGColors[i].getDisplayName();
            }
        }
        return null;
    }
    
    static boolean equals (Color a, Color b) {
        return a.getRGB() == b.getRGB();
    }
    
    private static final NamedColor[] SVGColors = new BasicNamedColor[] {
        new BasicNamedColor("aliceblue", 240, 248, 255),
        new BasicNamedColor("antiquewhite", 250, 235, 215),
        new BasicNamedColor("aqua",  0, 255, 255),
        new BasicNamedColor("aquamarine", 127, 255, 212),
        new BasicNamedColor("azure", 240, 255, 255),
        new BasicNamedColor("beige", 245, 245, 220),
        new BasicNamedColor("bisque", 255, 228, 196),
        new BasicNamedColor("black",  0, 0, 0),
        new BasicNamedColor("blanchedalmond", 255, 235, 205),
        new BasicNamedColor("blue",  0, 0, 255),
        new BasicNamedColor("blueviolet", 138, 43, 226),
        new BasicNamedColor("brown", 165, 42, 42),
        new BasicNamedColor("burlywood", 222, 184, 135),
        new BasicNamedColor("cadetblue",  95, 158, 160),
        new BasicNamedColor("chartreuse", 127, 255, 0),
        new BasicNamedColor("chocolate", 210, 105, 30),
        new BasicNamedColor("coral", 255, 127, 80),
        new BasicNamedColor("cornflowerblue", 100, 149, 237),
        new BasicNamedColor("cornsilk", 255, 248, 220),
        new BasicNamedColor("crimson", 220, 20, 60),
        new BasicNamedColor("cyan",  0, 255, 255),
        new BasicNamedColor("darkblue",  0, 0, 139),
        new BasicNamedColor("darkcyan",  0, 139, 139),
        new BasicNamedColor("darkgoldenrod", 184, 134, 11),
        new BasicNamedColor("darkgray", 169, 169, 169),
        new BasicNamedColor("darkgreen",  0, 100, 0),
        new BasicNamedColor("darkgrey", 169, 169, 169),
        new BasicNamedColor("darkkhaki", 189, 183, 107),
        new BasicNamedColor("darkmagenta", 139, 0, 139),
        new BasicNamedColor("darkolivegreen",  85, 107, 47),
        new BasicNamedColor("darkorange", 255, 140, 0),
        new BasicNamedColor("darkorchid", 153, 50, 204),
        new BasicNamedColor("darkred", 139, 0, 0),
        new BasicNamedColor("darksalmon", 233, 150, 122),
        new BasicNamedColor("darkseagreen", 143, 188, 143),
        new BasicNamedColor("darkslateblue",  72, 61, 139),
        new BasicNamedColor("darkslategray",  47, 79, 79),
        new BasicNamedColor("darkslategrey",  47, 79, 79),
        new BasicNamedColor("darkturquoise",  0, 206, 209),
        new BasicNamedColor("darkviolet", 148, 0, 211),
        new BasicNamedColor("deeppink", 255, 20, 147),
        new BasicNamedColor("deepskyblue",  0, 191, 255),
        new BasicNamedColor("dimgray", 105, 105, 105),
        new BasicNamedColor("dimgrey", 105, 105, 105),
        new BasicNamedColor("dodgerblue",  30, 144, 255),
        new BasicNamedColor("firebrick", 178, 34, 34),
        new BasicNamedColor("floralwhite", 255, 250, 240),
        new BasicNamedColor("forestgreen",  34, 139, 34),
        new BasicNamedColor("fuchsia", 255, 0, 255),
        new BasicNamedColor("gainsboro", 220, 220, 220),
        new BasicNamedColor("ghostwhite", 248, 248, 255),
        new BasicNamedColor("gold", 255, 215, 0),
        new BasicNamedColor("goldenrod", 218, 165, 32),
        new BasicNamedColor("gray", 128, 128, 128),
        new BasicNamedColor("grey", 128, 128, 128),
        new BasicNamedColor("green",  0, 128, 0),
        new BasicNamedColor("greenyellow", 173, 255, 47),
        new BasicNamedColor("honeydew", 240, 255, 240),
        new BasicNamedColor("hotpink", 255, 105, 180),
        new BasicNamedColor("indianred", 205, 92, 92),
        new BasicNamedColor("indigo",  75, 0, 130),
        new BasicNamedColor("ivory", 255, 255, 240),
        new BasicNamedColor("khaki", 240, 230, 140),
        new BasicNamedColor("lavender", 230, 230, 250),
        new BasicNamedColor("lavenderblush", 255, 240, 245),
        new BasicNamedColor("lawngreen", 124, 252, 0),
        new BasicNamedColor("lemonchiffon", 255, 250, 205),
        new BasicNamedColor("lightblue", 173, 216, 230),
        new BasicNamedColor("lightcoral", 240, 128, 128),
        new BasicNamedColor("lightcyan", 224, 255, 255),
        new BasicNamedColor("lightgoldenrodyellow", 250, 250, 210),
        new BasicNamedColor("lightgray", 211, 211, 211),
        new BasicNamedColor("lightgreen", 144, 238, 144),
        new BasicNamedColor("lightgrey", 211, 211, 211),
        new BasicNamedColor("lightpink", 255, 182, 193),
        new BasicNamedColor("lightsalmon", 255, 160, 122),
        new BasicNamedColor("lightseagreen",  32, 178, 170),
        new BasicNamedColor("lightskyblue", 135, 206, 250),
        new BasicNamedColor("lightslategray", 119, 136, 153),
        new BasicNamedColor("lightslategrey", 119, 136, 153),
        new BasicNamedColor("lightsteelblue", 176, 196, 222),
        new BasicNamedColor("lightyellow", 255, 255, 224),
        new BasicNamedColor("lime",  0, 255, 0),
        new BasicNamedColor("limegreen",  50, 205, 50),
        new BasicNamedColor("linen", 250, 240, 230),
        new BasicNamedColor("magenta", 255, 0, 255),
        new BasicNamedColor("maroon", 128, 0, 0),
        new BasicNamedColor("mediumaquamarine", 102, 205, 170),
        new BasicNamedColor("mediumblue",  0, 0, 205),
        new BasicNamedColor("mediumorchid", 186, 85, 211),
        new BasicNamedColor("mediumpurple", 147, 112, 219),
        new BasicNamedColor("mediumseagreen",  60, 179, 113),
        new BasicNamedColor("mediumslateblue", 123, 104, 238),
        new BasicNamedColor("mediumspringgreen",  0, 250, 154),
        new BasicNamedColor("mediumturquoise",  72, 209, 204),
        new BasicNamedColor("mediumvioletred", 199, 21, 133),
        new BasicNamedColor("midnightblue",  25, 25, 112),
        new BasicNamedColor("mintcream", 245, 255, 250),
        new BasicNamedColor("mistyrose", 255, 228, 225),
        new BasicNamedColor("moccasin", 255, 228, 181),
        new BasicNamedColor("navajowhite", 255, 222, 173),
        new BasicNamedColor("navy",  0, 0, 128),
        new BasicNamedColor("oldlace", 253, 245, 230),
        new BasicNamedColor("olive", 128, 128, 0),
        new BasicNamedColor("olivedrab", 107, 142, 35),
        new BasicNamedColor("orange", 255, 165, 0),
        new BasicNamedColor("orangered", 255, 69, 0),
        new BasicNamedColor("orchid", 218, 112, 214),
        new BasicNamedColor("palegoldenrod", 238, 232, 170),
        new BasicNamedColor("palegreen", 152, 251, 152),
        new BasicNamedColor("paleturquoise", 175, 238, 238),
        new BasicNamedColor("palevioletred", 219, 112, 147),
        new BasicNamedColor("papayawhip", 255, 239, 213),
        new BasicNamedColor("peachpuff", 255, 218, 185),
        new BasicNamedColor("peru", 205, 133, 63),
        new BasicNamedColor("pink", 255, 192, 203),
        new BasicNamedColor("plum", 221, 160, 221),
        new BasicNamedColor("powderblue", 176, 224, 230),
        new BasicNamedColor("purple", 128, 0, 128),
        new BasicNamedColor("red", 255, 0, 0),
        new BasicNamedColor("rosybrown", 188, 143, 143),
        new BasicNamedColor("royalblue",  65, 105, 225),
        new BasicNamedColor("saddlebrown", 139, 69, 19),
        new BasicNamedColor("salmon", 250, 128, 114),
        new BasicNamedColor("sandybrown", 244, 164, 96),
        new BasicNamedColor("seagreen",  46, 139, 87),
        new BasicNamedColor("seashell", 255, 245, 238),
        new BasicNamedColor("sienna", 160, 82, 45),
        new BasicNamedColor("silver", 192, 192, 192),
        new BasicNamedColor("skyblue", 135, 206, 235),
        new BasicNamedColor("slateblue", 106, 90, 205),
        new BasicNamedColor("slategray", 112, 128, 144),
        new BasicNamedColor("slategrey", 112, 128, 144),
        new BasicNamedColor("snow", 255, 250, 250),
        new BasicNamedColor("springgreen",  0, 255, 127),
        new BasicNamedColor("steelblue",  70, 130, 180),
        new BasicNamedColor("tan", 210, 180, 140),
        new BasicNamedColor("teal",  0, 128, 128),
        new BasicNamedColor("thistle", 216, 191, 216),
        new BasicNamedColor("tomato", 255, 99, 71),
        new BasicNamedColor("turquoise",  64, 224, 208),
        new BasicNamedColor("violet", 238, 130, 238),
        new BasicNamedColor("wheat", 245, 222, 179),
        new BasicNamedColor("white", 255, 255, 255),
        new BasicNamedColor("whitesmoke", 245, 245, 245),
        new BasicNamedColor("yellow", 255, 255, 0),
        new BasicNamedColor("yellowgreen", 154, 205, 50)
    }; //NOI18N
    
    static class SwingColor extends BasicNamedColor {
        public SwingColor(String name, int r, int g, int b) {
            super(name,r,g,b);
        }
        public String toString() {
            return "UIManager.getColor("+getName()+")";
        }
        public String getDisplayName() {
            return getName();
        }
        
        public String getInstantiationCode() {
            return toString();
        }
    }

    private static SwingColor[] swingColors = null;
    private static SwingColor[] getSwingColors() {
        if (swingColors != null) {
            return swingColors;
        }
        java.util.List l = new java.util.ArrayList();
        Color c;
        c = UIManager.getColor("windowText");
        if (c != null) {
            l.add(new SwingColor("windowText", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("activeCaptionBorder");
        if (c != null) {
            l.add(new SwingColor("activeCaptionBorder", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("inactiveCaptionText");
        if (c != null) {
            l.add(new SwingColor("inactiveCaptionText", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("controlLtHighlight");
        if (c != null) {
            l.add(new SwingColor("controlLtHighlight", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("inactiveCaptionBorder");
        if (c != null) {
            l.add(new SwingColor("inactiveCaptionBorder", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("textInactiveText");
        if (c != null) {
            l.add(new SwingColor("textInactiveText", c.getRed(), c.getGreen(), c.getBlue()));
            }
        c = UIManager.getColor("control");
        if (c != null) {
            l.add(new SwingColor("control", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("textText");
        if (c != null) {
            l.add(new SwingColor("textText", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("menu");
        if (c != null) {
            l.add(new SwingColor("menu", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("windowBorder");
        if (c != null) {
            l.add(new SwingColor("windowBorder", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("infoText");
        if (c != null) {
            l.add(new SwingColor("infoText", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("menuText");
        if (c != null) {
            l.add(new SwingColor("menuText", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("textHighlightText");
        if (c != null) {
            l.add(new SwingColor("textHighlightText", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("activeCaptionText");
        if (c != null) {
            l.add(new SwingColor("activeCaptionText", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("textHighlight");
        if (c != null) {
            l.add(new SwingColor("textHighlight", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("controlShadow");
        if (c != null) {
            l.add(new SwingColor("controlShadow", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("controlText");
        if (c != null) {
            l.add(new SwingColor("controlText", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("menuPressedItemF");
        if (c != null) {
            l.add(new SwingColor("menuPressedItemF", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("menuPressedItemB");
        if (c != null) {
            l.add(new SwingColor("menuPressedItemB", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("info");
        if (c != null) {
            l.add(new SwingColor("info", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("controlHighlight");
        if (c != null) {
            l.add(new SwingColor("controlHighlight", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("scrollbar");
        if (c != null) {
            l.add(new SwingColor("scrollbar", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("window");
        if (c != null) {
            l.add(new SwingColor("window", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("inactiveCaption");
        if (c != null) {
            l.add(new SwingColor("inactiveCaption", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("controlDkShadow");
        if (c != null) {
            l.add(new SwingColor("controlDkShadow", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("activeCaption");
        if (c != null) {
            l.add(new SwingColor("activeCaption", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("text");
        if (c != null) {
            l.add(new SwingColor("text", c.getRed(), c.getGreen(), c.getBlue()));
        }
        c = UIManager.getColor("desktop");
        if (c != null) {
            l.add(new SwingColor("desktop", c.getRed(), c.getGreen(), c.getBlue()));
        }
        swingColors = (SwingColor[]) l.toArray(new SwingColor[0]);
        return swingColors;
    }
    
    /** Names of system colors. <em>Note:</em> not localizable,
     * those names corresponds to programatical names. */
    private static final String systemGenerate[] = {
        "activeCaption", "activeCaptionBorder", // NOI18N
        "activeCaptionText", "control", "controlDkShadow", // NOI18N
        "controlHighlight", "controlLtHighlight", // NOI18N
        "controlShadow", "controlText", "desktop", // NOI18N
        "inactiveCaption", "inactiveCaptionBorder", // NOI18N
        "inactiveCaptionText", "info", "infoText", "menu", // NOI18N
        "menuText", "scrollbar", "text", "textHighlight", // NOI18N
        "textHighlightText", "textInactiveText", "textText", // NOI18N
        "window", "windowBorder", "windowText"}; // NOI18N
        
        /** System colors used in System Palette. */
        private static final Color sColors[] = {
            SystemColor.activeCaption, SystemColor.activeCaptionBorder,
            SystemColor.activeCaptionText, SystemColor.control,
            SystemColor.controlDkShadow, SystemColor.controlHighlight,
            SystemColor.controlLtHighlight, SystemColor.controlShadow,
            SystemColor.controlText, SystemColor.desktop,
            SystemColor.inactiveCaption, SystemColor.inactiveCaptionBorder,
            SystemColor.inactiveCaptionText, SystemColor.info,
            SystemColor.infoText, SystemColor.menu,
            SystemColor.menuText, SystemColor.scrollbar, SystemColor.text,
            SystemColor.textHighlight, SystemColor.textHighlightText,
            SystemColor.textInactiveText, SystemColor.textText,
            SystemColor.window, SystemColor.windowBorder,
            SystemColor.windowText
        };

        private static class SysColor extends BasicNamedColor {
            public SysColor(String name, Color scolor) {
                super(name, scolor.getRed(), scolor.getGreen(), scolor.getBlue());
            }
            
            public String toString() {
                return "SystemColor." + getName();
            }
            
            public String getDisplayName() {
                return getName();
            }
        }
        
        
        
     private static NamedColor[] systemColors=null;
     private static NamedColor[] getSystemColors() {
         if (systemColors == null) {
             systemColors = new BasicNamedColor[sColors.length];
             for (int i=0; i < sColors.length; i++) {
                 systemColors[i] = new SysColor(systemGenerate[i], sColors[i]);
             }
         }
         return systemColors;
     }
     
    
    /*
    //A cheap and cheesy way to quickly make an English-localized bundle for
    //an array of NamedColor.
    public static void main(String args[]) {
        for (int i=0; i < SVGColors.length; i++) {
            System.out.println("# " + SVGColors[i].getRed() + "," + SVGColors[i].getGreen() + "," + SVGColors[i].getBlue());
            String name = SVGColors[i].getName();
            StringBuffer sb = new StringBuffer(name);
            sb.replace (0, 1, new String(new char[]{Character.toUpperCase(name.charAt(0))}));
            String dispName = sb.toString();
     
            for (int k=0; k < tros.length; k++) {
                String lookFor = tros[k];
                StringBuffer sb2 = new StringBuffer(lookFor);
                sb.replace (0, 1, new String(new char[]{Character.toUpperCase(lookFor.charAt(0))}));
                String replaceWith = " " + sb2.toString();
                dispName = org.openide.util.Utilities.replaceString(
                    dispName, tros[k], replaceWith);
            }
     
            System.out.println(SVGColors[i].getName() + "=" + dispName);
        }
    }
     
   private static String[] tros = new String[] {
       "brown", "blue", "red", "violet", "orchid", "green", "yellow",
       "orange", "black", "gray", "grey", "white", "maroon", "goldenrod",
       "cream", "spring", "purple", "sea", "magenta", "steel", "slate","pink",
       "cyan", "chiffon", "coral", "wheat", "blush", "brick","olive","khaki",
       "silk","crimson","wood","almond", "smoke", "puff", "whip", "peach", "lace",
       "rose","turquoise","aquamarine","salmon", "sky"
   };
     */    
    /**
     * //some boilerplate to make a list of swing colors
     * public static void main (String[] args) {
     * javax.swing.LookAndFeel lf = new javax.swing.plaf.metal.MetalLookAndFeel();
     * javax.swing.UIDefaults def = javax.swing.UIManager.getLookAndFeel().getDefaults();
     * java.util.Iterator i = new java.util.ArrayList(def.keySet()).iterator();
     * while (i.hasNext()) {
     * Object o = i.next();
     * //            System.err.println(o);
     * Object result = def.get(o);
     * if (result instanceof Color) {
     * String s = o.toString();
     * if (s.indexOf(".") == -1) {
     * System.err.println("c = UIManager.getColor(\"" + s +"\");\nif (c != null) {\n     l.add(new SwingColor(\"" + s + "\", c.getRed(), c.getGreen(), c.getBlue()));\n}");
     * }
     * }
     * }
     * System.err.println("Done");
     * }
     */    
}
