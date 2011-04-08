/*
 * Aggregate.java
 *
 * Created on November 6, 2006, 10:34 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector;

/**
 * A Primitive which acts as a container by which multiple other primitives
 * may be manipulated as a group.
 *
 * @author Tim Boudreau
 */
public interface Aggregate extends Primitive {
    public int getPrimitiveCount();
    public Primitive getPrimitive (int i);
    public int getVisualPrimitiveCount();
    public Primitive getVisualPrimitive (int i);
}
