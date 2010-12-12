package imagedisplay;

import imagedisplay.zoom.core.ZoomJPanel;
import imagedisplay.zoom.core.ZoomScrollPane;
import imagedisplay.zoom.util.MouseSensitiveZSP;
import imagedisplay.zoom.util.ResourceManager;
import imagedisplay.zoom.util.ctrlbar.ZoomCtrlBar;
import java.awt.*;
import java.awt.event.*;

import java.text.*;

import java.util.Iterator;

import javax.swing.*;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

/** @todo
 * Panner
 * disable hand if no scrollbars
 * ZoomToROI
 * enable ZoomToROI if ROI selected
 *  */
public class ZoomControl16 extends ZoomCtrlBar // extends JPanel
 {
    JButton buttonZoomIn = new JButton();
    JButton buttonZoomOne = new JButton();
    JButton buttonZoomOut = new JButton();
    JButton buttonZoomRoi = new JButton();
    JButton buttonFitWindow = new JButton();
    JToggleButton togglePan = new JToggleButton();
    JLabel zFactor = new JLabel();
    ZoomScrollPane zsp;
    
    double[] factors = {1.0/32, 1.0/16 , 1.0/8 , 1.0/4 , 1.0/2 , 1.0/1.0 , 2.0, 4.0, 8.0, 16.0, 32.0};
    int currentFactorIndex = 5; // zoomfactor = 1.0
    
    // @todo disable hand if no scrollbars
    // @todo enable ZoomToROI if ROI selected
    NumberFormat formatter = new DecimalFormat("#.##");

    //MouseSensitiveZSP zsp;
    public ZoomControl16(ZoomScrollPane zsp) { //(ZoomScrollPane zsp) {
        this.zsp = zsp;
        addZoomScrollPane(zsp);

        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        ResourceManager rm = new ResourceManager(
                "edu/mbl/jif/gui/imaging/icons");
        // @todo Fix: on loading icons from jar file
        buttonZoomIn.setToolTipText("Zoom In");
        buttonZoomIn.setIcon(new ImageIcon(ZoomControl16.class
                                               .getResource("icons/zoomin.gif")));
        buttonZoomIn.setMargin(new Insets(0, 0, 0, 0));

        buttonZoomOne.setMargin(new Insets(0, 0, 0, 0));
        buttonZoomOne.setToolTipText("Zoom 1:1");
        buttonZoomOne.setIcon(new ImageIcon(ZoomControl16.class
                                                .getResource("icons/zoom1.gif")));

        buttonZoomOut.setMargin(new Insets(0, 0, 0, 0));
        buttonZoomOut.setToolTipText("Zoom Out");
        buttonZoomOut.setIcon(new ImageIcon(ZoomControl16.class
                                                .getResource("icons/zoomout.gif")));

        /** @todo Zoom to ROI...  */
        buttonZoomRoi.setMargin(new Insets(0, 0, 0, 0));
        buttonZoomRoi.setToolTipText("Zoom to ROI");
        buttonZoomRoi.setIcon(new ImageIcon(ZoomControl16.class
                                                .getResource("icons/zoomToRoi.gif")));

        buttonFitWindow.setMargin(new Insets(0, 0, 0, 0));
        buttonFitWindow.setToolTipText("Fit to Window");
        buttonFitWindow.setIcon(new ImageIcon(ZoomControl16.class
                                                  .getResource("icons/fitToWindow.png")));

        togglePan.setMargin(new Insets(0, 0, 0, 0));
        togglePan.setToolTipText("Pan image");
        togglePan.setIcon(new ImageIcon(ZoomControl16.class
                                            .getResource("icons/hand16.gif")));
        togglePan.setSelectedIcon(new ImageIcon(ZoomControl16.class
                                                    .getResource("icons/normal.gif")));

        togglePan.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if (togglePan.isSelected()) {
                    } else {
                    }
                    updateZoomValue();
                }
            }
        );

        buttonZoomIn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    // zsp.getBounds();
                    zsp.zoom(0, 0, zsp.getZoomFactorX() + 1.1,
                        zsp.getZoomFactorY() + 1.1);
                    updateZoomValue();
                }
            });

        buttonZoomOut.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    zsp.zoom(0, 0, zsp.getZoomFactorX() / 1.5,
                        zsp.getZoomFactorY() / 1.5);
                    updateZoomValue();
                }
            });

        buttonZoomOne.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    //currentFactorIndex = 5;
                    //factor[5]
                    zsp.zoom(0, 0, 1.0, 1.0);
                    //zsp.restore();
                    //zsp.repaint();
                    updateZoomValue();
                }
            });

        buttonFitWindow.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    zsp.fitToScreen();
                    updateZoomValue();
                }
            });

        buttonZoomRoi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    zoomToRoi();
                    //            if (zsp.getRoiRect() != null) {
                    //               zsp.zoomRectangleToWholeWindow(zsp.getRoiRect());
                    //            }
                    updateZoomValue();
                }
            });

        this.add(buttonZoomIn, null);
        this.add(buttonZoomOne, null);
        this.add(buttonZoomOut, null);
        this.add(buttonZoomRoi, null);
        this.add(buttonFitWindow, null);
        this.add(togglePan, null);

        zFactor.setHorizontalAlignment(JLabel.CENTER);
        zFactor.setPreferredSize(new Dimension(32, 16));
        zFactor.setFont(new Font("Dialog", Font.PLAIN, 10));
        this.add(zFactor, null);
        this.setMargin(new Insets(0, 0, 0, 0));
        updateZoomValue();
    }

    public void setToPanningMode() {
        // set to panning mode using current zoom factor
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void updateZoomValue() {
        zFactor.setText("" + formatter.format(zsp.getZoomFactorX()));
    }

    /**
     * set the zoom mode
     * zoom mode decides what happens when the zoomable JPanel is mouse clicked
     */
    public void setZoomMode(int zoomManner) {
        Iterator it = zoomScrollPanes.iterator();
        while (it.hasNext()) {
            ZoomScrollPane zsp = (ZoomScrollPane) it.next();
            if (zsp instanceof MouseSensitiveZSP) {
                ((MouseSensitiveZSP) zsp).setZoomMode(zoomManner);
            }
        }
    }

    /**
     * zoomToRoi
     */
    public void zoomToRoi() {
        Iterator it = zoomScrollPanes.iterator();

        while (it.hasNext()) {
            ZoomScrollPane zsp = (ZoomScrollPane) it.next();
            if (zsp instanceof MouseSensitiveZSP) {
                Rectangle roi = ((MouseSensitiveZSP) zsp).getRoiRect();
                ((MouseSensitiveZSP) zsp).zoomRectangleToWholeWindow(roi);
                ((MouseSensitiveZSP) zsp).setRoiRect(null);
            }
            if (zsp instanceof ZoomScrollPane) {
                ZoomJPanel zjp = zsp.getZoomPanel();
                Rectangle roi = zjp.getROI();
                zsp.zoomRectangleToWholeWindow(roi);
                zjp.setROI(null);
            }
        }
    }

    public void addZoomScrollPane(ZoomScrollPane zsp) {
        super.addZoomScrollPane(zsp);

        if (zsp instanceof MouseSensitiveZSP) {
            ((MouseSensitiveZSP) zsp).enableMouseInteraction(true);
        }
    }

    public void removeScrollPane(ZoomScrollPane zsp) {
        super.removeScrollPane(zsp);

        if (zsp instanceof MouseSensitiveZSP) {
            ((MouseSensitiveZSP) zsp).enableMouseInteraction(false);
        }
    }


}
