/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package imagej.envisaje.tools.selectiontools;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import javax.swing.event.ChangeListener;
import imagej.envisaje.api.selection.Selection;
import imagej.envisaje.api.selection.Selection.Op;
import imagej.envisaje.spi.tools.PaintParticipant;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.util.ChangeListenerSupport;
import imagej.envisaje.tools.spi.MouseDrivenTool;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Timothy Boudreau
 */
@ServiceProvider(service = imagej.envisaje.spi.tools.Tool.class)
public class OvalSelectionTool extends MouseDrivenTool implements PaintParticipant {

	/** Creates a new instance of OvalSelectionTool */
	public OvalSelectionTool() {
		super("imagej/envisaje/tools/resources/roundselection.png",
				NbBundle.getMessage(OvalSelectionTool.class,
				"NAME_OvalSelectionTool"));   //XXX
	}
	private Ellipse2D.Double shape = null;

	protected void dragged(Point p, int modifiers) {
		if (shape == null && !p.equals(startPoint)) {
			shape = new Ellipse2D.Double();
		}
		if (shape != null) {
			if (startPoint != null) {
				shape.setFrame(startPoint.x, startPoint.y, p.x
						- startPoint.x, p.y - startPoint.y);
				fire();
			}
		}
		repainter.requestRepaint(null); //XXX bounds
	}

	public Shape getSelection() {
		return shape;
	}
	private ChangeListenerSupport changes = new ChangeListenerSupport(this);

	@Override
	public void addChangeListener(ChangeListener cl) {
		changes.add(cl);
	}

	@Override
	public void removeChangeListener(ChangeListener cl) {
		changes.remove(cl);
	}

	private void fire() {
		changes.fire();
	}

	public void clear() {
		shape = null;
		fire();
	}
	private Point startPoint = null;

	@Override
	protected void beginOperation(Point p, int modifiers) {
		startPoint = p;
	}

	@Override
	protected void endOperation(Point p, int modifiers) {
		super.endOperation(p, modifiers);
		Op op;
		boolean ctrlDown = (modifiers & KeyEvent.CTRL_DOWN_MASK) != 0;
		boolean altDown = (modifiers & KeyEvent.ALT_DOWN_MASK) != 0;
		if (ctrlDown && altDown) {
			op = Op.XOR;
		} else if (ctrlDown) {
			op = Op.INTERSECT;
		} else if (altDown) {
			op = Op.SUBTRACT;
		} else {
			op = Op.ADD;
		}
		Layer layer = this.getLayer();
		Selection sel = layer.getLookup().lookup(Selection.class);
		if (sel != null) {
			if (shape != null) {
				sel.add(shape, op);
			} else {
				sel.clear();
			}
		}
		startPoint = null;
	}

	public void attachRepainter(Repainter repainter) {
		super.repainter = repainter;
	}

	public void paint(Graphics2D g2d, Rectangle layerBounds, boolean commit) {
		if (shape != null && startPoint != null && !commit) {
			Selection.paintSelectionAsShape(g2d, shape);
		}
	}
}
