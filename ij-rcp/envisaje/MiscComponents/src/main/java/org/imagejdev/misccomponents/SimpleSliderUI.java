/*
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.imagejdev.misccomponents;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.SliderUI;

/**
 *
 *
 * @author Timothy Boudreau
 */
public class SimpleSliderUI extends SliderUI implements ChangeListener {

    /**
     * Creates a new instance of SimpleSliderUI
     */

    public SimpleSliderUI() {
    }

    public static ComponentUI createUI(JComponent b) {
        return new SimpleSliderUI();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        JSlider js = (JSlider)c;
        int or = js.getOrientation();
        if (charWidth == -1) {
            computeCharWidth(null, c.getFont());
        }
        int maxChars;
        StringConverter sc = (StringConverter) c.getClientProperty("converter");
        if (sc != null) {
            maxChars = sc.maxChars();
        } else {
            maxChars = Math.max((js.getMinimum() + "").length(), (js.getMaximum() + "").toString().length());
        }
        maxChars+=3;
        
        Dimension result = new Dimension(or == JSlider.VERTICAL ? maxChars * charWidth
                                                    : 120,
                             or == JSlider.VERTICAL ? 120
                                                    : charWidth); //XXX should be height
        Insets ins = c.getInsets();
        result.width += ins.left + ins.right;
        result.height += ins.top + ins.bottom;
        if (or == JSlider.VERTICAL) {
            result.width += EDGE_GAP * 2;
        } else {
            result.height += EDGE_GAP * 2;
        }
        return result;
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        JSlider sl = (JSlider)c;
//        ((Graphics2D) g).setRenderingHints(getHints());

        if (sl.getOrientation() == JSlider.VERTICAL) {
            g.translate(-8, 0);
            paintTrack(g, sl);
            paintThumb(g, sl);
            paintCaption(g, sl);
            g.translate(8, 0);
        }
        else {
            g.translate(0, -7);
            paintTrackH(g, sl);
            paintThumbH(g, sl);
            g.translate(0, 7);
            paintCaptionH(g, sl);
        }
    }

    @Override
    public void installUI(JComponent c) {
        c.setBorder(BorderFactory.createRaisedBevelBorder());
        Font f = UIManager.getFont("controlFont");

        if (f != null) {
            c.setFont(f.deriveFont(f.getSize2D() - 2));
        }
        ((JSlider)c).addChangeListener(this);
        c.setBackground(Color.WHITE);
//        c.setFocusable(false);
        
    }

    @Override
    public void uninstallUI(JComponent c) {
        c.setBorder(null);
        ((JSlider)c).removeChangeListener(this);
    }

    private void computeCharWidth(Graphics g, Font f) {
        boolean created = g == null;
        if (created) {
            BufferedImage im = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            g = im.createGraphics();
        }
        FontMetrics fm = g.getFontMetrics(f);
        charWidth = fm.charWidth('0');
        if (created) g.dispose();
    }

    private void paintTrackH(Graphics g, JSlider sl) {
        Insets ins = sl.getInsets();
        int center = (sl.getHeight() - (ins.top + ins.bottom))/2;
        int end = sl.getWidth() - (EDGE_GAP + ins.left + ins.right);

        g.setColor(Color.black);
        g.drawLine(EDGE_GAP + ins.left, center, end, center);
        g.drawLine(EDGE_GAP + ins.left, center + EDGE_GAP, EDGE_GAP + ins.left,
                   center);
        g.drawLine(end, center + EDGE_GAP, end, center);
    }
    private static final int THUMB_SIZE = 7;
    private static final int EDGE_GAP = (THUMB_SIZE/2) + 3;

    private void paintThumbH(Graphics g, JSlider sl) {
        int pos = getThumbPositionH(sl);
        int center = (sl.getHeight()/2) - 2;
        int[] yp = new int[] {center + 1, 
                center + 1 + THUMB_SIZE,
                center + 1 + THUMB_SIZE};
        int[] xp = new int[] {pos, pos - THUMB_SIZE, pos + THUMB_SIZE};

        g.fillPolygon(xp, yp, 3);
    }

    private int getThumbPositionH(JSlider sl) {
        int val = sl.getValue();
        Insets ins = sl.getInsets();
        float range = sl.getWidth() -
                      ((EDGE_GAP*2) + ((ins.left + ins.right + ins.left)));
        float scale = sl.getMaximum() - sl.getMinimum();
        float factor = range/scale;
        float normVal = val - sl.getMinimum();

        return (int)(EDGE_GAP + (normVal*factor)) + ins.left;
    }

    private void paintCaptionH(Graphics g, JSlider sl) {
        Font f = sl.getFont();

        g.setFont(f);
        int center = sl.getHeight()/2;
        String s = valueToString (sl);
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(s);
        int h = fm.getMaxAscent();
        int y = (center + THUMB_SIZE + (h/2)) - 3;
        int x = getThumbPositionH(sl) - (w/2);

        g.setXORMode(Color.WHITE);
        g.drawString(s, x, y);
        g.setPaintMode();
    }
    
    public void setStringConverter (StringConverter converter) {
        this.converter = converter;
    }
    
    private StringConverter converter = null;
    String valueToString (JSlider sl) {
        return converter == null ? Integer.toString (sl.getValue()) : converter.valueToString(sl);
    }
    
    public interface StringConverter {
        public String valueToString(JSlider sl);
        public int maxChars();
    }

    private void paintTrack(Graphics g, JSlider sl) {
        Insets ins = sl.getInsets();
        int center = (sl.getWidth() - (ins.left + ins.right))/2;
        int end = sl.getHeight() - (EDGE_GAP + ins.top + ins.bottom);

        g.setColor(Color.black);
        g.drawLine(center, EDGE_GAP + ins.top, center, end);
        g.drawLine(center, EDGE_GAP + ins.top, center + EDGE_GAP,
                   EDGE_GAP + ins.top);
        g.drawLine(center, end, center + EDGE_GAP, end);
    }

    private void paintThumb(Graphics g, JSlider sl) {
        int pos = getThumbPosition(sl);
        int center = sl.getWidth()/2;
        int[] xp = new int[] {center + 1, center + 1 + THUMB_SIZE,
                center + 1 + THUMB_SIZE};
        int[] yp = new int[] {pos, pos - THUMB_SIZE, pos + THUMB_SIZE};

        g.fillPolygon(xp, yp, 3);
    }

    private int getThumbPosition(JSlider sl) {
        int val = sl.getValue();
        Insets ins = sl.getInsets();
        float range = sl.getHeight() -
                      ((EDGE_GAP*2) + ((ins.top + ins.bottom + ins.top)));
        float scale = sl.getMaximum() - sl.getMinimum();
        float factor = range/scale;
        float normVal = val - sl.getMinimum();

        return (int)(EDGE_GAP + (normVal*factor)) + ins.top;
    }

    int charWidth = -1;
    private void paintCaption(Graphics g, JSlider sl) {
        String s = valueToString(sl);
        Font f = sl.getFont();
        g.setFont(f);
        if (charWidth == -1) {
            computeCharWidth (g, f);
        }
//        g.setXORMode(Color.WHITE);
        g.setColor (UIManager.getColor("textText"));
        int x = (sl.getWidth()/2) + THUMB_SIZE + 3;
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(s);
        int h = fm.getMaxAscent();
        // int y = (sl.getHeight() / 2) - h / 2;
        int y = getThumbPosition(sl) + (h/2);

        g.drawString(s, x, y);
        g.setPaintMode();
    }
    private int detent = -1;

    void setDetent(int x) {
        detent = x;
    }

    void clearDetent() {
        detent = -1;
        baseValue = -1;
    }
    int baseValue = -1;

    void dragTo(int x, JSlider sl) {
        if (detent == -1) {
            detent = x;
            baseValue = sl.getValue();
            return;
        }
        boolean horiz = sl.getOrientation() == JSlider.HORIZONTAL;
        Insets ins = sl.getInsets();
        float range = horiz ? (sl.getWidth() - (ins.left + ins.right))
                            : sl.getHeight() - (ins.top + ins.bottom);
        float scale = sl.getMaximum() - sl.getMinimum();
        float factor = scale/range;
        
        int normVal = baseValue +
                      (int)(factor*
                            ((x + (horiz ? ins.left
                                         : ins.top) - detent)));
        int val = sl.getMinimum() + normVal;

        if (val < sl.getMinimum()) {
            detent -= ((float)sl.getMinimum() - (normVal + baseValue))*factor;
            if (sl.getValue() > sl.getMinimum()) {
                sl.setValue(sl.getMinimum());
            }
            baseValue = sl.getValue();
        }
        else if (val > sl.getMaximum()) {
            if (sl.getValue() < sl.getMaximum()) {
                // We moved the mouse fast enough that we never got an event for
                // the last mouse position, so the slider says e.g. max-2 but our mouse
                // position is past the end so it's not increasing
                sl.setValue(sl.getMaximum());
            }
            detent = -1;
        }
        else {
            sl.setValue(val);
            sl.repaint();
        }
    }

    public void stateChanged(ChangeEvent e) {
        ((JSlider)e.getSource()).repaint();
    }

    private static Map hintsMap = null;
    static final Map getHints() {
        //XXX We REALLY need to put this in a graphics utils lib
        if (hintsMap == null) {
            //Thanks to Phil Race for making this possible
            hintsMap = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (hintsMap == null) {
                hintsMap = new HashMap();
                hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
        }
        return hintsMap;
    }
    
}
