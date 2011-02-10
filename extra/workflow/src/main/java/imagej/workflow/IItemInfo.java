/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

/**
 *
 * @author aivar
 */
public interface IItemInfo {
    public enum Type {
        STRING, INTEGER, FLOATING, URL, IMAGE, ITEM
    }

    public String getName();

    public Type getType();

    public Object getDefault();
}
