/*
 *
 * Sun Public License Notice
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

import org.openide.util.lookup.ServiceProvider;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import javax.swing.event.ChangeListener;
import imagej.envisaje.api.selection.Selection;
import imagej.envisaje.api.selection.Selection.Op;
import imagej.envisaje.spi.tools.PaintParticipant;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.util.ChangeListenerSupport;
import imagej.envisaje.tools.spi.MouseDrivenTool;
import org.openide.util.NbBundle;
import static imagej.envisaje.tools.selectiontools.MutableRectangle.SW;
import static imagej.envisaje.tools.selectiontools.MutableRectangle.ANY;

/**
 *
 * Tool which lets the user draw a rectangular selection. Most of the work
 * is done in the MutableRectangle class, which is a Rectangle whose bounds
 * can be set by specifying a corner to move.
 *
 * @author Timothy Boudreau
 */
@ServiceProvider(service = imagej.envisaje.spi.tools.Tool.class)
public class RectangularSelectionTool extends MouseDrivenTool implements PaintParticipant {

	/**
	 * Creates a new instance of RectangularSelectionTool
	 */
	public RectangularSelectionTool() {
		super("imagej/envisaje/tools/resources/rectselection.png",
				NbBundle.getMessage(RectangularSelectionTool.class,
				"NAME_RectangularSelectionTool"));
	}

	public Shape getSelection() {
		// Do a defensive copy first
		return selection == null ? null
				: new Rectangle(selection);
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
	private MutableRectangle selection = null;
	private Point startingPoint = null;

	@Override
	protected void beginOperation(Point p, int modifiers) {
		startingPoint = p;
		if (selection != null) {
			int corner = selection.nearestCorner(p);

			setDraggingCorner(corner);
		} else {
			setDraggingCorner(SW);
		}
	}

	@Override
	protected void endOperation(Point p, int modifiers) {
		if (selection != null) {
			if (selection.width == 0 || selection.height == 0
					|| p.equals(startingPoint)) {
				selection = null;
			}
			fire();
		}
		Op op;
		boolean ctrlDown = (modifiers & KeyEvent.CTRL_DOWN_MASK) != 0;
		boolean altDown = (modifiers & KeyEvent.ALT_DOWN_MASK) != 0;
		boolean shiftDown = (modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0;
		if (ctrlDown && altDown) {
			op = Op.XOR;
		} else if (ctrlDown) {
			op = Op.INTERSECT;
		} else if (altDown) {
			op = Op.SUBTRACT;
		} else if (shiftDown) {
			op = Op.ADD;
		} else {
			op = Op.REPLACE;
		}
		Layer layer = this.getLayer();
		Selection sel = layer.getLookup().lookup(Selection.class);
		if (sel != null) {
			if (selection != null) {
				sel.add(selection, op);
			} else {
				sel.clear();
			}
		} else {
			System.err.println("SELECTION NULL");
		}
		startingPoint = null;
	}

	public void clear() {
		selection = null;
		draggingCorner = ANY;
		fire();
	}
	private int draggingCorner = ANY;

	protected void dragged(Point p, int modifiers) {
		if (p == null || startingPoint == null) {
			return;
		}
		if (selection == null) {
			if (p.x != startingPoint.x && p.y != startingPoint.y) {
				selection = new MutableRectangle(startingPoint, p);
				fire();
			}
		} else {
			int currCorner = getDraggingCorner();
			int corner = selection.setPoint(p, currCorner);

			if ((modifiers & java.awt.event.KeyEvent.SHIFT_DOWN_MASK) != 0) {
				selection.makeSquare(currCorner);
			}
			if (corner == -2 || (corner != currCorner && corner != -1)) {
				if (corner != -2) {
					setDraggingCorner(corner);
				}
				fire();
			}
		}
//        repainter.requestRepaint(selection);
		repainter.requestRepaint(null); //XXX
	}

	private int getDraggingCorner() {
		return draggingCorner;
	}

	private void setDraggingCorner(int draggingCorner) {
		this.draggingCorner = draggingCorner;
	}

	public void paint(Graphics2D g2d, Rectangle layerBounds, boolean commit) {
		if (selection != null && !commit && startingPoint != null) {
			Selection.paintSelectionAsShape(g2d, selection);
		}
	}

	public void attachRepainter(Repainter repainter) {
		//XXX why does this field exist in the superclass?
		super.repainter = repainter;
	}
}
