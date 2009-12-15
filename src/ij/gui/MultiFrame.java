package ij.gui;

/*
 * $Id: MultiFrame.java,v 1.7 2007/07/06 20:45:32 jeffmc Exp $
 *
 * Copyright  1997-2004 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */



//import ucar.unidata.util.GuiUtils;
//import ucar.unidata.util.LogUtil;
//import ucar.unidata.util.Misc;



import java.awt.*;
import java.awt.event.*;

import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.*;



/**
 * A class that holds either a JFrame or a JInteralFrame. It allows for client
 * code to eqaily switch between regular windows and JDesktop windows
 *
 * @author IDV development team
 */
public class MultiFrame {

    /** Global to add the internal frames to as a default behavior */
    private static JDesktopPane desktopPane;

    /** Used for event processing */
    private static Window dummyWindow;




    /** The frame */
    private JFrame frame;

    /** The internal frame */
    private JInternalFrame internalFrame;

    /** mapping from windowlistener to internalframelistener */
    private Hashtable listeners = new Hashtable();


    /**
     * Set the global desktopPane. This causes all default MultiFrames
     * to be an internalFrame.
     *
     * @param desktopPane desktop pane
     */
    public static void useDesktopPane(JDesktopPane desktopPane) {
        MultiFrame.desktopPane = desktopPane;
    }



    /**
     * ctor
     */
    public MultiFrame() {
        this("");
    }


    /**
     * ctor
     *
     * @param title Create a JFrame with the given title
     */
    public MultiFrame(String title) {
        if (desktopPane != null) {
            internalFrame = new JInternalFrame(title, true, true, true, true);
            desktopPane.add(internalFrame);
        } else {
            frame = new JFrame(title);
        }
    }

    /**
     * ctor
     *
     * @param frame The frame
     */
    public MultiFrame(JFrame frame) {
        this.frame = frame;
    }

    /**
     * ctor
     *
     * @param internalFrame The internal frame
     */
    public MultiFrame(JInternalFrame internalFrame) {
        this.internalFrame = internalFrame;
    }


    /**
     * Show the component
     */
    public void show() {
        if (frame != null) {
            frame.show();
        } else {
            internalFrame.show();
        }
    }


    /**
     * dispose of the component
     */
    public void dispose() {
        if (frame != null) {
            frame.dispose();
        } else {
            internalFrame.dispose();
        }
    }


    /**
     * set visibility of the component
     *
     * @param visible visible
     */
    public void setVisible(boolean visible) {
        if (frame != null) {
            frame.setVisible(visible);
        } else {
            internalFrame.setVisible(visible);
        }
    }

    /**
     * Get the content pane of the component
     *
     * @return content pane
     */
    public Container getContentPane() {
        if (frame != null) {
            return frame.getContentPane();
        } else {
            return internalFrame.getContentPane();
        }
    }


    /**
     * Get the container
     *
     * @return Either the frame or the internalFrame
     */
    public Container getContainer() {
        if (frame != null) {
            return frame;
        } else {
            return internalFrame;
        }
    }

    /**
     * Set the cursor
     *
     * @param cursor cursor
     */
    public void setCursor(Cursor cursor) {
        getWindow().setCursor(cursor);
    }


    /**
     * wrapper method
     *
     * @param state state
     */
    public void setState(int state) {
        if (frame != null) {
            frame.setState(state);
        } else {
            try {
                if (state == Frame.NORMAL) {
                    internalFrame.setIcon(false);
                } else {
                    internalFrame.setIcon(true);
                }
            } catch (Exception exc) {
               // LogUtil.logException("MultiFrame", exc);
            }
        }
    }

    /**
     * Set the title
     *
     * @param title The title
     */
    public void setTitle(String title) {
        if (frame != null) {
            frame.setTitle(title);
        } else {
            internalFrame.setTitle(title);
        }
    }


    /**
     * Get the title
     *
     * @return The title
     */
    public String getTitle() {
        if (frame != null) {
            return frame.getTitle();
        } else {
            return internalFrame.getTitle();
        }
    }


    /**
     * access the JFrame
     *
     * @return The frame
     */
    public JFrame getFrame() {
        return frame;
    }


    /**
     * Access the internal frame
     *
     * @return internal frame
     */
    public JInternalFrame getInternalFrame() {
        return internalFrame;
    }

    /**
     * Finds the Window we are a part of
     *
     * @return the window
     */
    public Window getWindow() {
        if (frame != null) {
            return frame;
        } else {
            return getWindow(getComponent());
        }

    }

    /**
     * wrapper method
     */
    public void pack() {
        if (frame != null) {
            frame.pack();
        } else {
            internalFrame.pack();
        }
    }

    /**
     * wrapper method
     *
     * @param operation operation
     */
    public void setDefaultCloseOperation(int operation) {
        if (frame != null) {
            frame.setDefaultCloseOperation(operation);
        } else {
            internalFrame.setDefaultCloseOperation(operation);
        }
    }





    /**
     * wrapper method
     *
     * @param l listener
     */
    public void addWindowListener(final WindowListener l) {
        if (frame != null) {
            frame.addWindowListener(l);
        } else {
            if (dummyWindow == null) {
                dummyWindow = new JFrame();
            }
            //Make a bridge here
            InternalFrameListener listener = new InternalFrameListener() {
                public void internalFrameActivated(InternalFrameEvent e) {
                    l.windowActivated(new WindowEvent(dummyWindow, 0));
                }

                public void internalFrameClosed(InternalFrameEvent e) {
                    l.windowClosed(new WindowEvent(dummyWindow, 0));
                }

                public void internalFrameClosing(InternalFrameEvent e) {
                    l.windowClosing(new WindowEvent(dummyWindow, 0));
                }

                public void internalFrameDeactivated(InternalFrameEvent e) {
                    l.windowDeactivated(new WindowEvent(dummyWindow, 0));
                }

                public void internalFrameDeiconified(InternalFrameEvent e) {
                    l.windowDeiconified(new WindowEvent(dummyWindow, 0));
                }

                public void internalFrameIconified(InternalFrameEvent e) {
                    l.windowIconified(new WindowEvent(dummyWindow, 0));
                }

                public void internalFrameOpened(InternalFrameEvent e) {
                    l.windowOpened(new WindowEvent(dummyWindow, 0));
                }
            };
            internalFrame.addInternalFrameListener(listener);
            listeners.put(l, listener);

        }
    }


    /**
     * wrapper method
     *
     * @param l _more_
     */
    public void removeWindowListener(WindowListener l) {
        //TODO
        if (frame != null) {
            frame.removeWindowListener(l);
        } else {
            InternalFrameListener listener =
                (InternalFrameListener) listeners.get(l);
            if (listener != null) {
                listeners.remove(l);
                internalFrame.removeInternalFrameListener(listener);
            }

        }
    }

    /**
     * wrapper method
     *
     * @param menuBar _more_
     */
    public void setJMenuBar(JMenuBar menuBar) {
        if (frame != null) {
            frame.setJMenuBar(menuBar);
        } else {
            internalFrame.setJMenuBar(menuBar);
        }
    }

    /**
     * wrapper method
     *
     * @return _more_
     */
    public Rectangle getBounds() {
        return getComponent().getBounds();
    }

    /**
     * wrapper method
     *
     * @param bounds _more_
     */
    public void setBounds(Rectangle bounds) {
        if (frame != null) {
            frame.setBounds(bounds);
        } else {
            internalFrame.setBounds(bounds);
        }
    }

    /**
     * wrapper method
     *
     * @param icon _more_
     */
    public void setIconImage(Image icon) {
        if (frame != null) {
            frame.setIconImage(icon);
        } else {
            //            internalFrame.setFrameIcon(new ImageIcon(icon));
        }
    }

    /**
     * wrapper method
     */
    public void toFront() {
        if (frame != null) {
            toFront(frame);
        } else {
            toFront(getWindow());
            internalFrame.toFront();
        }
    }

    /**
     * Return the component. Either the frame or the internalFrame
     *
     * @return The component
     */
    public Component getComponent() {
        if (frame != null) {
            return frame;
        } else {
            return internalFrame;
        }
    }

    /**
     * wrapper method
     *
     * @return is visible
     */
    public boolean isVisible() {
        return getComponent().isVisible();
    }


    /**
     * wrapper method
     *
     * @return is showing
     */
    public boolean isShowing() {
        if (frame != null) {
            return frame.isShowing();
        } else {
            return getWindow().isShowing() && !internalFrame.isIcon();
        }
    }

    /**
     * wrapper method
     *
     * @return location
     */
    public Point getLocation() {
        return getComponent().getLocation();
    }


    /**
     * wrapper method
     *
     * @param x x
     * @param y y
     */
    public void setLocation(int x, int y) {
        if (frame != null) {
            frame.setLocation(x, y);
        } else {
            //TODO?
        }
    }


    /**
     * wrapper method
     *
     * @param size size_
     */
    public void setSize(Dimension size) {
        getComponent().setSize(size);
    }


    /**
     * wrapper method
     *
     * @return size
     */
    public Dimension getSize() {
        return getComponent().getSize();
    }

    /**
     * wrapper method
     *
     * @return state
     */
    public int getState() {
        if (frame != null) {
            return frame.getState();
        }
        if (internalFrame.isIcon()) {
            return Frame.ICONIFIED;
        } else {
            return Frame.NORMAL;
        }
    }

        public static Window getWindow(Component component) {
        if (component == null) {
            return null;
        }
        Component parent = component.getParent();
        while (parent != null) {
            if (parent instanceof Window) {
                return (Window) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
    public static void toFront(Window window) {
        window.setVisible(true);
        window.toFront();
        if (window instanceof Frame) {
            Frame f = (Frame) window;
            //            f.setExtendedState(Frame.ICONIFIED);
            //            f.setExtendedState(Frame.NORMAL);
        }


    }


}

