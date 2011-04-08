/*
 * ImageTool.java
 *
 * Created on October 1, 2006, 2:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package imagej.envisaje.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.spi.tools.Customizer;
import imagej.envisaje.spi.tools.CustomizerProvider;
import imagej.envisaje.spi.tools.PaintParticipant;
import imagej.envisaje.spi.tools.PaintParticipant.Repainter;
import imagej.envisaje.spi.tools.Tool;
import imagej.envisaje.api.image.Surface;
import imagej.envisaje.misccomponents.FileChooserUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */

@ServiceProvider(service=imagej.envisaje.spi.tools.Tool.class)

public class ImageTool implements Tool, MouseListener, MouseMotionListener, KeyListener, PaintParticipant, CustomizerProvider, Customizer {

    private BufferedImage image = null;
    private Repainter repainter;

    /** Creates a new instance of ImageTool */
    public ImageTool() {
    }

    public boolean canAttach(Layer layer) {
        return layer.getLookup().lookup(Surface.class) != null;
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            commit();
        }
    }
    private boolean armed;

    public void mousePressed(MouseEvent e) {
        armed = true;
    }

    public void mouseReleased(MouseEvent e) {
        armed = false;
        commit();
        image = null;
    }

    public void mouseEntered(MouseEvent e) {
        armed = true;
    }

    public void mouseExited(MouseEvent e) {
        armed = false;
    }

    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    public void mouseMoved(MouseEvent e) {
        armed = true;
        imgLocation = e.getPoint();
        repainter.requestRepaint(null); //XXX why repaint everything?
    }

    public void keyTyped(KeyEvent e) {
    }
    private Point imgLocation = new Point();
    boolean resizeMode = true;

    public void keyPressed(KeyEvent e) {
        Point p = imgLocation;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN:
                p.y++;
                break;
            case KeyEvent.VK_UP:
                p.y--;
                break;
            case KeyEvent.VK_LEFT:
                p.x--;
                break;
            case KeyEvent.VK_RIGHT:
                p.x++;
                break;
            case KeyEvent.VK_ENTER:
                commit();
                break;
            case KeyEvent.VK_SHIFT:
                resizeMode = false;
                repainter.requestRepaint(null);
                break;
        }
    }

    public void paint(Graphics2D g, Rectangle bds, boolean commit) {
        if (image == null) {
            return;
        }
        BufferedImage img = image;

        double w = bds.width;
        double h = bds.height;
        double iw = img.getWidth();
        double ih = img.getHeight();

        double fw = w / iw;
        double fh = h / ih;

        double xform;
        if (fw * h > img.getHeight()) {
            xform = fh;
        } else if (fh * w > img.getWidth()) {
            xform = fw;
        } else {
            xform = 1;
        }
        boolean needResize = w < img.getWidth() || h < img.getHeight();

        AffineTransform at = AffineTransform.getTranslateInstance(bds.x + imgLocation.x, bds.y + imgLocation.y);
        if (!resizeMode && needResize) {
            at.concatenate(AffineTransform.getScaleInstance(xform, xform));
        }

        if (!commit) {
            Composite c = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
            g.drawRenderedImage(img, at);
            g.setComposite(c);
        } else {
            layer.getSurface().beginUndoableOperation(toString());
            try {
                g.drawRenderedImage(img, at);
            } finally {
                layer.getSurface().endUndoableOperation();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SHIFT:
                resizeMode = true;
                repainter.requestRepaint(null);
                break;
        }
    }
    BufferedImage lastImage = null;

    private void commit() {
        repainter.requestCommit();
    }

    public void attachRepainter(Repainter repainter) {
        this.repainter = repainter;
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(getClass(), "Image");
    }

    private BufferedImage loadImage() {
        JFileChooser jfc = FileChooserUtils.getFileChooser("image"); //NOI18N
        jfc.setDialogTitle(NbBundle.getMessage(getClass(), "Load_Image")); //NOI18N
        jfc.setDialogType(JFileChooser.OPEN_DIALOG);
        jfc.setFileHidingEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setApproveButtonText(NbBundle.getMessage(getClass(), "Open_Image")); //NOI18N
        jfc.setMultiSelectionEnabled(false);
        jfc.setAccessory(new ImagePanel(jfc));
        jfc.setFileFilter(new FF());
        if (jfc.showOpenDialog(Frame.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
            if (jfc.getSelectedFile().isFile()) {
                try {
                    image = ImageIO.read(jfc.getSelectedFile());
                    return image;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(Frame.getFrames()[0],
                            e.getMessage());
                }
            }
        }
        return null;
    }

    private static final class FF extends FileFilter {

        private final Set formats;

        FF() {
            String[] sfxs = ImageIO.getReaderFileSuffixes();
            formats = new HashSet(Arrays.asList(sfxs));
        }

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            } else {
                String s = getFileExt(f);
                return s != null && formats.contains(s);
            }
        }

        public String getDescription() {
            return NbBundle.getMessage(getClass(), "Image_File_Formats"); //NOI18N
        }

        private String getFileExt(File f) {
            String s = f.getName();
            int ix = s.lastIndexOf(".");
            return ix <= 0 && ix < s.length() - 2 ? null : s.substring(ix + 1, s.length()).toLowerCase();
        }
    }

    private static final class ImagePanel extends JComponent implements PropertyChangeListener {

        private final JFileChooser jfc;
        private BufferedImage img;
        private final ExecutorService exe;

        ImagePanel(JFileChooser jfc) {
            jfc.addPropertyChangeListener(this);
            this.jfc = jfc;
            exe = Executors.newSingleThreadExecutor();
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5),
                    BorderFactory.createLineBorder(Color.BLACK)));
        }

        void setImage(BufferedImage img) {
            this.img = img;
            repaint();
        }
        private FileSetter setter;
        Future future;

        private void setFile(File f) {
            synchronized (this) {
                if (future != null) {
                    future.cancel(true);
                }
                if (f != null) {
                    setter = new FileSetter(f);
                    future = exe.submit(setter);
                }
            }
            if (f == null) {
                setImage(null);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                File f = jfc.getSelectedFile();
                setFile(f);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(306, 306);
        }

        @Override
        public void paintComponent(Graphics g) {
            if (img == null) {
                return;
            }
            Graphics2D gg = (Graphics2D) g;
            Insets ins = getInsets();
            java.awt.Rectangle bds = new java.awt.Rectangle(ins.left, ins.top, getWidth()
                    - (ins.left + ins.right), getHeight() - (ins.top + ins.bottom));

            double w = bds.width;
            double h = bds.height;
            double iw = img.getWidth();
            double ih = img.getHeight();

            double fw = w / iw;
            double fh = h / ih;

            AffineTransform at = AffineTransform.getTranslateInstance(bds.x, bds.y);
            at.concatenate(AffineTransform.getScaleInstance(fw, fh));

            gg.drawRenderedImage(img, at);
        }

        private class FileSetter implements Runnable {

            private final File file;

            FileSetter(File file) {
                this.file = file;
                assert file != null;
            }
            private BufferedImage img;

            public void run() {
                synchronized (ImagePanel.this) {
                    setter = null;
                }
                if (!EventQueue.isDispatchThread()) {
                    if (Thread.interrupted()) {
                        return;
                    }
                    try {
                        img = ImageIO.read(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (img != null) {
                        EventQueue.invokeLater(this);
                    }
                } else {
                    setImage(img);
                }
            }
        }
    }

    public String getInstructions() {
        return NbBundle.getMessage(getClass(), "Position_image_and_double-click_or_press_enter"); //NOI18N
    }

    public Icon getIcon() {
        return new ImageIcon(DrawTool.load(DrawTool.class, "image.png")); //NOI18N
    }

    public Customizer getCustomizer() {
        return this;
    }

    public String getName() {
        return NbBundle.getMessage(getClass(), "Image");
    }

    public Object get() {
        return image;
    }

    public void deactivate() {
        repainter = null;
        layer = null;
        image = null;
    }

    public Lookup getLookup() {
        return Lookups.singleton(this);
    }
    private Layer layer;

    public void activate(Layer layer) {
        this.layer = layer;
    }

    public JComponent getComponent() {
        JButton jb = new JButton(NbBundle.getMessage(getClass(), "Load_Image")); //NOI18N
        jb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                loadImage();
            }
        });
        JPanel jp = new JPanel(new FlowLayout());
        jp.add(jb);
        return jp;
    }
}
