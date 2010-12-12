package imagedisplay;

import imagedisplay.zoom.core.ZoomGraphics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.event.MouseInputAdapter;

/**
 * PointMarker - when enabled, allows points to be marked by clicking the mouse on a point.
 * Marked points are displayed/overlayed on the image pane.  A list of points is maintained.
 * 
 * @todo Add action/button to getMarkedPoints...
 * 
 * @author GBH
 *  // Modes: 
    // 1) set a visible marker at one reference point;
    // 2) repeatedly mark points, adding them to an ArrayList, until mode is cancelled
    // add any points that are clicked to the list of marked points
    // Clear the list of marked points
 */
public class PointMarker implements ImageViewerPlugin {

    ImagePanelZoomable iPane;
    private boolean showMarkedPoints = true;
    GraphicOverlay overlay;
    ArrayList<Point> markedPointsList = new ArrayList<Point>();
    MouseListenerPointMarker mListenerPointMarker = new MouseListenerPointMarker();

    public PointMarker(ImagePanelZoomable iPane) {
        this.iPane = iPane;

        overlay = new GraphicOverlay() {

            public void drawGraphicOverlay(ZoomGraphics zg) {
                if (isShowMarkedPoints()) {
                    for (Point p : markedPointsList) {
                        int wid = 3;
                        zg.setColor(Color.black);
                        zg.setStroke(new BasicStroke(2.0f));
                        zg.drawRect(p.x, p.y, wid, wid);
                        zg.setColor(Color.yellow);
                        zg.setStroke(new BasicStroke(1.0f));
                        zg.drawRect(p.x, p.y, wid, wid);
                    }
                }
            }

        };
    }

    public void toggleMarkReferencePoint(boolean on) {
        if (on) {
            iPane.setMouseInputAdapter(mListenerPointMarker);
            // for custom Cursor
            Toolkit toolkit = Toolkit.getDefaultToolkit();
                URL url = getClass().getResource("icons/pointCursor.gif");
            Image cursorImage = toolkit.getImage(url);
            Point cursorHotSpot = new Point(15, 15);
            Cursor customCursor = toolkit.createCustomCursor(cursorImage, cursorHotSpot, "Cursor");
            iPane.setCursor(customCursor);
            //iPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            iPane.restoreDefaultMouseInputAdapter();
            iPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    private void markCurrentPoint(MouseEvent e) {
        Point2D zc = iPane.toUserSpace(e.getX(), e.getY());
        int x = (int) (zc.getX());
        int y = (int) (zc.getY());
        Point p = new Point(x, y);
        System.out.println("Marked point: " + p);
        markedPointsList.add(p);
    }

    public ArrayList<Point> getMarkedPoints() {
        return markedPointsList;
    }

    public void clearMarkedPoints() {
        markedPointsList.clear();
    }

    public void setShowMarkedPoints(boolean t) {
        showMarkedPoints = t;
    }

    private boolean isShowMarkedPoints() {
        return showMarkedPoints;
    }

    class MouseListenerPointMarker
        extends MouseInputAdapter {

        public void mouseClicked(MouseEvent e) {
            markCurrentPoint(e);
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
            iPane.valuePoint(e);
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

    }

    @Override
    public List<AbstractButton> getButtons() {
        List<AbstractButton> buttons = new ArrayList<AbstractButton>();
        // JToggleButton and JButton are both javax.swing.AbstractButton

        // Toggle button
        JToggleButton buttonPoint = new JToggleButton();
        buttonPoint.setMargin(new Insets(0, 0, 0, 0));
        buttonPoint.setMinimumSize(new Dimension(16, 16));
        buttonPoint.setToolTipText("Mark reference point");
        try {
            buttonPoint.setIcon(new ImageIcon(PointMarker.class.getResource("icons/markPoint.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        buttonPoint.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                int state = itemEvent.getStateChange();
                if (state == ItemEvent.SELECTED) {
                    toggleMarkReferencePoint(true);
                } else {
                    toggleMarkReferencePoint(false);
                }
            }

        });
        buttonPoint.setToolTipText("Mark reference point");
        buttonPoint.setMargin(new Insets(0, 0, 0, 0));
        buttonPoint.setMinimumSize(new Dimension(16, 16));
        buttons.add(buttonPoint);
        // Clear Points button
        JButton buttonClearPoints = new JButton();
        buttonClearPoints.setMargin(new Insets(0, 0, 0, 0));
        buttonClearPoints.setMinimumSize(new Dimension(16, 16));
        buttonClearPoints.setToolTipText("Clear marked points");
        try {
            buttonClearPoints.setIcon(new ImageIcon(PointMarker.class.getResource(
                "icons/markPointClear.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        buttonClearPoints.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                clearMarkedPoints();
            }

        });
        buttons.add(buttonClearPoints);
        return buttons;
    }

    @Override
    public GraphicOverlay getOverlay() {
        return this.overlay;
    }

}
