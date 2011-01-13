/*
 * PencilTool.java
 *
 * Created on October 16, 2005, 2:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.tools;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.imagejdev.imagine.spi.tools.Customizer;
import org.imagejdev.imagine.spi.tools.CustomizerProvider;
import org.imagejdev.imagine.tools.spi.Fill;
import org.imagejdev.imagine.tools.spi.MouseDrivenTool;
import org.imagejdev.misccomponents.explorer.FolderPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
/**
 *
 * @author Timothy Boudreau
 */
@ServiceProvider(service=org.imagejdev.imagine.spi.tools.Tool.class)
public class PencilTool extends MouseDrivenTool implements ChangeListener, ActionListener, CustomizerProvider, Customizer {
    public PencilTool() {
        super ("org/imagejdev/imagine/tools/resources/pencil.png", //NOI18N
                NbBundle.getMessage (PencilTool.class, "NAME_PencilTool")); //NOI18N
    }

    protected void dragged (Point p, int modifiers) {
        if (!isActive()) {
            return;
        }
        Graphics2D g2d = getLayer().getSurface().getGraphics();
        g2d.setPaint(getPaint());
        int half = diam / 2;
        g2d.fillRect(p.x - half, p.y - half, diam, diam);
    }

    private int diam = 24;
    private int getDiam() {
        return diam;
    }

    private void setDiam(int diam) {
        if (this.diam != diam) {
            this.diam = diam;
            if (view != null) {
                view.repaint();
            }
        }
    }

    private void setRound (boolean val) {
        if (val != round) {
            round = val;
            if (view != null) {
                view.repaint();
            }
        }
    }

    private boolean round = true;
    private boolean isRound() {
        return round;
    }

    private PencilSizeView view = null;
    private JRadioButton roundButton = null;
    private FolderPanel paintSelector = null;
    @Override
    protected JComponent createCustomizer() {
	JPanel result = new JPanel();
	result.setLayout (new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.weighty = 1.0;
	c.anchor = GridBagConstraints.FIRST_LINE_START;

        JLabel slbl = new JLabel (NbBundle.getMessage (PencilTool.class,
                "LBL_PencilSize")); //NOI18N

        JSlider slider = new JSlider (1, 24);
        slider.setValue (diam);
        slider.addChangeListener(this);
        slbl.setLabelFor (slider);

	result.add (slbl, c);
	c.gridx = 1;
	c.weightx = 1.0;
	c.fill = GridBagConstraints.HORIZONTAL;
	result.add (slider, c);

        view = new PencilSizeView();
	c.fill = GridBagConstraints.NONE;
	c.gridx = 2;
	c.weightx = 0;
	result.add (view, c);

	c.fill = GridBagConstraints.HORIZONTAL;
	c.anchor = GridBagConstraints.NORTH;
	c.gridx = 0;
	c.gridwidth = 3;
	c.gridy = 1;
	c.weighty = 1.0;
	c.weightx = 1.0;
        paintSelector = new FolderPanel ("fills", Fill.class); //NOI18N
	result.add (paintSelector, c);
        return result;
    }

    private Paint getPaint() {
        if (paintSelector != null) {
            Fill fill = (Fill) paintSelector.getSelection();
            if (fill != null) {
                return fill.getPaint();
            }
        }
        return Color.BLUE; //punt - can get called while initializing
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof JSlider) {
            JSlider sl = (JSlider) e.getSource();
            setDiam (sl.getValue());
        }
    }

    public void actionPerformed(ActionEvent e) {
        setRound (roundButton.isSelected());
    }

    private class PencilSizeView extends JComponent {
        public boolean isOpaque () {
            return false;
        }

        public void paint (Graphics g) {
            g.setColor (Color.BLACK);
            int x = getWidth() / 2;
            int y = getHeight() / 2;
            int half = diam / 2;
            g.fillRect(x - half, y - half, diam, diam);
        }

        public Dimension getPreferredSize() {
            return new Dimension (32, 32);
        }
    }

    public JComponent getComponent() {
        return createCustomizer();
    }
}
