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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.SliderUI;
import org.imagejdev.misccomponents.SimpleSliderUI.StringConverter;

/**
 *
 * An implementation of SliderUI that draws an integer with a down arrow,
 * and pops up an actual JSlider.
 *
 * @author Timothy Boudreau
 */
public final class PopupSliderUI extends SliderUI implements PropertyChangeListener,
                                                             ChangeListener,
                                                             MouseListener,
                                                             KeyListener,
                                                             FocusListener,
                                                             MouseMotionListener {
    private Icon downArrow;
    private Icon downArrowLit;
    
    public PopupSliderUI() {
        try {
            InputStream downArrowStream = PopupSliderUI.class.getResourceAsStream("downarrow.png");
            BufferedImage downArrowImage = ImageIO.read(downArrowStream);
            downArrow = new ImageIcon(downArrowImage);
            
            InputStream downArrowLitStream = PopupSliderUI.class.getResourceAsStream("downarrowlit.png");
            BufferedImage downArrowLitImage = ImageIO.read (downArrowLitStream);
            downArrowLit = new ImageIcon(downArrowLitImage);
        } catch (Exception e) {
            throw new Error (e);
        }
    }
    Border raised = BorderFactory.createBevelBorder(BevelBorder.RAISED);
    private static Map mdls2sliders = new WeakHashMap();
    private static PopupSliderUI instance = null;

    public static ComponentUI createUI(JComponent b) {
        if (instance == null) {
            // Stateless UI, there never more than one
            instance = new PopupSliderUI();
        }
        return instance;
    }

    private static JSlider findSlider(BoundedRangeModel mdl) {
        Reference r = (Reference)mdls2sliders.get(mdl);

        return (JSlider)r.get();
    }

    @Override
    public void installUI(JComponent c) {
        JSlider js = (JSlider)c;

        c.addMouseListener(this);
        c.addKeyListener(this);
        js.getModel().addChangeListener(this);
        js.setOrientation(JSlider.VERTICAL);
        c.setFocusable(true);
        mdls2sliders.put(((JSlider)c).getModel(), new WeakReference(c));
        c.addPropertyChangeListener("model", this);
        c.setFont(UIManager.getFont("controlFont"));
        c.addFocusListener(this);
    }

    private void initBorder(JComponent c) {
        Insets ins = raised.getBorderInsets(c);

        c.setBorder(BorderFactory.createEmptyBorder(ins.top, ins.left,
                                                    ins.bottom, ins.right));
    }

    @Override
    public void uninstallUI(JComponent c) {
        c.removeMouseListener(this);
        c.removeKeyListener(this);
        ((JSlider)c).getModel().addChangeListener(this);
        c.setBorder(null);
        mdls2sliders.remove(((JSlider)c).getModel());
        c.removePropertyChangeListener(this);
        c.removeFocusListener(this);
    }
    private static final int FOCUS_GAP = 2;

    @Override
    public void paint(Graphics g, JComponent c) {
        Rectangle clip = new Rectangle(0, 0, c.getWidth(), c.getHeight());

        clip = clip.intersection(g.getClipBounds());
        g.setClip(clip);
        JSlider js = (JSlider)c;
        String val;
        StringConverter conv = (StringConverter) js.getClientProperty("converter");
        if (conv != null) {
            val = conv.valueToString(js);
        } else {
            val = Integer.toString(js.getValue());
        }

        g.setFont(js.getFont());
        FontMetrics fm = g.getFontMetrics();
        Insets ins = js.getInsets();

        // Leave room for the focus rectangle
        ins.left += FOCUS_GAP;
        ins.right += FOCUS_GAP;
        ins.top += FOCUS_GAP;
        ins.bottom += FOCUS_GAP;
        int h = fm.getHeight();
        int w = fm.stringWidth(val);
        int txtY = fm.getMaxAscent() + ins.top;
        int txtX = c.getWidth() -
                   (w + ins.right + FOCUS_GAP + downArrow.getIconWidth() - 2);

        g.setColor(js.isEnabled() ? js.getForeground()
                                  : UIManager.getColor("controlDkShadow"));
        g.setColor(Color.BLACK);
        g.drawString(val, txtX, txtY);
        if (c.hasFocus() && c.getBorder() != raised) {
            Color col = UIManager.getColor("controlShadow");

            g.setColor(col);
            g.drawRect(ins.left, ins.top,
                       js.getWidth() - (ins.left + ins.right),
                       js.getHeight() - (ins.top + ins.bottom));
        }
        int iconY = txtY + 1;
        int iconX = c.getWidth() - (ins.right + downArrow.getIconWidth());
        Icon ic = containsMouse(js) ? downArrowLit
                                    : downArrow;

        ic.paintIcon(js, g, iconX, iconY);
        g.setColor(UIManager.getColor("controlShadow"));
        g.drawLine(ins.left, iconY, txtX + w, iconY);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Graphics g = c.getGraphics();
        boolean created = false;
        if (g == null) {
            g = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().
                    getDefaultConfiguration().createCompatibleImage(2,2).
                    getGraphics();
            created = true;
        }
        try {
            g.setFont(c.getFont());
            JSlider js = (JSlider)c;
            int maxchars = Math.max(Integer.toString(js.getMaximum()).length(),
                                    Integer.toString(js.getMinimum()).length());
            int w = g.getFontMetrics().charWidth('A') * maxchars;
            int h = g.getFontMetrics().getHeight();

            w += downArrow.getIconWidth()/2;
            Insets ins = c.getInsets();

            w += ins.left + ins.right + (FOCUS_GAP*2) +
                 (downArrow.getIconWidth() - 2);
            h += ins.top + ins.bottom + (FOCUS_GAP*2) + 3;
            return new Dimension(w, h);
        } finally {
            if (created) {
                g.dispose();
            }
        }
    }
    
    @Override
    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }
    Popup currPopup = null;
    JSlider popupOwner = null;
    JSlider sliderInPopup = null;

    private void showPopup(JSlider js, Point p) {
        if (currPopup != null) {
            hidePopup();
        }
        p.y += js.getHeight()/2;
        popupOwner = js;
        sliderInPopup = new JSlider();
        SimpleSliderUI ui = (SimpleSliderUI)SimpleSliderUI.createUI(sliderInPopup);
        SimpleSliderUI.StringConverter conv = (SimpleSliderUI.StringConverter) js.getClientProperty("converter");
        if (conv != null) {
            ui.setStringConverter (conv);
        }
        sliderInPopup.setUI(ui);
        sliderInPopup.setBorder(BorderFactory.createLineBorder(UIManager.getColor("controlShadow")));
        sliderInPopup.setOrientation(js.getOrientation());
        sliderInPopup.setOrientation(js.getOrientation());
        sliderInPopup.setPaintLabels(true);
        sliderInPopup.addFocusListener(this);
        SwingUtilities.convertPointToScreen(p, js);
        Dimension psize = sliderInPopup.getPreferredSize();
        Rectangle r = js.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();
        Rectangle rf = js.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();
        Rectangle test = new Rectangle (p, psize);
        if (!r.contains(test)) {
            int offy = Math.max (0, (test.y + test.height) - (rf.y + rf.height));
            int offx = Math.max (0, (test.x + test.width) - (rf.x + rf.width));
            p.x -= offx;
            p.y -= offy;
        }
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
        sliderInPopup.setModel(js.getModel());
        currPopup = PopupFactory.getSharedInstance().getPopup(js, sliderInPopup,
                                                              p.x, p.y);
        currPopup.show();
        popupOwner.addMouseMotionListener(this);
    }

    private void hidePopup() {
        if (currPopup == null) {
            return;
        }
        currPopup.hide();
        sliderInPopup.removeFocusListener(this);
        // Whoever designed these method names really liked typing...
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(this);
        // Do this so our slider's model won't hold a permanent
        // reference to our popup slider by listening on its model
        sliderInPopup.setModel(new DefaultBoundedRangeModel());
        popupOwner.removeMouseMotionListener(this);
        popupOwner = null;
        sliderInPopup = null;
        currPopup = null;
    }

    public void mouseClicked(MouseEvent e) {
    }

    private SimpleSliderUI uiOf(JSlider sl) {
        return (SimpleSliderUI)sl.getUI();
    }
    private Reference containingMouse = null;

    public void mouseEntered(MouseEvent e) {
        JSlider js = (JSlider)e.getSource();

        containingMouse = new WeakReference(e.getSource());
        js.repaint();
    }

    private boolean containsMouse(JSlider slider) {
        if (containingMouse != null) {
            return containingMouse.get() == slider;
        }
        return false;
    }

    public void mouseExited(MouseEvent e) {
        JSlider js = (JSlider)e.getSource();

        containingMouse = null;
        js.repaint();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        JSlider js = (JSlider)e.getSource();
        boolean horiz = js.getOrientation() == JSlider.HORIZONTAL;
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
            if (currPopup != null) {
                hidePopup();
            } else {
                Point p = js.getLocation();
                SwingUtilities.convertPointToScreen(p, js);
                showPopup(js,
                      new Point(p.x + js.getWidth()/2,
                                p.y + js.getHeight()));
            }
            e.consume();
        }
        else if (((key == KeyEvent.VK_UP && horiz) ||
                  (key == KeyEvent.VK_DOWN && !horiz)) ||
                 key == KeyEvent.VK_RIGHT) {
            int val = js.getValue();

            if (val < js.getMaximum()) {
                val++;
                js.setValue(val);
            }
            e.consume();
        }
        else if (((key == KeyEvent.VK_DOWN && horiz) ||
                  (key == KeyEvent.VK_UP && !horiz)) || key == KeyEvent.VK_LEFT) {
            int val = js.getValue();

            if (val >= js.getMinimum()) {
                val--;
                js.setValue(val);
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            hidePopup();
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void focusGained(FocusEvent e) {
        e.getComponent().repaint();
    }

    public void focusLost(FocusEvent e) {
        e.getComponent().repaint();
    }

    public void stateChanged(ChangeEvent e) {
        BoundedRangeModel mdl = (BoundedRangeModel)e.getSource();
        JSlider js = findSlider(mdl);

        js.repaint();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof JSlider) {
            if ("model".equals(evt.getPropertyName())) {
                JSlider js = (JSlider)evt.getSource();

                mdls2sliders.put(js.getModel(), new WeakReference(js));
            }
        }
        else if (evt.getSource() instanceof KeyboardFocusManager) {
            System.err.println("GOT: " + evt.getPropertyName());
            if ("activeWindow".equals(evt.getPropertyName())) {
                hidePopup();
            }
            else if (!"permanentFocusOwner".equals(evt.getPropertyName()) ||
                     evt.getNewValue() == sliderInPopup ||
                     evt.getNewValue() == null)
                if ("focusOwner".equals(evt.getPropertyName())) {
                    if (!(evt.getNewValue() instanceof JSlider) &&
                        evt.getNewValue() != null) {
                        System.err.println("Hide for focus gone to " +
                                           evt.getNewValue());
                        hidePopup();
                    }
                }
        }
    }

    public void mousePressed(MouseEvent e) {
        JSlider js = (JSlider)e.getSource();
        boolean horiz = js.getOrientation() == JSlider.HORIZONTAL;

        js.requestFocus();
        if (!e.isPopupTrigger() && e.getClickCount() == 1) {
            showPopup((JSlider)e.getSource(), e.getPoint());
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (popupOwner != null && sliderInPopup.isShowing()) {
            JSlider js = (JSlider)e.getSource();
            boolean horiz = js.getOrientation() == JSlider.HORIZONTAL;

            uiOf(sliderInPopup).dragTo(horiz ? e.getX()
                                             : e.getY(), sliderInPopup);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (currPopup != null) {
            uiOf(sliderInPopup).clearDetent();
            hidePopup();
            e.consume();
        }
    }

    public void mouseMoved(MouseEvent e) {
    }
}
