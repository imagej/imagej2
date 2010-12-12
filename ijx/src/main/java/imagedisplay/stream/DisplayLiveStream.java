/*
 * DisplayLive.java
 * Created on May 9, 2006, 1:48 PM
 */
package imagedisplay.stream;


import imagedisplay.ImageDisplayPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.image.ImageProducer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * Live display of images streamed from Camera containing an ImageDisplayPanel
 * This is used by Magnifier and Lightfield
 * Uses ImageProducer (not StreamSource, as in DisplayLiveCamera)
 * @author GBH
 */
public class DisplayLiveStream extends JFrame implements DisplayLiveInterface {
    ImageDisplayPanel viewPanel;
    //StreamSource source;
    ImageProducer iProd;
    
    public DisplayLiveStream(String title, ImageProducer iProd, int width, int height, ImageIcon icon) {
        System.out.println("opening Display");
        this.iProd = iProd;
        this.setTitle(title);
        try {
            this.setIconImage(icon.getImage());
            // (new javax.swing.ImageIcon(getClass().getResource("/edu/mbl/jif/camera/icons/lightfield16.png"))).getImage());
        } catch (Exception ex) {
        }
        
        //get the camera's StreamSource
        Dimension imageDim = new Dimension(width, height);
        viewPanel = new ImageDisplayPanel(imageDim);
        viewPanel.setImageProducer(iProd); // <<<<<<<<<<<
        add(viewPanel, BorderLayout.CENTER);
//            Rectangle r = sizeAppropriately(imageDim);
//            this.setSize(r.width, r.height);
//            this.setLocation(r.x, r.y);
        
        super.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                onCloseContainer();
            }
        });
        super.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                viewPanel.onResize();
            }
        });
        this.setSize(300,300);
        this.setLocation(10,10);
        setVisible(true);
    }
    
    public Rectangle sizeAppropriately(Dimension imageDim) {
        int xBuffer = 50;
        int yBuffer = 50;
        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        // temp
        //Rectangle r = new Rectangle(10, 10, 600, 550);
        r.x = r.x + xBuffer;
        r.y = r.y + yBuffer;
        r.width = r.width - (2 * xBuffer);
        r.height = r.height - (2 * yBuffer);
        if (imageDim.getWidth() < r.width) {
            r.width = (int) imageDim.getWidth() + 12;
        }
        if (imageDim.getHeight() < r.height) {
            r.height = (int) imageDim.getHeight() + 57;
        }
        return r;
    }
    
    public void setStreamSource(StreamSource source) {
        
    }
    
    public StreamSource getStreamSource() {
        return null;
    }
    
    //--------------------------------------------------------
    @Override
    public void setSize(Dimension dim) {
        super.setSize((int) dim.getWidth(), (int) dim.getHeight());
    }
    
    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
    }
    
    @Override
    public void setScale(float scale) {
    }
    
    @Override
    public void fitToScreen() {
    }
    
    @Override
    public void suspend() {
    }
    
    @Override
    public void resume() {
    }
    
    public void restart() {
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }
    
    @Override
    public void setSelectedROI(Rectangle roi) {
        viewPanel.setROI(roi);
    }
    
    @Override
    public Rectangle getSelectedROI() {
        return viewPanel.getROI();
    }
    
    @Override
    public boolean isROISet() {
        return viewPanel.isROIset();
    }
    
    // Closing...
    public void onCloseContainer() {
        close();
    }
    
    @Override
    public void close() {
        System.out.println("Closing DisplayLiveStream");
        this.setVisible(false);
        if(viewPanel != null) {
            viewPanel.releaseStreamingSource();
            viewPanel = null;
        }
        iProd = null;
        //  ((InstrumentController) CamAcqJ.getInstance().getController()).setDisplayLive(null);
        this.dispose();
    }
}
/*
 //Create the dialog.
                    final JDialog dialog = new JDialog(frame,
                                                       "A Non-Modal Dialog");
 
                    //Add contents to it. It must have a close button,
                    //since some L&Fs (notably Java/Metal) don't provide one
                    //in the window decorations for dialogs.
                    JLabel label = new JLabel("<html><p align=center>"
                        + "This is a non-modal dialog.<br>"
                        + "You can have one or more of these up<br>"
                        + "and still use the main window.");
                    label.setHorizontalAlignment(JLabel.CENTER);
                    Font font = label.getFont();
                    label.setFont(label.getFont().deriveFont(font.PLAIN,
                                                             14.0f));
 
                    JButton closeButton = new JButton("Close");
                    closeButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                    });
                    JPanel closePanel = new JPanel();
                    closePanel.setLayout(new BoxLayout(closePanel,
                                                       BoxLayout.LINE_AXIS));
                    closePanel.add(Box.createHorizontalGlue());
                    closePanel.add(closeButton);
                    closePanel.setBorder(BorderFactory.
                        createEmptyBorder(0,0,5,5));
 
                    JPanel contentPane = new JPanel(new BorderLayout());
                    contentPane.add(label, BorderLayout.CENTER);
                    contentPane.add(closePanel, BorderLayout.PAGE_END);
                    contentPane.setOpaque(true);
                    dialog.setContentPane(contentPane);
 
                    //Show it.
                    dialog.setSize(new Dimension(300, 150));
                    dialog.setLocationRelativeTo(frame);
                    dialog.setVisible(true);
                }
 */