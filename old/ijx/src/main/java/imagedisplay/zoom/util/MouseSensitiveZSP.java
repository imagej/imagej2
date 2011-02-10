package imagedisplay.zoom.util;

import imagedisplay.zoom.core.ZoomJPanel;
import imagedisplay.zoom.core.ZoomScrollPane;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;


/**
 * ZoomScrollPane reacts to user's mouse click.
 * also a demo of how to use zoomscrollpane and zoomjpanel *
 * @author  Qiang Yu (qiangyu@gmail.com)
 */
public class MouseSensitiveZSP
      extends ZoomScrollPane implements MouseMotionListener, MouseListener
{
   public final static int ZOOMOUT_MODE = -1; //in this mode, mouse left-click zooms out
   public final static int DEFAULT_MODE = 0; //in this mode, nothing follows mouse-click
   public final static int ZOOMIN_MODE = 1; //in this mode, mouse left-click zooms in

   private boolean mouseInteractionEnabled = false;
   private Cursor zoomInCursor = null, zoomOutCursor = null,
   defaultCursor = null;
   private int mode = DEFAULT_MODE;
   /**
    * varibles help draw feedbacks
    */

   BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
         BasicStroke.JOIN_MITER, 2.0f, new float[] {2.0f}, 0.0f);
   double zoomffactor = 0.5;
   int dragStartX = -1, dragStartY = -1;
   Rectangle zoomRect = null;
   Color zoomRectColor = Color.LIGHT_GRAY;
   // GBH: Added ROI selection functions
   Rectangle roiRect = null;
   Color roiRectColor = Color.YELLOW;
   ZoomJPanel view;
   /**
    * ctor
    * @param view
    * @param vsbPolicy
    * @param hsbPolicy
    */
   public MouseSensitiveZSP (ZoomJPanel view, int vsbPolicy, int hsbPolicy) {
      super(view, vsbPolicy, hsbPolicy);
      this.view = view;
   }


   /**
    * ctor
    * @param view
    */
   public MouseSensitiveZSP (ZoomJPanel view) {
      super(view);
         this.view = view;
   }


   /**
    * whether to enable mouse interaction or not
    * if mouse interaction is enabled, users can use mouse to do zooming based
    * on current mode (ZOOMIN_MODE, ZOOMOUT_MODE)
    * @param enable
    */
   public void enableMouseInteraction (boolean enable) {
      mouseInteractionEnabled = enable;
      if (zoomInCursor == null) {
         ResourceManager rm = new ResourceManager(
               "edu/mbl/jif/imaging/zoom/util/resources/images");
         Toolkit tk = Toolkit.getDefaultToolkit();
         zoomInCursor = tk.createCustomCursor(rm.getImage("zoomin_cursor.gif"),
               new Point(15, 15), "zoomin");
         zoomOutCursor = tk.createCustomCursor(rm.getImage("zoomoutcursor.gif"),
               new Point(15, 15), "zoomout");
         defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
      }
      if (mouseInteractionEnabled) {
         zoomPanel.addMouseListener(this);
         zoomPanel.addMouseMotionListener(this);
      } else {
         zoomPanel.removeMouseListener(this);
         zoomPanel.removeMouseMotionListener(this);
         mode = DEFAULT_MODE;
      }
   }


   /**
    * Set Current Zoom Mode
    * @param m
    */
   public void setZoomMode (int m) {
      mode = m;
   }


   /**
    * Get Current Zoom Mode
    * @return
    */
   public int getZoomMode () {
      return mode;
   }


   public Rectangle getRoiRect () {
      return roiRect;
   }


   public Rectangle setRoiRect (Rectangle roiNew) {
      Rectangle roiWas = roiRect;
      roiRect = roiNew;
      return roiWas;
   }


//   public void paintRoiRect() {
//      ZoomGraphics g = (ZoomGraphics) zoomPanel.getGraphics();
//      g.setXORMode(roiRectColor);
//      Stroke p = g.getStroke();
//      g.setStroke(dashed);
//      if (roiRect != null) {
//         g.drawRect(roiRect.x, roiRect.y, roiRect.width, roiRect.height);
//      }
//      super.zoomPanel.paintFront(g);
//      g.setPaintMode();
//      g.setStroke(p);
//      g.dispose();
//   }
   /**
    * mouse event handlers
    */

   public void mouseDragged (MouseEvent e) {
      if (mode == ZOOMIN_MODE) {
         roiRect = null;
         Graphics2D g = (Graphics2D) zoomPanel.getGraphics();
         g.setXORMode(zoomRectColor);
         Stroke p = g.getStroke();
         g.setStroke(dashed);
         if (zoomRect != null) {
            g.drawRect(zoomRect.x, zoomRect.y, zoomRect.width, zoomRect.height);
         }
         zoomRect = new Rectangle((dragStartX <= e.getX()) ? dragStartX : e.getX(),
               (dragStartY <= e.getY()) ? dragStartY : e.getY(),
               Math.abs(e.getX() - dragStartX), Math.abs(e.getY() - dragStartY));
         g.drawRect(zoomRect.x, zoomRect.y, zoomRect.width, zoomRect.height);
         g.setPaintMode();
         g.setStroke(p);
         g.dispose();
      } else { // Select ROI mode
         zoomRect = null;
         Graphics2D g = (Graphics2D) zoomPanel.getGraphics();
         g.setXORMode(roiRectColor);
         Stroke p = g.getStroke();
         g.setStroke(dashed);
         if (roiRect != null) {
            g.drawRect(roiRect.x, roiRect.y, roiRect.width, roiRect.height);
         }
         roiRect = new Rectangle((dragStartX <= e.getX()) ? dragStartX : e.getX(),
               (dragStartY <= e.getY()) ? dragStartY : e.getY(),
               Math.abs(e.getX() - dragStartX), Math.abs(e.getY() - dragStartY));
         g.drawRect(roiRect.x, roiRect.y, roiRect.width, roiRect.height);
         g.setPaintMode();
         g.setStroke(p);
         g.dispose();
      }
   }


   public void mouseMoved (MouseEvent e) {
      System.out.println("x, y: " + e.getX() + ", " + e.getY());
      Point2D.Double zc = toUserSpace(e.getX(), e.getY());
      System.out.println("UserSpace = " + zc);
//       if((zc.getX() < imageWidth-1) &&
//       (zc.getX() < imageWidth-1)  ){}

   }


   public void mouseClicked (MouseEvent e) {
      if ((mode == ZOOMIN_MODE) || (mode == ZOOMOUT_MODE)) {
         doMouseZoom(e.getX(), e.getY());
      } else {
         roiRect = null;
         zoomPanel.repaint();
      }
   }


   public void mouseEntered (MouseEvent e) {
      if (mode == ZOOMIN_MODE) {
         zoomPanel.setCursor(zoomInCursor);
      }
      if (mode == ZOOMOUT_MODE) {
         zoomPanel.setCursor(zoomOutCursor);
      }
      if (mode == DEFAULT_MODE) {
         zoomPanel.setCursor(defaultCursor);
      }
   }


   public void mouseExited (MouseEvent e) {
      zoomPanel.setCursor(defaultCursor);
   }


   public void mousePressed (MouseEvent e) {
      dragStartX = e.getX();
      dragStartY = e.getY();
   }


   public void mouseReleased (MouseEvent e) {
      if (mode == ZOOMIN_MODE) {
         if (zoomRect != null) {
            Graphics2D g = (Graphics2D) zoomPanel.getGraphics();
            g.setXORMode(zoomRectColor);
            Stroke p = g.getStroke();
            g.setStroke(dashed);
            if (zoomRect != null) {
               g.drawRect(zoomRect.x, zoomRect.y, zoomRect.width, zoomRect.height);
            }
            if ((zoomRect.width >= 3) && (zoomRect.height >= 3)) {
               doMouseZoom(zoomRect);
            }
            zoomRect = null;
            g.setPaintMode();
            g.setStroke(p);
            g.dispose();
         }
      } else if (mode == DEFAULT_MODE) {
         if (roiRect != null) {
            Graphics2D g = (Graphics2D) zoomPanel.getGraphics();
            g.setXORMode(roiRectColor);
            Stroke p = g.getStroke();
            g.setStroke(dashed);
            if (roiRect != null) {
               g.drawRect(roiRect.x, roiRect.y, roiRect.width, roiRect.height);
            }
            g.setPaintMode();
            g.setStroke(p);
            g.dispose();

         }
      }
   }


   /**
    * if mouse dragged,
    * zoom the rectangle formed by mouse dragging so that it fills the whole window
    * @param r
    */
   private void doMouseZoom (Rectangle r) {
      zoomRectangleToWholeWindow(r);
   }


   /**
    * if mouse clicked, doing the zoom with the point clicked as the zoom center
    * @param zcx
    * @param zcy
    */
   private void doMouseZoom (int zcx, int zcy) {
      double zfx = zoomPanel.getZoomFactorX(), zfy = zoomPanel.getZoomFactorY();
      if (mode == ZOOMIN_MODE) {
         zfx /= zoomffactor;
         zfy /= zoomffactor;
      } else {
         zfx *= zoomffactor;
         zfy *= zoomffactor;
      }
      Point2D.Double zc = toUserSpace(zcx, zcy);
      zoom(zc.x, zc.y, zfx, zfy);
   }

}
