/*
 * TextTool.java
 *
 * Created on September 28, 2006, 10:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.tools;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.spi.tools.Customizer;
import imagej.envisaje.spi.tools.CustomizerProvider;
import imagej.envisaje.spi.tools.PaintParticipant;
import imagej.envisaje.spi.tools.Tool;
import imagej.envisaje.api.image.Surface;
import imagej.envisaje.api.toolcustomizers.AggregateCustomizer;
import imagej.envisaje.api.toolcustomizers.Constants;
import imagej.envisaje.api.toolcustomizers.Customizers;
import imagej.envisaje.api.toolcustomizers.FontStyle;
import imagej.envisaje.tools.fills.FillCustomizer;
import imagej.envisaje.tools.spi.Fill;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */

@ServiceProvider(service=imagej.envisaje.spi.tools.Tool.class)
public class TextTool implements Tool, KeyListener, MouseListener, MouseMotionListener, PaintParticipant, CustomizerProvider {
    public boolean canAttach (Layer layer) {
        return layer.getLookup().lookup (Surface.class) != null;
    }

    public void keyTyped(KeyEvent e) {
        if (armed && repainter != null) {
//            txt.append (e.getKeyChar());
            repainter.requestRepaint(null);
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
//            if (txt.length() > 0) {
//                txt.deleteCharAt(txt.length() - 1);
//                if (armed) {
//                    repainter.requestRepaint(null);
//                }
//                e.consume();
//            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        setLoc (e.getPoint());
    }

    public void mouseReleased(MouseEvent e) {
        if (armed) {
            commit (e.getPoint());
        }
    }

    public void paint(Graphics2D g2d, Rectangle layerBounds, boolean commit) {
        committing = commit;
        paint (g2d);
        committing = false;
    }


    boolean committing = false;
    private void commit (Point p) {
        setLoc (p);
        repainter.requestCommit();
    }

    boolean armed;
    public void mouseEntered(MouseEvent e) {
        armed = true;
        Point p = e.getPoint();
        setLoc (p);
        repainter.requestRepaint(null);
    }

    public void mouseExited(MouseEvent e) {
        armed = false;
        setLoc (null);
        repainter.requestRepaint(null);
    }

    public void paint(Graphics2D g2d) {
        String text = textC.get();
        if (text.length() == 0 || "".equals(text.trim())) {
            return;
        }
        Composite comp = null;
        if (!committing) {
            comp = g2d.getComposite();
            g2d.setComposite (AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
        }
        g2d.setFont (getFont());
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (loc == null) {
            return;
        }
        g2d.setPaint(fillC.get().getPaint());
        g2d.drawString(text, loc.x, loc.y);
        if (!committing) {
            g2d.setComposite (comp);
        }
    }

    public void mouseDragged(MouseEvent e) {
        armed = true;
        setLoc (e.getPoint());
    }

    public void mouseMoved(MouseEvent e) {
        armed = true;
        setLoc (e.getPoint());
    }

    private Point loc = null;
    private void setLoc(Point p) {
        loc = p;
        repainter.requestRepaint (null);
    }

    @Override
    public String toString() {
        return NbBundle.getMessage (TextTool.class, "Text"); //NOI18N
    }

    public String getInstructions() {
        return NbBundle.getMessage (TextTool.class, "Click_to_position_text"); //NOI18N
    }

    public Icon getIcon() {
        return new ImageIcon (DrawTool.load(DrawTool.class, NbBundle.getMessage (TextTool.class, "text.png")));
    }

    public String getName() {
        return toString();
    }

    private Layer layer;
    public void activate(Layer layer) {
        this.layer = layer;
    }

    public void deactivate() {
        repainter.requestRepaint(null);
        armed = false;
        loc = null;
    }

    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    private Repainter repainter;
    public void attachRepainter(PaintParticipant.Repainter repainter) {
        this.repainter = repainter;
    }

    Customizer<Fill> fillC = FillCustomizer.getDefault();
    Customizer<String> textC = Customizers.getCustomizer(String.class, Constants.TEXT);
    Customizer<Float> sizeC = Customizers.getCustomizer(Float.class, Constants.FONT_SIZE, 4F, 180F);
    Customizer<FontStyle> styleC = Customizers.getCustomizer(FontStyle.class, Constants.FONT_STYLE);
    Customizer<Font> fontC = Customizers.getCustomizer(Font.class, Constants.FONT);
    
    public Customizer getCustomizer() {
        return new AggregateCustomizer("foo", textC, fontC, sizeC, styleC, fillC); //NOI18N
    }
    
    private Font getFont() {
        Font f = fontC.get();
        float fontSize = sizeC.get();
        int style = styleC.get().toFontConstant();
        return f.deriveFont(style, fontSize);
    }
}
