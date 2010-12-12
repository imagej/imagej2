package imagedisplay;

import imagedisplay.zoom.core.ZoomScrollPane;
import imagedisplay.zoom.util.ResourceManager;
import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ZoomControl
      extends JPanel
{
   JButton buttonZoomIn = new JButton();
   JButton buttonZoomOne = new JButton();
   JButton buttonZoomOut = new JButton();
   JButton buttonZoomRoi = new JButton();
   JButton buttonFitWindow = new JButton();
   JLabel zFactor = new JLabel();
   ZoomScrollPane zsp;

   public ZoomControl (ZoomScrollPane zsp) {
      this.zsp = zsp;
      try {
         jbInit();
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }
   }


   void jbInit () throws Exception {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      ResourceManager rm = new ResourceManager("edu/mbl/jif/gui/imaging/icons");

      buttonZoomIn.setToolTipText("Zoom In");
      buttonZoomIn.setIcon(rm.getImageIcon("zoomIn24.gif"));
      buttonZoomIn.setMargin(new Insets(0, 0, 0, 0));

      buttonZoomOne.setMargin(new Insets(0, 0, 0, 0));
      buttonZoomOne.setToolTipText("Zoom 1:1");
      buttonZoomOne.setIcon(rm.getImageIcon("zoomOne24.gif"));

      buttonZoomOut.setMargin(new Insets(0, 0, 0, 0));
      buttonZoomOut.setToolTipText("Zoom Out");
      buttonZoomOut.setIcon(rm.getImageIcon("zoomOut24.gif"));

      /** @todo Zoom to ROI...  */
      buttonZoomRoi.setMargin(new Insets(0, 0, 0, 0));
      buttonZoomRoi.setToolTipText("Zoom Out");
      buttonZoomRoi.setIcon(rm.getImageIcon("zoomRoi24.gif"));

      buttonFitWindow.setMargin(new Insets(0, 0, 0, 0));
      buttonFitWindow.setToolTipText("Zoom Out");
      buttonFitWindow.setIcon(rm.getImageIcon("zoomFitWindow24.gif"));

      buttonZoomIn.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent ae) {
            zsp.getBounds();
            zsp.zoom(0, 0, zsp.getZoomFactorX() + 1, zsp.getZoomFactorY() + 1);
            updateZoomPanel(); }
      });

      buttonZoomOut.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent ae) {
            zsp.zoom(0, 0, zsp.getZoomFactorX() / 1.5, zsp.getZoomFactorY() / 1.5);
            updateZoomPanel();
         }
      });

      buttonZoomOne.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent ae) {
            zsp.restore();
            zsp.repaint();
            updateZoomPanel();
         }
      });

      buttonFitWindow.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent ae) {
            zsp.fitToScreen();
            updateZoomPanel();
         }
      });

      this.add(buttonZoomIn, null);
      this.add(buttonZoomOne, null);
      this.add(buttonZoomOut, null);
      //this.add(buttonZoomRoi, null);
      this.add(buttonFitWindow, null);
      zFactor.setHorizontalAlignment(JLabel.CENTER);
      zFactor.setPreferredSize(new Dimension(24, 16));
      zFactor.setFont(new Font( "Dialog", Font.PLAIN, 10 ));
      this.add(zFactor, null);
      updateZoomPanel ();
   }


   NumberFormat formatter = new DecimalFormat("#.##");

   void updateZoomPanel () {
      zFactor.setText(formatter.format(zsp.getZoomFactorX()));
   }
}
