/*
 * Proxy.java
 *
 * Created on November 6, 2006, 1:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector;

/**
 * A Primitive which proxies another primitive.  In practice useful mainly for
 * visual primitives (Vectors, Volumes, Strokables).
 *
 * @author Tim Boudreau
 */
public interface Proxy {
    public Primitive getProxiedPrimitive();
}
