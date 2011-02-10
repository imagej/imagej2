package imagedisplay;

import imagedisplay.NavigableImagePanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author GBH
 */
public class NavigableImageFrame extends JFrame {

    public NavigableImageFrame(String filename) {
        super("Navigable Image Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            final BufferedImage image = ImageIO.read(new File(filename));
            new NavigableImageFrame(image);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

    }

    public NavigableImageFrame(BufferedImage image) {
        NavigableImagePanel panel = new NavigableImagePanel();
        panel.setImage(image);
        panel.setNavigationImageEnabled(true);
        getContentPane().add(panel, BorderLayout.CENTER);
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle bounds = ge.getMaximumWindowBounds();
        setSize(new Dimension(bounds.width, bounds.height));
        setVisible(true);
    }
}
