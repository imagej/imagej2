/*
 * BrushTool.java
 *
 * Created on October 15, 2005, 6:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package imagej.envisaje.tools.spi;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.util.ChangeListenerSupport;
import imagej.envisaje.spi.tools.Customizer;
import imagej.envisaje.spi.tools.PaintParticipant.Repainter;
import imagej.envisaje.spi.tools.Tool;
import imagej.envisaje.api.image.Surface;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Convenience class for implementing tools which listen on the canvas and draw
 * on it or select it.  Implements the methods of PaintParticipant and 
 * CustomizerProvider w/o actually implementing the interfaces - just add them
 * to your subclass to have a basic implementation.
 *
 * MouseEvents are dispatched to this from PaintCanvas...
 * 
 * @author Timothy Boudreau & GBH
 */
public abstract class MouseDrivenTool extends MouseAdapter implements Tool, MouseMotionListener {

	private JComponent customizer = null;
	private final String iconBase;
	private final String name;
	private Icon icon = null;
	private final ChangeListenerSupport changes = new ChangeListenerSupport(this);

	protected MouseDrivenTool(Icon icon, String name) {
		this.icon = icon;
		this.iconBase = null;
		this.name = name;
		if (icon == null) {
			throw new NullPointerException("Icon may not be null"); //NOI18N
		}
	}

	protected MouseDrivenTool(String iconBase, String name) {
		this.iconBase = iconBase;
		this.name = name;
	}

	@Override
	public final Icon getIcon() {
		if (this.icon == null && iconBase != null) {
			return new ImageIcon(Utilities.loadImage(iconBase));
		}
		return icon;
	}

	public final String getName() {
		return name;
	}
	private Layer layer;

	public final void activate(Layer layer) {
		this.layer = layer;
		activated(layer);
	}

	protected void activated(Layer layer) {
		//do nothing
	}

	protected Layer getLayer() {
		return layer;
	}

	public final void deactivate() {
		this.layer = null;
		painting = false;
	}

	protected final boolean isActive() {
		return layer != null;
	}

	public final JComponent getCustomizer(boolean create) {
		if (customizer == null && create) {
			customizer = createCustomizer();
		}
		return customizer;
	}

	public boolean canAttach(Layer layer) {
		return layer.getLookup().lookup(Surface.class) != null;
	}

	/** Override to provide a customizer for the tool.  By default,
	 * simply creates a disabled JLabel saying "No customizer" */
	protected JComponent createCustomizer() {
		JLabel result = new JLabel();
		result.setEnabled(false);
		result.setHorizontalAlignment(SwingConstants.CENTER);
		result.setHorizontalTextPosition(SwingConstants.CENTER);
		result.setText(NbBundle.getMessage(MouseDrivenTool.class,
				"LBL_NoCustomizer")); //NOI18N
		return result;
	}
	boolean painting = false;

	@Override
	public final void mousePressed(MouseEvent e) {
		if (isActive()) {
			layer.getSurface().beginUndoableOperation(getName());
			painting = true;
			beginOperation(e.getPoint(), e.getModifiersEx());
		}
	}

	@Override
	public final void mouseReleased(MouseEvent e) {
		if (isActive() && painting) {
			dragged(e.getPoint(), e.getModifiersEx());
			endOperation(e.getPoint(), e.getModifiersEx());
			layer.getSurface().endUndoableOperation();
		}
	}

	@Override
	public final void mouseDragged(MouseEvent e) {
		if (painting) {
			dragged(e.getPoint(), e.getModifiersEx());
		}
	}

	/** The user has pressed the mouse, starting an operation */
	protected void beginOperation(Point p, int modifiers) {
	}

	/** The user has released the mouse, ending an operation */
	protected void endOperation(Point p, int modifiers) {
	}

	/** The mouse was dragged */
	protected abstract void dragged(Point p, int modifiers);

	@Override
	public final void mouseMoved(MouseEvent e) {
		//do nothing
	}
	public static final String BASIC_TOOLS_CATEGORY = "Basic Tools";

	public String getCategory() {
		return BASIC_TOOLS_CATEGORY;
	}

	public Lookup getLookup() {
		return Lookups.singleton(this);
	}
	protected Repainter repainter;

	public void attach(Repainter repainter) {
		this.repainter = repainter;
	}

	public void paint(Graphics2D g2d) {
		//do nothing
	}

	public Customizer getCustomizer() {
		return this instanceof Customizer ? (Customizer) this
				: null;
	}

//    public JComponent[] getComponents() {
//        return new JComponent[] { getCustomizer(true) };
//    }
	public void addChangeListener(ChangeListener l) {
		changes.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		changes.remove(l);
	}

	protected void fireChangeEvent() {
		changes.fire();
	}

	public Object get() {
		return null;
	}
}
