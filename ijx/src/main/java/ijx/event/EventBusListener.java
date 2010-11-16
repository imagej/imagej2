/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx.event;

public interface EventBusListener<T> {

    public void notify(T object);
}
