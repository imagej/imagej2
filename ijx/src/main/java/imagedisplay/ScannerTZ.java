package imagedisplay;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class ScannerTZ extends JPanel {
    static SeriesOfImages series;
    
    //ZoomImagePanel pZi = new ZoomImagePanel();
    ImageDisplayPanel idp;
    JPanel bottomPane = new JPanel();
    JLabel label;
    int n;
    int nT;
    int nZ;
    int t = 0;
    int z = 0;
    private float scaleFactor = 1.0f;
    
    public ScannerTZ(SeriesOfImages series) {
        super(true);
        this.series = series;
        n = series.getNumImages();
        nT = series.getTimeIntervals();
        nZ = series.getZSections();
        setLayout(new BorderLayout());
        
        // Horizontal (T) scrollbar ------------------
        final JSlider hbar = new JSlider( SwingConstants.HORIZONTAL,0, nT -1, 1);
        ChangeListener adjustmentListenerH = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                t = ((JSlider)e.getSource()).getValue();
                //System.out.println("t=" + t);
                showImage();
            }
        };
        hbar.addChangeListener(adjustmentListenerH);
        hbar.setMajorTickSpacing(1);
        hbar.setPaintTicks(true);
        hbar.setPaintTrack(false);
        add(hbar, BorderLayout.NORTH);
        
        // add right/left arrow key responses
        //InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        InputMap im = this.getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        AbstractAction right = new AbstractAction("right") {
            public void actionPerformed(ActionEvent evt) {
                if (hbar.getValue() < hbar.getMaximum()) {
                    hbar.setValue(hbar.getValue() + 1);
                }
                ;
            }
        };
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        AbstractAction left = new AbstractAction("left") {
            public void actionPerformed(ActionEvent evt) {
                if (hbar.getValue() > hbar.getMinimum()) {
                    hbar.setValue(hbar.getValue() - 1);
                }
                ;
            }
        };
        ActionMap am = this.getActionMap();
        am.put("right", right);
        am.put("left", left);
        
        // Vertical (Z) scrollbar ------------------------------
        if (nZ > 1) {
            final JSlider vbar = new JSlider(JScrollBar.VERTICAL, 0, nZ - 1, 1);
            ChangeListener adjustmentListenerV = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    z = ((JSlider)e.getSource()).getValue();
                    //System.out.println("z=" + t);
                    showImage();
                }
            };
            vbar.addChangeListener(adjustmentListenerV);
            vbar.setMajorTickSpacing(1);
            vbar.setPaintTicks(true);
            vbar.setPaintTrack(false);
            vbar.setInverted(true);
            add(vbar, BorderLayout.WEST);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "dn");
            AbstractAction dn = new AbstractAction("dn") {
                public void actionPerformed(ActionEvent evt) {
                    if (vbar.getValue() < vbar.getMaximum()) {
                        vbar.setValue(vbar.getValue() + 1);
                    }
                    ;
                }
            };
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
            AbstractAction up = new AbstractAction("up") {
                public void actionPerformed(ActionEvent evt) {
                    if (vbar.getValue() > vbar.getMinimum()) {
                        vbar.setValue(vbar.getValue() - 1);
                    }
                    ;
                }
            };
            am.put("dn", dn);
            am.put("up", up);
        }
        
        bottomPane.setLayout(new BorderLayout());
        label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);
        bottomPane.add(label, BorderLayout.CENTER);
        SeriesEditor seriesEdit = new SeriesEditor(n);
        // If editing enabled {
        // Delete Z section
        JButton deleteZsection = new JButton("DelZ");
        // Delete TimePoint
        JButton deleteTimePoint = new JButton("DelT");
        bottomPane.add(deleteZsection, BorderLayout.WEST);
        bottomPane.add(deleteTimePoint, BorderLayout.EAST);
        // }
        add(bottomPane, BorderLayout.SOUTH);
        idp = new ImageDisplayPanel(series.getImageDimensions());
        idp.changeImage(series.getImage(1));
        //pZi.setImage(series.getImage(1), scaleFactor, true);
        //add(pZi, BorderLayout.CENTER);
        add(idp, BorderLayout.CENTER);
        showImage();
    }
    
    void showImage() {
        int imgIndex = (t * nZ) + z;
        System.out.println(t + ":" + z + ":" + imgIndex);
        idp.changeImage(series.getImage((t * nZ) + z));
        label.setText("T: " + (t + 1) + "/" + nT + "    Z: " + (z + 1) + "/" + nZ + "     (" +
                ((t * nZ) + z + 1) + ")");
    }
    
    public static void main(String[] s) {
        String FILE = //edu.mbl.jif.Constants.testDataPath +
                //"Series_TZ\\" +
                //"STMPS_Yuki_TZ_04_0428_1427_41_Z-5.tif";
        //"Zxyzt-200x200x10x15.tif";
        "Z5_T10.tif";
        int zSections = 5;
        
        // xyzt-200x200x10x15_b causes TIFFImageReader.getCompression NullPointer
        //"Series_TZ\\xyzt-200x200x10x15_b.tif";
        JFrame frame = new JFrame("ScannerTZ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        series = new SeriesOfImagesTiff(FILE, zSections);
        
        frame.setContentPane(new ScannerTZ(series));
        frame.setSize(200, 200);
        frame.setVisible(true);
    }
}
