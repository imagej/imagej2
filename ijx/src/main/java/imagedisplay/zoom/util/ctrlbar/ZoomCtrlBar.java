package imagedisplay.zoom.util.ctrlbar;

import imagedisplay.zoom.core.ZoomScrollPane;
import javax.swing.*;
import java.util.*;

/**
 * zoom ctrl bar base class
 *
 * If you need to modify this file or use this library in your own project, please
 * let me know. Thanks!
 *
 * @author Qiang Yu (qiangyu@gmail.com)
 */
public abstract class ZoomCtrlBar extends JToolBar {
    
    protected LinkedList zoomScrollPanes = new LinkedList();
    
    public void addZoomScrollPane(ZoomScrollPane zsp) {
        if (!zoomScrollPanes.contains(zsp)) {
            this.zoomScrollPanes.add(zsp);
        }
    }

    public void removeScrollPane(ZoomScrollPane zsp) {
        this.zoomScrollPanes.remove(zsp);
    }

    public void fitToScreen() {
        Iterator it = zoomScrollPanes.iterator();
        while (it.hasNext()) {
            ZoomScrollPane zp = (ZoomScrollPane) it.next();
            zp.fitToScreen();
        }
    }

    public void restore() {
        Iterator it = zoomScrollPanes.iterator();
        while (it.hasNext()) {
            ZoomScrollPane zp = (ZoomScrollPane) it.next();
            zp.restore();
        }
    }
}
