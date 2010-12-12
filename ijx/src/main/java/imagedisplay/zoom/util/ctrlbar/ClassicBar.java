package imagedisplay.zoom.util.ctrlbar;

import imagedisplay.zoom.core.ZoomScrollPane;
import imagedisplay.zoom.util.MouseSensitiveZSP;
import imagedisplay.zoom.util.ResourceManager;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import java.util.*;


/**
 * ClassicBar is a control bar which allows users to do zooming interactively
 * Originall authored by  Qiang Yu (qiangyu@gmail.com)
 */
public class ClassicBar
      extends ZoomCtrlBar
{
   private JButton zoomInButton = new JButton();
   private JButton zoomOutButton = new JButton();
   private JButton normalModeButton = new JButton();
   private JButton fitToScreenButton = new JButton();
   private JButton restoreButton = new JButton();
   private JButton zoomToRoiButton = new JButton();
   private JComboBox zoomFactorSelector = new JComboBox();

   /**
    * constructor
    */
   public ClassicBar () {
      super();

      String zoomFactors[] = {"25%", "50%", "x1", "x2", "x4", "x8", "x16",
      };

      setMargin(new Insets(1, 5, 1, 1));
      setFloatable(false);
      add(Box.createHorizontalGlue());

      zoomFactorSelector = new JComboBox(zoomFactors);
      zoomFactorSelector.setMinimumSize(new Dimension(32, 24));
      zoomFactorSelector.setMaximumSize(new Dimension(32, 24));
      zoomFactorSelector.setSelectedIndex(2);
      zoomFactorSelector.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent e) {
            /*
             * if(gotMouseEvent) { gotMouseEvent = false; return; }
             */
            zoomFactorSelected(e);
         }
      });

      add(normalModeButton);
      normalModeButton.setToolTipText("default");
      add(Box.createRigidArea(new Dimension(11, 1)));
      fitToScreenButton.setToolTipText("fit to screen");
      add(this.fitToScreenButton);
      add(Box.createRigidArea(new Dimension(2, 1)));
      restoreButton.setToolTipText("restore");
      add(this.restoreButton);
      add(Box.createRigidArea(new Dimension(2, 1)));
      add(this.zoomToRoiButton);
      zoomToRoiButton.setText("roi");
      add(Box.createRigidArea(new Dimension(2, 1)));
      add(zoomFactorSelector);
      add(Box.createRigidArea(new Dimension(2, 1)));

      add(this.zoomInButton);
      zoomInButton.setToolTipText("zoom in");
      add(Box.createRigidArea(new Dimension(2, 1)));
      zoomOutButton.setToolTipText("zoom out");

      add(this.zoomOutButton);
      add(Box.createRigidArea(new Dimension(2, 1)));
      add(Box.createHorizontalGlue());

      ResourceManager rm = new ResourceManager(
            "edu/mbl/jif/imaging/zoom/util/resources/images");

      zoomInButton.setIcon(rm.getImageIcon("zoomin16.gif"));
      zoomOutButton.setIcon(rm.getImageIcon("zoomout16.gif"));
      normalModeButton.setIcon(rm.getImageIcon("normal.gif"));
      fitToScreenButton.setIcon(rm.getImageIcon("fit2screen.gif"));
      restoreButton.setIcon(rm.getImageIcon("restore.gif"));

      zoomInButton.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent e) {
            setZoomMode(MouseSensitiveZSP.ZOOMIN_MODE);
         }
      });
      zoomOutButton.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent e) {
            setZoomMode(MouseSensitiveZSP.ZOOMOUT_MODE);
         }
      });
      restoreButton.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent e) {
            restore();
         }
      });

      normalModeButton.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent e) {
            setZoomMode(MouseSensitiveZSP.DEFAULT_MODE);
         }
      });

      fitToScreenButton.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent e) {
            fitToScreen();
         }
      });
      zoomToRoiButton.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent e) {
            zoomToRoi();
         }
      });

   }


   /**
    * set the zoom mode
    * zoom mode decides what happens when the zoomable JPanel is mouse clicked
    */
   public void setZoomMode (int zoomManner) {
      Iterator it = zoomScrollPanes.iterator();
      while (it.hasNext()) {
         ZoomScrollPane zsp = (ZoomScrollPane) it.next();
         if (zsp instanceof MouseSensitiveZSP) {
            ((MouseSensitiveZSP) zsp).setZoomMode(zoomManner);
         }
      }
   }


   /**
    * do the zooming
    */
   public void zoomFactorSelected (ActionEvent e) {
      JComboBox cb = (JComboBox) e.getSource();
      double zf = 1;

      int index = cb.getSelectedIndex();

      switch (index) {

         case 0:
            zf = 0.25;
            break;

         case 1:
            zf = 0.50;
            break;

         case 2:
            zf = 1;
            break;

         case 3:
            zf = 2;
            break;

         case 4:
            zf = 4;
            break;

         case 5:
            zf = 8;
            break;

         case 6:
            zf = 16;
            break;
      }
      Iterator it = zoomScrollPanes.iterator();
      while (it.hasNext()) {
         ZoomScrollPane zp = (ZoomScrollPane) it.next();
         Point2D.Double p = zp.toUserSpace(0, 0); //use the 0, 0 as the zoom center
         zp.zoom(p.x, p.y, zf, zf);
      }
   }


   public void addZoomScrollPane (ZoomScrollPane zsp) {
      super.addZoomScrollPane(zsp);
      if (zsp instanceof MouseSensitiveZSP) {
         ((MouseSensitiveZSP) zsp).enableMouseInteraction(true);
      }
   }


   public void removeScrollPane (ZoomScrollPane zsp) {
      super.removeScrollPane(zsp);
      if (zsp instanceof MouseSensitiveZSP) {
         ((MouseSensitiveZSP) zsp).enableMouseInteraction(false);
      }
   }


   /**
    * zoomToRoi
    */
   public void zoomToRoi () {
      Iterator it = zoomScrollPanes.iterator();
      while (it.hasNext()) {
         ZoomScrollPane zsp = (ZoomScrollPane) it.next();
         if (zsp instanceof MouseSensitiveZSP) {
            Rectangle roi = ((MouseSensitiveZSP) zsp).getRoiRect();
            ((MouseSensitiveZSP) zsp).zoomRectangleToWholeWindow(roi);
            ((MouseSensitiveZSP) zsp).setRoiRect(null);
         }
      }
   }

}
