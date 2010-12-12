package imagedisplay;

import imagedisplay.stream.DisplayLiveStream;
import imagedisplay.stream.IntensityWatcher;
import imagedisplay.stream.StreamSource;
import imagedisplay.util.StaticSwingUtils;
import imagedisplay.zoom.core.ZoomScrollPane;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;

import java.util.List;
import javax.swing.*;

// !*!*! New and Improved !!  GBH, April 2006
// Uses Zoom components
// This encloses an 
//   ImagePanelZoomable, 
//   a ZoomControl16 controller tool bar,
//   and a PanelValuePointPixel.

public class ImageDisplayPanel extends JPanel {

    public Dimension imageDim;
    public ImagePanelZoomable imagePane;  // extends ZoomJPanel
    public ZoomScrollPane zsp;
    public ZoomControl16 zoomCtrl;
    public PanelValuePointPixel valuePanel;
    StreamSource source;
    JPanel ctrlPanelBottom;
    ImageProducer iProd;    
    private DisplayLiveStream magnifier = null;
    
    // @todo Add knowledge of ImageOrigin iOrigin; i.e. filename, timeStamp, whatever.
    
    public ImageDisplayPanel() {}
    
    public ImageDisplayPanel(BufferedImage image) {
        this(new Dimension(image.getWidth(), image.getHeight()));
        imagePane.changeImage(image);
    }

    public ImageDisplayPanel(Dimension _imageDim) {
        super();
        this.setLayout(new BorderLayout());
        imageDim = _imageDim;
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // Value Panel displays the pixel & ROI values
        valuePanel = new PanelValuePointPixel(1);

        // add zoomable image panel
        imagePane = new ImagePanelZoomable(this);
        zsp = new ZoomScrollPane(imagePane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(zsp, BorderLayout.CENTER);

        // Bottom control panel
        ctrlPanelBottom = new JPanel();
        ctrlPanelBottom.setLayout(new BoxLayout(ctrlPanelBottom, BoxLayout.X_AXIS));
        // add zoom control
        zoomCtrl = new ZoomControl16(zsp);
        ctrlPanelBottom.add(zoomCtrl);
        ctrlPanelBottom.add(valuePanel);

        // --- Image Info Button
        JButton info = new JButton();
        info.setIcon(new ImageIcon(ImageDisplayPanel.class.getResource("icons/info16.gif")));
        info.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showImageInfo(getImageDisplayed());
            }
        });
        info.setMargin(new Insets(0, 0, 0, 0));
        info.setMinimumSize(new Dimension(16, 16));
        ctrlPanelBottom.add(info);
        add(ctrlPanelBottom, BorderLayout.SOUTH);

        // add listeners for pixel and roi changes
        addPixelChangeListener(valuePanel);
        addRoiChangeListener(valuePanel);

        setPreferredSize(new Dimension((int) imageDim.getWidth(), (int) imageDim.getHeight() + 38));
        validate();
    }

    public void addMagnifierButton() {
        // --- Magnifier
        JButton buttonMagnifier = new JButton();
        buttonMagnifier.setMargin(new Insets(0, 0, 0, 0));
        buttonMagnifier.setMinimumSize(new Dimension(16, 16));
        buttonMagnifier.setToolTipText("Open Magnifier");
        buttonMagnifier.setIcon(new ImageIcon(ImageDisplayPanel.class.getResource("icons/magnifyingGlass16.png")));
        buttonMagnifier.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openMagnifier();
            }
        });
        ctrlPanelBottom.add(buttonMagnifier);
    }

    public void addPlotButton() {
        // Add Plot Intensity of ROI button
        JButton buttonPlot = new JButton();
        buttonPlot.setMargin(new Insets(0, 0, 0, 0));
        try {
            buttonPlot.setIcon(new ImageIcon(ImageDisplayPanel.class.getResource("icons/plot.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        buttonPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IntensityWatcher iWatcher = new IntensityWatcher(1, "\\camOut.csv");
                addRoiChangeListener(iWatcher);
            }
        });
        buttonPlot.setMargin(new Insets(0, 0, 0, 0));
        buttonPlot.setMinimumSize(new Dimension(16, 16));
        ctrlPanelBottom.add(buttonPlot);
    }

    public void installButtons(ImageViewerPlugin plugin) {
        List<AbstractButton> buttons = plugin.getButtons();
        for (AbstractButton abstractButton : buttons) {
            ctrlPanelBottom.add(abstractButton);
        }
    }

    public void addMarkPointButton() {
        PointMarker pMarker = new PointMarker(this.imagePane);
        installButtons(pMarker);
        imagePane.addGraphicOverlay(pMarker.getOverlay());
    }

    private JButton createButton(String iconPath) {
        JButton newButton = new JButton();
        newButton.setMargin(new Insets(0, 0, 0, 0));
        newButton.setMinimumSize(new Dimension(16, 16));
        try {
            newButton.setIcon(new ImageIcon(ImageDisplayPanel.class.getResource("icons/markPointClear.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newButton;
    }

    public  ImagePanelZoomable getImagePane() {
        return imagePane;
    }

    public void changeImage(BufferedImage img) {
        imagePane.changeImage(img);
    }

    public void setImageOp(BufferedImageOp op) {
        imagePane.setLookupOp(op);
    }

    public void showImage(final BufferedImage img) {
        // show without ... ?
        StaticSwingUtils.dispatchToEDT(new Runnable() {
            public void run() {
                imagePane.showImage(img);
            }
        });
    }

    public void addImage(BufferedImage img, double x, double y) {
        // show without ... ?
        imagePane.addImage(img, x, y);
    }

    
    public void onResize() {
        fitImageToWindow();
    }

    public void fitImageToWindow() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                zsp.fitToScreen();
                zoomCtrl.updateZoomValue();
            }

        });
    }
// <editor-fold defaultstate="collapsed" desc=" Streaming ">
// When used as Streaming display...
    public void setStreamingSource(StreamSource source) {
        this.source = source;
        imagePane.setupForStreaming();
        if (source != null) {
            source.attachToStream(imagePane);
        }
        //source.addConsumer(imagePane);
    }

    public void setImageProducer(ImageProducer iProd) {
        //imagePane.setupForStreaming(iProd, dim.width, dim.height);
        imagePane.setupForStreaming();
        if (iProd != null) {
            iProd.addConsumer(imagePane);
        }
    }

    public StreamSource getStreamingSource() {
        return source;
    }

    public void releaseStreamingSource() {
        if (source != null) {
            source.detachFromStream(imagePane);
        }
        if (iProd != null) {
            iProd.removeConsumer(imagePane);
        }
    }
    //   public void suspendStreaming()   {
    //      source.startProduction (imagePane);
    //   }
// </editor-fold>
    
    
// <editor-fold defaultstate="collapsed" desc=" ROI ">
    // 
    public void setSelectingROI(boolean selRoi) {
        imagePane.setSelectingROI(selRoi);
    }

    public void setROI(Rectangle roi) {
        imagePane.setROI(roi);
    }

    public Rectangle getROI() {
        return imagePane.getROI();
    }

    public boolean isROIset() {
        return imagePane.isROIset();
    }

    public void addPixelChangeListener(PixelChangeListener pixListener) {
        imagePane.addPixelChangeListener(new PixelChangedWeakListener(pixListener, imagePane));
    }

    public void addRoiChangeListener(RoiChangeListener roiListener) {
        imagePane.addRoiChangeListener(new RoiChangedWeakListener(roiListener, imagePane));
    }
// </editor-fold>

    //------------------------------------------------------
    // get image, including overlay and/or color
    public BufferedImage getImageWithOverlay() {
        return imagePane.getImageWithOverlay();
    }

    public BufferedImage getImageDisplayed() {
        return imagePane.getImageDisplayed();
    }

    public BufferedImage getImageDisplayedLut() {
        return imagePane.getImageDisplayedLut();
    }

    private void showImageInfo(BufferedImage img) {
        TextWindow tw = new TextWindow("ImageInfo");
        tw.set(ImgInfoDumper.dump(img));
        tw.setVisible(true);
    }

// <editor-fold defaultstate="collapsed" desc=" Magnifier ">
    public void openMagnifier() {
        if (isROIset()) {
            if (magnifier != null) {
                synchronized (magnifier) {
                    magnifier.close();
                    magnifier = null;
                }
            }
            String title = "Magnifier";
            Rectangle roi = this.getROI();
            System.out.println("roi: " + roi);
            ImageProducer iProd = null;

            MagnifyImageFilter filter = new MagnifyImageFilter(roi.x, roi.y, roi.width, roi.height);

            if (getStreamingSource() != null) {
                iProd = new FilteredImageSource(this.getStreamingSource().getImageProducer(), filter);
            } else if (this.getImageDisplayed() != null) {
                iProd = getImageDisplayed().getSource();
            } else {
                return;
            }

            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                "icons/magnifyingGlass16.png"));
            if (iProd != null) {
                magnifier = new DisplayLiveStream(title, iProd, roi.width, roi.height, icon);
                magnifier.setSize(300, 300);
            }

        } else {
//         Application.getInstance().error("No ROI defined for magnifier");
        }
    }
    // Using DetachedMagnifyingGlass...
    //    public void openMagnifier() {
    //        this.getROI();
    //        ZoomJPanel zjp = zsp.getZoomPanel();
    //        double scale = 8.0;
    //        JFrame magFrame = new JFrame("8x");
    //        DetachedMagnifyingGlass mag = new DetachedMagnifyingGlass(zjp,
    //                new Dimension(150, 150), scale);
    //        magFrame.setIconImage(new ImageIcon(ImageDisplayPanel.class
    //        .getResource("magnifyingGlass16.png")).getImage());
    //        magFrame.getContentPane().add(mag);
    //        magFrame.pack();
    //        magFrame.setLocation(new Point(this.getLocation().x + this.getWidth(),
    //                this.getLocation().y));
    //        magFrame.setAlwaysOnTop(true);
    //        magFrame.setVisible(true);
    //    }
// </editor-fold>

}
