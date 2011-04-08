/*
 * EraserTool.java
 *
 * Created on October 16, 2005, 12:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package imagej.envisaje.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SliderUI;
import imagej.envisaje.spi.tools.CustomizerProvider;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.image.Surface;
import imagej.envisaje.tools.spi.MouseDrivenTool;
import imagej.envisaje.misccomponents.PopupSliderUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Timothy Boudreau
 */
@ServiceProvider(service = imagej.envisaje.spi.tools.Tool.class)
public class EraserTool extends MouseDrivenTool implements ChangeListener, ActionListener, CustomizerProvider {

	public EraserTool() {
		super("imagej/envisaje/tools/resources/eraser.png",
				NbBundle.getMessage(EraserTool.class, "NAME_EraserTool"));
	}

	@Override
	public boolean canAttach(Layer layer) {
		return layer.getLookup().lookup(Surface.class) != null;
	}

	protected void dragged(Point p, int modifiers) {
		if (!isActive()) {
			return;
		}
		Graphics2D g2d = getLayer().getSurface().getGraphics();
		if (round) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g2d.setBackground(new Color(255, 255, 255, 0));
		int half = diam / 2;
		if (round) {
			g2d.setComposite(AlphaComposite.Clear);
			g2d.fillOval(p.x - half, p.y - half, diam, diam);
		} else {
			g2d.clearRect(p.x - half, p.y - half, diam, diam);
		}

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

	private void setRound(boolean val) {
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
	private EraserSizeView view = null;
	private JRadioButton roundButton = null;

	protected JComponent createCustomizer() {
		JPanel result = new JPanel();
		result.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = c.LINE_START;
		c.weighty = 1.0;

		JLabel slbl = new JLabel(NbBundle.getMessage(EraserTool.class,
				"LBL_EraserSize"));

		result.add(slbl, c);

		view = new EraserSizeView();

		JSlider slider = new JSlider(1, 24);
		slider.setValue(diam);
		slider.addChangeListener(this);
		slbl.setLabelFor(slider);
		slider.setUI((SliderUI) PopupSliderUI.createUI(slider));

		c.gridx = 1;
		c.weightx = 1.0;
		result.add(slider);

		c.weightx = 0.0;
		c.gridx = 3;
		c.gridwidth = 2;
		result.add(view);

		c.gridy = 1;
		c.gridx = 1;
		c.weightx = 1.0;

		ButtonGroup bg = new ButtonGroup();
		JRadioButton squareButton = new JRadioButton(NbBundle.getMessage(EraserTool.class, "SHAPE_Square"));
		roundButton = new JRadioButton(NbBundle.getMessage(EraserTool.class, "SHAPE_Round"));
		roundButton.setSelected(true);
		bg.add(squareButton);
		bg.add(roundButton);
		squareButton.addActionListener(this);
		roundButton.addActionListener(this);
		result.add(squareButton, c);
		c.gridx++;
		result.add(roundButton, c);
		//XXX delete Customizer class;
		return result;
	}

	public void stateChanged(ChangeEvent e) {
		JSlider sl = (JSlider) e.getSource();
		setDiam(sl.getValue());
	}

	public void actionPerformed(ActionEvent e) {
		setRound(roundButton.isSelected());
	}

	private class EraserSizeView extends JComponent {

		public boolean isOpaque() {
			return false;
		}

		public void paint(Graphics g) {
			g.setColor(Color.BLACK);
			Graphics2D g2d = (Graphics2D) g;
			Object hint = null;
			if (round) {
				hint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
				g2d.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
			}
			int x = getWidth() / 2;
			int y = getHeight() / 2;
			int half = diam / 2;
			if (round) {
				g.drawOval(x - half, y - half, diam, diam);
				g2d.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						hint);
			} else {
				g.drawRect(x - half, y - half, diam, diam);
			}
		}

		public Dimension getPreferredSize() {
			return new Dimension(32, 32);
		}
	}
}
