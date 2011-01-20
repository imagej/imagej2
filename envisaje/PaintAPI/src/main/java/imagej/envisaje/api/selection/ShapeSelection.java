/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.api.selection;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.openide.util.NbBundle;
/**
 * A selection involving shapes
 *
 * @author Tim Boudreau
 */
public final class ShapeSelection extends Selection<Shape> {
    private Shape content = null;
    
    public ShapeSelection() {
        super (Shape.class);
    }
    
    @Override
    public void add(Shape toAdd, Op op) {
        Shape old = content;
        System.err.println("Add a " + toAdd.getClass() + " with " + op + " to " + this);
        if (this.content == null || op == Op.REPLACE) {
            this.content = toAdd;
        } else {
            Area a = asArea();
            switch (op) {
                case ADD :
                    a.add(new Area(toAdd));
                    break;
                case SUBTRACT :
                    a.subtract(new Area(toAdd));
                    break;
                case XOR :
                    a.exclusiveOr(new Area(toAdd));
                    break;
                case INTERSECT :
                    a.intersect(new Area(toAdd));
                    break;
                default :
                    throw new AssertionError();
            }
        }
        Ed ed = new Ed(this, old, content, op);
        changed(ed);
    }
    
    @Override
    public Shape get() {
        return content;
    }
    
    private Area asArea() {
        if (!(content instanceof Area)) {
            content = new Area(content);
        }
        return (Area) content;
    }
    
    @Override
    public void clear() {
        Shape old = content;
        content = null;
        Ed ed = new Ed (this, old, content, null);
        changed(ed);
    }
    
    public void transform (AffineTransform xform) {
        if (content != null) {
            content = xform.createTransformedShape(content);
        }
    }
    
    public boolean supportsTransformation() {
        return true;
    }

    @Override
    public void paint(Graphics2D g) {
        if (content != null) {
            paintSelectionAsShape (g, content);
        }
    }
    
    private static final class Ed implements UndoableEdit {
        private Shape old;
        private Shape nue;
        private ShapeSelection sel;
        private Op op;
        Ed (ShapeSelection sel, Shape old, Shape nue, Op op) {
            this.sel = sel;
            this.old = old;
            this.nue = nue;
            this.op = op;
        }

        public void undo() throws CannotUndoException {
            sel.content = old;
        }

        public boolean canUndo() {
            return true;
        }

        public void redo() throws CannotRedoException {
            sel.content = nue;
        }

        public boolean canRedo() {
            return true;
        }

        public void die() {
            old = null;
            nue = null;
            sel = null;
            op = null;
        }

        public boolean addEdit(UndoableEdit anEdit) {
            return false;
        }

        public boolean replaceEdit(UndoableEdit anEdit) {
            return false;
        }

        public boolean isSignificant() {
            return true;
        }

        public String getPresentationName() {
            if (op == null) {
                return NbBundle.getMessage(ShapeSelection.class, 
                        "CLEAR_EDIT_NAME"); //NOI18N
            }
            return NbBundle.getMessage(ShapeSelection.class, "SEL_EDIT_NAME", //NOI18N
                    op.toString());
        }

        public String getUndoPresentationName() {
            if (op == null) {
                return NbBundle.getMessage(ShapeSelection.class, 
                        "CLEAR_UNDO_NAME"); //NOI18N
            }
            return NbBundle.getMessage(ShapeSelection.class, "UNDO_NAME", //NOI18N
                    op.toString());
        }

        public String getRedoPresentationName() {
            if (op == null) {
                return NbBundle.getMessage(ShapeSelection.class, 
                        "CLEAR_REDO_NAME"); //NOI18N
            }
            return NbBundle.getMessage(ShapeSelection.class, "REDO_NAME", //NOI18N
                    op.toString());
        }
    }

    @Override
    public String toString() {
        if (content == null) return "Empty ShapeSelection";
        StringBuilder sb = new StringBuilder("ShapeSelection: ");
        PathIterator it = content.getPathIterator(AffineTransform.getTranslateInstance(0d, 0d));
        double[] d = new double[6];
        while (!it.isDone()) {
            int op = it.currentSegment(d);
            int ct;
            switch (op) {
                case PathIterator.SEG_CLOSE :
                    sb.append ("Close");
                    ct = 0;
                    break;
                case PathIterator.SEG_CUBICTO :
                    sb.append ("Cubic To");
                    ct = 4;
                    break;
                case PathIterator.SEG_LINETO :
                    sb.append ("Line To");
                    ct = 2;
                    break;
                case PathIterator.SEG_MOVETO :
                    sb.append ("Move To");
                    ct = 2;
                    break;
                case PathIterator.SEG_QUADTO :
                    sb.append("Quad To");
                    ct = 4;
                    break;
                default :
                    throw new AssertionError ("" + op);
            }
            sb.append ('[');
            for (int i=0; i < ct; i++) {
                sb.append (d[i]);
                if (i != ct-1) {
                    sb.append(',');
                }
            }
            sb.append (']');
            it.next();
        }
        return sb.toString();
    }

    @Override
    public void translateFrom(Selection selection) {
        content = selection.asShape();
        selection.clearNoUndo();
    }

    @Override
    public Shape asShape() {
        return content;
    }

    @Override
    public void clearNoUndo() {
        content = null;
    }
    
    public void invert(Rectangle bounds) {
        if (content == null) {
            content = new Rectangle (bounds);
        } else {
            Area a = new Area (bounds);
            a.subtract(content instanceof Area ? (Area) content : new Area(content));
            content = a;
        }
    }
}
