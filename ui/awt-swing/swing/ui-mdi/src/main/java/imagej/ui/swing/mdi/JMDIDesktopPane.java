
/*
Adapted from JHotDraw7's org.jhotdraw.gui.JMDIDesktopPane class.

Licensed under CC BY 2.5, as stated on the JHotDraw7 web site.

Original copyright notice follows.
*/

/*
 * @(#)JMDIDesktopPane.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */

package imagej.ui.swing.mdi;

import imagej.ui.Arrangeable;
import imagej.ui.Desktop;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.beans.PropertyVetoException;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

/**
 * An extension of JDesktopPane that supports often used MDI functionality. This
 * class also handles setting scroll bars for when windows move too far to the left or
 * bottom, providing the JMDIDesktopPane is in a ScrollPane.
 * Note by dnoyeb: I dont know why the container does not fire frame close events when the frames
 * are removed from the container with remove as opposed to simply closed with the
 * "x".  so if you say removeAll from container you wont be notified.  No biggie.
 *
 * @author Werner Randelshofer
 * Original version by 
 * Wolfram Kaiser (adapted from an article in JavaWorld), 
 * C.L.Gilbert <dnoyeb@users.sourceforge.net>
 */
public class JMDIDesktopPane extends JDesktopPane implements  Desktop {
    private MDIDesktopManager manager;
    
    public JMDIDesktopPane() {
        manager = new MDIDesktopManager(this);
        setDesktopManager(manager);
        setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    @Override
    public void setArrangement(Arrangeable.Arrangement newValue) {
        Arrangeable.Arrangement oldValue = getArrangement();
        switch (newValue) {
            case CASCADE :
                arrangeFramesCascading();
                break;
            case HORIZONTAL :
                arrangeFramesHorizontally();
                break;
            case VERTICAL :
                arrangeFramesVertically();
                break;
        }
        firePropertyChange("arrangement", oldValue, newValue);
    }
    
    
    @Override
    public Arrangeable.Arrangement getArrangement() {
        // FIXME Check for the arrangement of the JInternalFrames here
        // and return the true value
        return Arrangeable.Arrangement.CASCADE;
    }
    /**
     * Cascade all internal frames
     */
    private void arrangeFramesCascading() {
        JInternalFrame[] allFrames = getAllFrames();
        
        // do nothing if no frames to work with
        if (allFrames.length == 0) {
            return;
        }
        
        manager.setNormalSize();
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int frameOffset=0;
        for (int i = allFrames.length - 1; i >= 0; i--) {
            Point p=SwingUtilities.convertPoint(allFrames[i].getContentPane(),0,0,allFrames[i]);
            frameOffset=Math.max(frameOffset,Math.max(p.x,p.y));
        }
        int frameHeight = (getBounds().height-insets.top-insets.bottom) - allFrames.length * frameOffset;
        int frameWidth = (getBounds().width-insets.left-insets.right) - allFrames.length * frameOffset;
        for (int i = allFrames.length - 1; i >= 0; i--) {
            try {
                allFrames[i].setMaximum(false);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
            
            allFrames[i].setBounds(x, y, frameWidth, frameHeight);
            x = x + frameOffset;
            y = y + frameOffset;
        }
        
        checkDesktopSize();
    }
    
    public void tileFramesHorizontally() {
        Component[] allFrames = getAllFrames();
        
        // do nothing if no frames to work with
        if (allFrames.length == 0) {
            return;
        }
        
        manager.setNormalSize();
        
        int frameHeight = getBounds().height/allFrames.length;
        int y = 0;
        for (int i = 0; i < allFrames.length; i++) {
            try {
                ((JInternalFrame)allFrames[i]).setMaximum(false);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
            
            allFrames[i].setBounds(0, y, getBounds().width,frameHeight);
            y = y + frameHeight;
        }
        
        checkDesktopSize();
    }
    
    public void tileFramesVertically() {
        Component[] allFrames = getAllFrames();
        
        // do nothing if no frames to work with
        if (allFrames.length == 0) {
            return;
        }
        manager.setNormalSize();
        
        int frameWidth = getBounds().width/allFrames.length;
        int x = 0;
        for (int i = 0; i < allFrames.length; i++) {
            try {
                ((JInternalFrame)allFrames[i]).setMaximum(false);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
            
            allFrames[i].setBounds(x, 0, frameWidth, getBounds().height);
            x = x + frameWidth;
        }
        
        checkDesktopSize();
    }
    
    /**
     * Arranges the frames as efficiently as possibly with preference for
     * keeping vertical size maximal.<br>
     *
     */
    public void arrangeFramesVertically() {
        Component[] allFrames = getAllFrames();
        // do nothing if no frames to work with
        if (allFrames.length == 0) {
            return;
        }
        
        manager.setNormalSize();
        
        int vertFrames = (int)Math.floor(Math.sqrt(allFrames.length));
        int horFrames = (int)Math.ceil(Math.sqrt(allFrames.length));
        
        // first arrange the windows that have equal size
        int frameWidth = getBounds().width / horFrames;
        int frameHeight = getBounds().height / vertFrames;
        int x = 0;
        int y = 0;
        int frameIdx = 0;
        for (int horCnt = 0; horCnt < horFrames-1; horCnt++) {
            y = 0;
            for (int vertCnt = 0; vertCnt < vertFrames; vertCnt++) {
                try {
                    ((JInternalFrame)allFrames[frameIdx]).setMaximum(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
                
                allFrames[frameIdx].setBounds(x, y, frameWidth, frameHeight);
                frameIdx++;
                y = y + frameHeight;
            }
            x = x + frameWidth;
        }
        
        // the rest of the frames are tiled down on the last column with equal
        // height
        frameHeight = getBounds().height / (allFrames.length - frameIdx);
        y = 0;
        for (; frameIdx < allFrames.length; frameIdx++) {
            try {
                ((JInternalFrame)allFrames[frameIdx]).setMaximum(false);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
            
            allFrames[frameIdx].setBounds(x, y, frameWidth, frameHeight);
            y = y + frameHeight;
        }
        
        checkDesktopSize();
    }
    
    /**
     * Arranges the frames as efficiently as possibly with preference for
     * keeping horizontal size maximal.<br>
     *
     */
    public void arrangeFramesHorizontally() {
        Component[] allFrames = getAllFrames();
        // do nothing if no frames to work with
        if (allFrames.length == 0) {
            return;
        }
        
        manager.setNormalSize();
        
        int vertFrames = (int)Math.ceil(Math.sqrt(allFrames.length));
        int horFrames = (int)Math.floor(Math.sqrt(allFrames.length));
        
        // first arrange the windows that have equal size
        int frameWidth = getBounds().width / horFrames;
        int frameHeight = getBounds().height / vertFrames;
        int x = 0;
        int y = 0;
        int frameIdx = 0;
        for (int vertCnt = 0; vertCnt < vertFrames-1; vertCnt++) {
            x = 0;
            for (int horCnt = 0; horCnt < horFrames; horCnt++) {
                try {
                    ((JInternalFrame)allFrames[frameIdx]).setMaximum(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
                
                allFrames[frameIdx].setBounds(x, y, frameWidth, frameHeight);
                frameIdx++;
                x = x + frameWidth;
            }
            y = y + frameHeight;
        }
        
        // the rest of the frames are tiled down on the last column with equal
        // height
        frameWidth = getBounds().width / (allFrames.length - frameIdx);
        x = 0;
        for (; frameIdx < allFrames.length; frameIdx++) {
            try {
                ((JInternalFrame)allFrames[frameIdx]).setMaximum(false);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
            
            allFrames[frameIdx].setBounds(x, y, frameWidth, frameHeight);
            x = x + frameWidth;
        }
        
        checkDesktopSize();
    }
    
    /**
     * Sets all component size properties ( maximum, minimum, preferred)
     * to the given dimension.
     */
    public void setAllSize(Dimension d) {
        setMinimumSize(d);
        setMaximumSize(d);
        setPreferredSize(d);
        setBounds(0, 0, d.width, d.height);
    }
    
    /**
     * Sets all component size properties ( maximum, minimum, preferred)
     * to the given width and height.
     */
    public void setAllSize(int width, int height) {
        setAllSize(new Dimension(width,height));
    }
    
    private void checkDesktopSize() {
        if ((getParent() != null) && isVisible()) {
            manager.resizeDesktop();
        }
    }
    
}
/**
 * Private class used to replace the standard DesktopManager for JDesktopPane.
 * Used to provide scrollbar functionality.
 */
class MDIDesktopManager extends DefaultDesktopManager {
    private JMDIDesktopPane desktop;
    
    public MDIDesktopManager(JMDIDesktopPane newDesktop) {
        this.desktop = newDesktop;
    }
    
    @Override
    public void endResizingFrame(JComponent f) {
        super.endResizingFrame(f);
        resizeDesktop();
    }
    
    @Override
    public void endDraggingFrame(JComponent f) {
        super.endDraggingFrame(f);
        resizeDesktop();
    }
    
    public void setNormalSize() {
        JScrollPane scrollPane = getScrollPane();
        Insets scrollInsets = getScrollPaneInsets();
        
        if (scrollPane != null) {
            Dimension d = scrollPane.getVisibleRect().getSize();
            if (scrollPane.getBorder() != null) {
                d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
                        d.getHeight() - scrollInsets.top - scrollInsets.bottom);
            }
            
            d.setSize(d.getWidth() - 20, d.getHeight() - 20);
            desktop.setAllSize(d);
            scrollPane.invalidate();
            scrollPane.validate();
        }
    }
    
    private Insets getScrollPaneInsets() {
        JScrollPane scrollPane = getScrollPane();
        if ((scrollPane == null) || (getScrollPane().getBorder() == null)) {
            return new Insets(0, 0, 0, 0);
        }
        return getScrollPane().getBorder().getBorderInsets(scrollPane);
    }
    
    public JScrollPane getScrollPane() {
        if (desktop.getParent() instanceof JViewport) {
            JViewport viewPort = (JViewport)desktop.getParent();
            if (viewPort.getParent() instanceof JScrollPane)
                return (JScrollPane)viewPort.getParent();
        }
        return null;
    }
    
    protected void resizeDesktop() {
        int x = 0;
        int y = 0;
        JScrollPane scrollPane = getScrollPane();
        Insets scrollInsets = getScrollPaneInsets();
        
        if (scrollPane != null) {
            JInternalFrame allFrames[] = desktop.getAllFrames();
            for (int i = 0; i < allFrames.length; i++) {
                if (allFrames[i].getX() + allFrames[i].getWidth() > x) {
                    x = allFrames[i].getX() + allFrames[i].getWidth();
                }
                if (allFrames[i].getY() + allFrames[i].getHeight() > y) {
                    y = allFrames[i].getY() + allFrames[i].getHeight();
                }
            }
            Dimension d=scrollPane.getVisibleRect().getSize();
            if (scrollPane.getBorder() != null) {
                d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
                        d.getHeight() - scrollInsets.top - scrollInsets.bottom);
            }
            
            if (x <= d.getWidth()) {
                x = ((int)d.getWidth()) - 20;
            }
            if (y <= d.getHeight()) {
                y = ((int)d.getHeight()) - 20;
            }
            desktop.setAllSize(x,y);
            scrollPane.invalidate();
            scrollPane.validate();
        }
    }
}
