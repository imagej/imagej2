/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.imagecachediagnostics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import imagej.envisaje.imagenio.nio.CacheMap;
import imagej.envisaje.imagenio.nio.CacheMap.Region;

/**
 *
 * @author tim
 */
public class RegionPanel extends JPanel {
    private final Region region;
    private final CacheMap map;
    public RegionPanel (Region region, CacheMap map) {
        this.region = region;
        this.map = map;
        switch (region.getState()) {
            case IN_USE :
                setBackground (new Color (128, 128, 255));
                break;
            case UNUSED :
                setBackground (Color.WHITE);
                break;
            case USED_BUT_OWNER_GONE :
                setBackground (Color.CYAN);
                break;
        }
        setOpaque(true);
        setLayout (new BorderLayout());
        JLabel lbl = new JLabel (map.getRegions().indexOf(region) + "");
        add (lbl, BorderLayout.CENTER);
        setBorder (BorderFactory.createLineBorder(Color.BLACK));
    }
    
    @Override
    public void paint (Graphics g) {
        super.paint (g);
        long sz = region.getEnd() - region.getStart();
        Insets ins = getInsets();
        if (sz != region.getUsedSize()) {
            double dif = (int) (sz - region.getUsedSize());
            double factor = dif / (double) sz;
            double w = getWidth();
            int whiteArea = (int) (factor * w);
            if (whiteArea > 0) {
                g.setColor (Color.WHITE);
                g.fillRect (getWidth() - (whiteArea + ins.right), ins.top, 
                        whiteArea, getHeight() - (ins.top + ins.bottom));
            }
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        ToolTipManager.sharedInstance().registerComponent(this);
    }
    
    @Override
    public void removeNotify() {
        ToolTipManager.sharedInstance().unregisterComponent(this);
        super.removeNotify();
    }
    
    @Override
    public Dimension getPreferredSize() {
        if (getParent() == null || getParent().getWidth() == 0) {
            return new Dimension (10, 10);
        }
        double sz = region.getEnd() - region.getStart();
        double len = map.getCacheFileLength();
        double factor = sz / len;
        double parentWidth = getParent().getWidth();
        int prefWidth = (int)(parentWidth * factor);
        Insets ins = getParent().getInsets();
        int prefHt = getParent().getHeight() - (ins.top + ins.bottom);
        System.err.println("PrefWidth for " + region + " " + prefWidth);
        return new Dimension (prefWidth, prefHt);
    }

    @Override
    public String getToolTipText() {
        StringBuilder sb = new StringBuilder("<html>");
        int sz = region.getUsedSize();
        sb.append ("<b>Size:</b> " + sz + "<br>");
        sb.append ("<b>Approx Dimensions:</b> " + (sz / 8));
        sb.append ("<br><b>Start:</b> " + region.getStart());
        sb.append ("<br><b>End:</b>" + region.getEnd());
        sb.append ("<br><b>Size: </b>" + region.getUsedSize());
        sb.append ("<br><b>Used Size: </b>" + (region.getEnd() - region.getStart()));
        sb.append ("<br><b>State:</b>: " + region.getState() + "<br>");
        StackTraceElement[] stack = region.getAllocationBacktrace();
        for (StackTraceElement t : stack) {
            sb.append (t);
            sb.append ("<br>");
        }
        return sb.toString();
    }
}
