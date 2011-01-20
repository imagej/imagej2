/*
 * OvalTool.java
 *
 * Created on September 29, 2006, 4:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.tools;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service=imagej.envisaje.spi.tools.Tool.class)

public class OvalTool extends RectangleTool {

    /** Creates a new instance of OvalTool */
    public OvalTool() {
    }

    @Override
    public String toString() {
        return NbBundle.getMessage (getClass(), "Oval");
    }

    @Override
    protected void draw (Rectangle toPaint, Graphics2D g2d, boolean fill) {
        if (fill) {
            g2d.setPaint (paintC.get().getPaint());
            g2d.fillOval(toPaint.x, toPaint.y, toPaint.width, toPaint.height);
            g2d.setColor (outlineC.get());
            g2d.drawOval(toPaint.x, toPaint.y, toPaint.width, toPaint.height);
        } else {
            g2d.drawOval(toPaint.x, toPaint.y, toPaint.width, toPaint.height);
        }
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon (DrawTool.load(DrawTool.class, "oval.png"));
    }

}
