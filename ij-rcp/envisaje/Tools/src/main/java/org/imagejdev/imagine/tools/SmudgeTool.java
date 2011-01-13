/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.imagine.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.imagejdev.imagine.api.image.Hibernator;
import org.imagejdev.imagine.api.image.Layer;
import org.imagejdev.imagine.api.image.Surface;
import org.imagejdev.imagine.api.util.RasterConverter;
import org.imagejdev.imagine.tools.spi.MouseDrivenTool;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service=org.imagejdev.imagine.spi.tools.Tool.class)

public class SmudgeTool extends MouseDrivenTool {
    private int radius = 20;
    public SmudgeTool() {
        super (new ImageIcon(Utilities.loadImage("org/imagejdev/imagine/tools/" + //NOI18N
                "resources/smudge.png")), NbBundle.getMessage(SmudgeTool.class,  //NOI18N
                "TOOL_SMUDGE")); //NOI18N
    }

    @Override
    protected void dragged(Point p, int modifiers) {
        Layer layer = this.getLayer();
        Surface surface = layer.getSurface();
        BufferedImage img = surface.getImage();
        if (img == null) {
            RasterConverter conv = RasterConverter.getDefault();
            if (conv != null) {
                Layer newLayer = RasterConverter.askUserToConvert(layer, null, this);
                if (newLayer != null) {
                    super.deactivate();
                    super.activate(newLayer);
                    surface = newLayer.getSurface();
                    img = surface.getImage();
                    if (img == null) {
                        return;
                    }
                    surface.setTool(this);
                }
            }
        }
        if (img == null) {
            return;
        }
        if (p.x < 0 || p.y < 0 || p.x >= img.getWidth() || p.y >= img.getHeight()) {
            return;
        }
        if (img.getType() == 0) { //Our NIO raster images
            Hibernator hib = layer.getLookup().lookup(Hibernator.class);
            hib.wakeup(true, null);
            img = surface.getImage();
        }
        float[] matrix = {
                0.111f, 0.111f, 0.111f, 
                0.111f, 0.111f, 0.111f, 
                0.111f, 0.111f, 0.111f, 
            };
        BufferedImageOp op = new ConvolveOp( new Kernel(3, 3, matrix) );
        BufferedImage sub = img.getSubimage(p.x, p.y, radius, radius);
        surface.getGraphics().drawImage(sub, op, p.x, p.y);
    }
    
    private static final class I implements Icon {

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor (Color.ORANGE);
            g.fillRect (x, y, 10, 10);
        }

        public int getIconWidth() {
            return 16;
        }

        public int getIconHeight() {
            return 16;
        }
        
    }

}
