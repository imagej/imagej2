/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.tools.brushes.tips;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

/**
 *
 * @author tim
 */
class TipDesigner extends JComponent implements MouseListener {
    private static final int COUNT = 32;
    private Set <Point> points = new HashSet<Point>();
    TipDesigner() {
        addMouseListener (this);
        setBorder (BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        setOpaque(true);
        setBackground (new Color (200, 200, 200));
    }
    
    public void clear() {
        points.clear();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension result = new Dimension (40, 40);
        Insets ins = getInsets();
        result.width += ins.left + ins.right;
        result.height += ins.top + ins.bottom;
        return result;
    }

    public void mouseClicked(MouseEvent e) {
        
    }

    public void mousePressed(MouseEvent e) {
        
    }

    public void mouseReleased(MouseEvent e) {
        Point p = e.getPoint();
        points.add (p);
        repaint (p.x - 1, p.y - 1, p.x + 2, p.y + 2);
    }

    public void mouseEntered(MouseEvent e) {
        
    }

    public void mouseExited(MouseEvent e) {
        
    }
    
    public Point[] getPoints () {
        Point[] result = (Point[]) points.toArray(new Point[points.size()]);
        return result;
    }
    
    @Override
    public void paint (Graphics g) {
        super.paint(g);
        g.setColor (Color.BLACK);
        for (Point p : getPoints()) {
            g.drawLine(p.x, p.y, p.x, p.y);
        }
    }
}
