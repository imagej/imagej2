/*
 * ClearTool.java
 *
 * Created on October 1, 2006, 1:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.tools;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.spi.tools.Tool;
import imagej.envisaje.api.image.Surface;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service=imagej.envisaje.spi.tools.Tool.class)

public class ClearTool extends MouseAdapter implements Tool, KeyListener {

    public ClearTool() {
    }

    public String getName() {
        return NbBundle.getMessage (getClass(), "Clear");
    }

    public boolean canAttach (Layer layer) {
        return layer.getLookup().lookup (Surface.class) != null;
    }

    private Layer layer;
    public void activate(Layer layer) {
        this.layer = layer;
    }

    public void deactivate() {
        this.layer = null;
    }

    public JComponent getCustomizer(boolean create) {
        return null;
    }

    public String getInstructions() {
        return NbBundle.getMessage (getClass(), "Click_or_press_Enter_to_clear_the_canvas");
    }

    public Icon getIcon() {
        return new ImageIcon (DrawTool.load(ClearTool.class, "clear.png"));
    }

    public void mouseReleased (MouseEvent e) {
        go();
    }

    public void keyTyped(KeyEvent e) {
        //do nothing
    }

    public void keyPressed(KeyEvent e) {
        //do nothing
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            go();
        }
    }

    public String toString() {
        return NbBundle.getMessage (getClass(), "Clear");
    }

    private void go() {
        Graphics g = layer.getSurface().getGraphics();
        g.setColor(new Color (255, 255, 255, 0));
        Rectangle r = layer.getBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        g.dispose();
    }

    public Lookup getLookup() {
        return Lookups.singleton(this);
    }
}
