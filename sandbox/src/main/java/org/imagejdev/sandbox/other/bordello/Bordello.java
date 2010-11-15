/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imagejdev.sandbox.other.bordello;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// from: http://www.artima.com/weblogs/viewpost.jsp?thread=238562


public class Bordello {

    private final static Map<Class<?>, Object> services = new ConcurrentHashMap<Class<?>, Object>();

    /**
     * Acquire an implementation of a service. If one has not already
     * been instantiated, instantiate the class defined by the
     * Implementor annotation on the interface
     */
    public static <T> T get(Class<T> interfaceClass) {
        synchronized (interfaceClass) {
            Object service = services.get(interfaceClass);
            if (service == null) {
                try {
                    Class<?> implementingClass =
                            interfaceClass.getAnnotation(Implementor.class).value();
                    service = implementingClass.newInstance();
                    services.put(interfaceClass, service);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return interfaceClass.cast(service);
        }
    }

    /**
     * Set an alternate service implementation.
     * Typically only called in unit tests.
     */
    public static <T> void set(Class<T> interfaceClass, T providor) {
        synchronized (interfaceClass) {
            services.put(interfaceClass, providor);
        }
    }
} 
