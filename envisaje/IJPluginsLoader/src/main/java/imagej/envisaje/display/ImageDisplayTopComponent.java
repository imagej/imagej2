package imagej.envisaje.display;

import imagej.awt.AWTImageDisplayWindow;
import imagej.data.AxisLabel;
import imagej.display.DisplayController;
import imagej.display.EventDispatcher;
import imagej.display.ImageCanvas;
import imagej.display.event.ZoomEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.miginfocom.swing.MigLayout;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//imagej.envisaje.display//ImageDisplay//EN",
autostore = false)
@TopComponent.Description(preferredID = "ImageDisplayTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "imagej.envisaje.display.ImageDisplayTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ImageDisplayAction",
preferredID = "ImageDisplayTopComponent")
public final class ImageDisplayTopComponent extends TopComponent
        implements AWTImageDisplayWindow, EventSubscriber<ZoomEvent> {

    private JLabel imageLabel;
    private SwingNavigableImageCanvas imgCanvas = null;
    private JPanel sliders;
    protected DisplayController controller;

    public ImageDisplayTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ImageDisplayTopComponent.class, "CTL_ImageDisplayTopComponent"));
        setToolTipText(NbBundle.getMessage(ImageDisplayTopComponent.class, "HINT_ImageDisplayTopComponent"));

    }

    public void addCanvas(final SwingNavigableImageCanvas imgCanvas) {
        this.imgCanvas = imgCanvas;
        imageLabel = new JLabel(" ");
        final int prefHeight = imageLabel.getPreferredSize().height;
        imageLabel.setPreferredSize(new Dimension(0, prefHeight));
        //imgCanvas.setPreferredSize(new Dimension(200,200));

        JPanel graphicPane = new JPanel();
        graphicPane.setLayout(new BorderLayout());
        graphicPane.setBorder(new LineBorder(Color.black));
        graphicPane.add(imgCanvas, BorderLayout.CENTER);


        sliders = new JPanel();
        sliders.setLayout(new MigLayout("fillx, wrap 2", "[right|fill,grow]"));

        final JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        pane.setBorder(new EmptyBorder(3, 3, 3, 3));
        pane.add(imageLabel, BorderLayout.NORTH);
        pane.add(graphicPane, BorderLayout.CENTER);
        pane.add(sliders, BorderLayout.SOUTH);

        subscribeToZoomEvents();
        
        pane.setPreferredSize(new Dimension(200,200));
        this.add(pane, BorderLayout.CENTER); 
        
        // Add TopComponent stuff here...

        //setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    @Override
    public void setDisplayController(final DisplayController controller) {
        this.controller = controller;
        final int[] dims = controller.getDims();
        final AxisLabel[] dimLabels = controller.getDimLabels();
        createSliders(dims, dimLabels);
        sliders.setVisible(dims.length > 2);
    }

    @Override
    public void setLabel(final String s) {
        imageLabel.setText(s);
    }

    public ImageCanvas getPanel() {
        return imgCanvas;
    }

    private void createSliders(final int[] dims, final AxisLabel[] dimLabels) {
        sliders.removeAll();
        for (int i = 0, p = -1; i < dims.length; i++) {
            if (AxisLabel.isXY(dimLabels[i])) {
                continue;
            }
            p++;
            if (dims[i] == 1) {
                continue;
            }

            final JLabel label = new JLabel(dimLabels[i].toString());
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            final JScrollBar slider =
                    new JScrollBar(Adjustable.HORIZONTAL, 1, 1, 1, dims[i] + 1);
            final int posIndex = p;
            slider.addAdjustmentListener(new AdjustmentListener() {

                @Override
                public void adjustmentValueChanged(final AdjustmentEvent e) {
                    controller.updatePosition(posIndex, slider.getValue() - 1);
                }
            });
            sliders.add(label);
            sliders.add(slider);
        }
    }

    @Override
    public void setImageCanvas(final ImageCanvas canvas) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageCanvas getImageCanvas() {
        return imgCanvas;
    }

    @Override
    public void updateImage() {
        imgCanvas.updateImage();
    }

    @Override
    public void addEventDispatcher(final EventDispatcher dispatcher) {
//		addWindowListener((AWTEventDispatcher) dispatcher);
    }

    private void subscribeToZoomEvents() {
        Events.subscribe(ZoomEvent.class, this);
    }

    /*
     * Handles setting the title to the current dataset name and zoom level
     */
    @Override
    public void onEvent(ZoomEvent event) {
        if (event.getCanvas() != imgCanvas) {
            return;
        }
        String datasetName = "";
        if (this.controller != null) {
            datasetName = this.controller.getDataset().getMetadata().getName();
        }
        double zoom = event.getNewZoom();
        if (zoom == 1.0) // exactly
        {
            setTitle(datasetName);
        } else {
            String percentZoom = String.format("%.2f", zoom * 100);
            setTitle(datasetName + " (" + percentZoom + "%)");
        }
    }


    @Override
    public void setTitle(String s) {
        this.setDisplayName(s);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void pack() {
    }
}
