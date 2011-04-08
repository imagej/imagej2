/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.tools.brushes.tips;

import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import imagej.envisaje.spi.tools.Customizer;
import imagej.envisaje.tools.fills.FillCustomizer;
import imagej.envisaje.tools.spi.BrushTip;
import imagej.envisaje.tools.spi.Fill;
import imagej.envisaje.misccomponents.explorer.Customizable;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public class MultiPointTip implements BrushTip, Customizer, Customizable {
    private TipDesigner designer;
    private JPanel pnl;
    private final Rectangle scratch = new Rectangle();

    public Rectangle draw(Graphics2D g, Point p, int size) {
        Point[] points = designer.getPoints();
        if (points.length == 0) {
            return new Rectangle (0,0,0,0);
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        g.setPaint (FillCustomizer.getDefault().get().getPaint());
        for (Point pp : points) {
            scratch.x = p.x + (pp.x - ((size * size) / 2));
            scratch.y = p.y + (pp.y - ((size * size) / 2));
            scratch.width = size;
            scratch.height = size;
            g.fillOval(scratch.x, scratch.y, scratch.width, scratch.height);
            minX = Math.min (scratch.x, minX);
            minY = Math.min (scratch.y, minY);
            maxX = Math.max (scratch.x + scratch.width, maxX);
            maxY = Math.max (scratch.y + scratch.height, maxY);
        }
        return new Rectangle (minX, minY, maxX - minX, maxY - minY);
    }

    public String getName() {
        return NbBundle.getMessage (MultiPointTip.class, "LBL_MultiPointTip");
    }

    public Object get() {
        return null;
    }

    public JComponent getCustomizer() {
        if (designer == null) {
            designer = new TipDesigner();
            pnl = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.EAST;
            JLabel lbl = new JLabel (NbBundle.getMessage(MultiPointTip.class, "LBL_Color"));
            pnl.add (lbl, gbc);
            gbc.gridx = 1;
            pnl.add (designer, gbc);
            gbc.gridx=0;
            gbc.gridy=1;
        }
        return pnl;
    }

    public JComponent getComponent() {
        return (JComponent) getCustomizer();
    }
}
