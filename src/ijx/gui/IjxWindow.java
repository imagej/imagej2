package ijx.gui;

/**
 * IjxWindow: interface for a generalized Window in ImageJ
 * 
 * Refactored from ImageJ by Grant B. Harris, November 2008, at ImageJ 2008, Luxembourg
 **/
public interface IjxWindow {

    String getTitle();

    void setTitle(String s);

    boolean isVisible();

    void setVisible(boolean b);

    java.awt.Dimension getSize();

    java.awt.Point getLocation();

    java.awt.Point getLocationOnScreen();

    java.awt.Rectangle getBounds();

    void setLocation(int x, int y);

    void setLocation(java.awt.Point p);

    void toFront();

    boolean isClosed();

    void close();
    
    boolean canClose();

    void dispose();

}
