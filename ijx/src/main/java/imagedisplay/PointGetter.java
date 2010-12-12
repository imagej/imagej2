package imagedisplay;

import imagedisplay.util.StaticSwingUtils;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import javax.swing.event.MouseInputAdapter;

/**
 * This is used in getting a point from an ImagePanel
 *
 * @author GBH
 */
public class PointGetter {

    ImagePanelZoomable iPane;
    GraphicOverlay overlay;
    MouseListenerPointGetter mListenerPointGetter = new MouseListenerPointGetter();
    Point thePoint;
    PointGetterCallback caller;

    public PointGetter(ImagePanelZoomable iPane) {
        this.iPane = iPane;
    }

    public void setCallback(PointGetterCallback caller) {
        this.caller = caller;
    }

    public void setupToPoint() {
        iPane.setMouseInputAdapter(mListenerPointGetter);
        // for custom Cursor
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        URL url = getClass().getResource("icons/pointCursor.gif");
        Image cursorImage = toolkit.getImage(url);
        Point cursorHotSpot = new Point(15, 15);
        Cursor customCursor = toolkit.createCustomCursor(cursorImage, cursorHotSpot,
            "Cursor");
        iPane.setCursor(customCursor);
    }

    private void getCurrentPoint(final MouseEvent e) {
        StaticSwingUtils.dispatchToEDT(new Runnable() {

            public void run() {
                Point2D zc = iPane.toUserSpace(e.getX(), e.getY());
                int x = (int) (zc.getX());
                int y = (int) (zc.getY());
                thePoint = new Point(x, y);
                iPane.restoreDefaultMouseInputAdapter();
                iPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                caller.callbackFromPointGetter(thePoint);
            }

        });
    }

    class MouseListenerPointGetter
        extends MouseInputAdapter {

        public void mouseClicked(MouseEvent e) {
            getCurrentPoint(e);
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
}
