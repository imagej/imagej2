package ijx.gui.icon;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * This is based in part on http://weblogs.java.net/blog/2005/07/23/how-create-scalable-icons-java2d
 * @author GBH <imagejdev.org>
 */

public class ScalableIcon {

    public static Icon getSuccessMarkerIcon(int dimension) {
        return new ImageIcon(getSuccessMarker(dimension));
    }

    // Define a function that creates a BufferedImage:
    public static BufferedImage getSuccessMarker(int dimension) {
        // First, we create a new image and set it to anti-aliased mode:
        // new RGB image with transparency channel
        BufferedImage image = new BufferedImage(dimension, dimension,
                BufferedImage.TYPE_INT_ARGB);
        // create new graphics and set anti-aliasing hint
        Graphics2D g2 = (Graphics2D) image.getGraphics().create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill the background: green background fill
        g2.setColor(new Color(0, 196, 0));
        g2.fillOval(0, 0, dimension - 1, dimension - 1);

        // Create a white spot in the top-left corner (to simulate 3D shining effect) - 
        // note that we set clipping area to the icon circle (so that all the rest will
        // remain transparent when our icon will be shown on non-white background):
        // create spot in the upper-left corner using temporary graphics
        // with clip set to the icon outline
        GradientPaint spot = new GradientPaint(0, 0, new Color(255, 255, 255,
                200), dimension, dimension, new Color(255, 255, 255, 0));
        Graphics2D tempGraphics = (Graphics2D) g2.create();
        tempGraphics.setPaint(spot);
        tempGraphics.setClip(new Ellipse2D.Double(0, 0, dimension - 1,
                dimension - 1));
        tempGraphics.fillRect(0, 0, dimension, dimension);
        tempGraphics.dispose();

        // Draw the outline (must be done after the white gradient so the outline is not affected by it):
        // draw outline of the icon
        g2.setColor(new Color(0, 0, 0, 128));
        g2.drawOval(0, 0, dimension - 1, dimension - 1);

        // Compute the stroke width for the V sign. This sign is created using the same path with different strokes, one for the outer rim (wider), and one for the inner filling (narrower).
        // draw the V sign
        float dimOuter = (float) (0.5f * Math.pow(dimension, 0.75));
        float dimInner = (float) (0.28f * Math.pow(dimension, 0.75));

        // Create a GeneralPath for the V sign
        // create the path itself
        GeneralPath gp = new GeneralPath();
        gp.moveTo(0.25f * dimension, 0.45f * dimension);
        gp.lineTo(0.45f * dimension, 0.65f * dimension);
        gp.lineTo(0.85f * dimension, 0.12f * dimension);

        // Draw the path twice
        // draw blackish outline
        g2.setStroke(new BasicStroke(dimOuter, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(0, 0, 0, 196));
        g2.draw(gp);
        // draw white inside
        g2.setStroke(new BasicStroke(dimInner, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g2.setColor(Color.white);
        g2.draw(gp);

        // Dispose of the temp graphics and return the image
        // dispose
        g2.dispose();
        return image;
    }

// For slightly curved mark (the second row), use the following path:
//      GeneralPath gp = new GeneralPath();
//      gp.moveTo(0.25f * dimension, 0.45f * dimension);
//      gp.quadTo(0.35f * dimension, 0.52f * dimension, 0.45f * dimension,
//            0.65f * dimension);
//      gp.quadTo(0.65f * dimension, 0.3f * dimension, 0.85f * dimension,
//            0.12f * dimension);

    public static void main(String[] args) {
        JFrame f = new JFrame();
        JButton b = new JButton();
        //b.setPreferredSize(new Dimension(24,24));

        b.setIcon(ScalableIcon.getSuccessMarkerIcon(24));
        b.setSize(24,24);
        b.setMaximumSize(new Dimension(24,24));
        f.add(b, BorderLayout.CENTER);
//        JLabel l = new JLabel();
//        l.setIcon(ScalableIcon.getSuccessMarkerIcon(24));
//        f.add(l);
        f.pack();
        f.setVisible(true);
    }
}
