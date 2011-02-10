
package org.imagejdev.sandbox.fromImagine;
/*
 * Sensor.java
 * Originally created on October 27, 2006, 7:35 PM
   Borrowed from: org.netbeans.paint.api.actions;
 *
 */

import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Class which consolidates lookup listeners.  All notifications
 * happen on the event thread.
 *
 * @author Tim Boudreau
 */
public final class Sensor <T> {
    static final Map <Lookup, Map <Class, Sensor>> sensors =
            new ConcurrentHashMap <Lookup, Map <Class, Sensor>> ();

    private Lookup.Result <T> result;
    private Lookup lkp;
    private final Class targetClass;
    Set <WeakReference <Notifiable<T>>> toNotify =
            new HashSet <WeakReference <Notifiable<T>>> ();

    Sensor(Class <T> targetClass, Lookup lkp) {
        this.targetClass = targetClass;
        this.lkp = lkp;
    }

    Sensor(Class <T> targetClass) {
        this (targetClass, Utilities.actionsGlobalContext());
    }

    private L l;
    private class L implements LookupListener {
        public void resultChanged (LookupEvent e) {
            change ((Collection<T>)result.allInstances()); //XXX how to do the generic right?
        }
    }

    Class<T> getTargetClass() {
        return targetClass;
    }

    static <T> Sensor create (Class <T> clazz, Lookup lkp) {
        Sensor sensor = new Sensor (clazz, lkp);
        return sensor;
    }

    public static <T> void register (Lookup lkp, Class <T> clazz, Notifiable<T> n) {
        Map <Class, Sensor> m = sensors.get(lkp);
        Sensor <T> sensor;
        if (m == null) {
            m = new ConcurrentHashMap <Class, Sensor>();
            sensors.put (lkp, m);
            sensor = null;
        } else {
            sensor = m.get(clazz);
        }
        if (sensor == null) {
            sensor = create (clazz, lkp);
            m.put (clazz, sensor);
        }
        sensor.doRegister (n);
    }

    public static <T> void registerOnDefaultLookup (Class<T> clazz, Notifiable<T> n) {
        register (Lookup.getDefault(), clazz, n);
    }

    public static <T> void registerOnGlobalActionContextLookup (Class<T> clazz, Notifiable<T> n) {
        register (Utilities.actionsGlobalContext(), clazz, n);
    }

    void doRegister (Notifiable <T> n) {
        int size = toNotify.size();
        toNotify.add (new WeakReference(n));
        if (size == 0) {
            addNotify();
        }
    }

    void doUnregister (Notifiable <T> n) {
        for (Iterator <WeakReference<Notifiable<T>>> i = toNotify.iterator(); i.hasNext();) {
            if (i.next().get() == n) {
                i.remove();
            }
        }
        if (toNotify.isEmpty()) {
            removeNotify();
        }
    }

    private static R r = null;
    void change (Collection <T> c) {
        synchronized (Sensor.class) {
            if (r == null) {
                r = new R (c, this);
            } else {
                r.add (c, this);
            }
        }
    }

    private void scan() {
        for (Iterator <WeakReference<Notifiable<T>>> i = toNotify.iterator(); i.hasNext();) {
            if (i.next().get() == null) {
                i.remove();
            }
        }
        if (toNotify.isEmpty()) {
            removeNotify();
        }
    }

    private static void scanAll() {
        for (Iterator <Lookup> lit=sensors.keySet().iterator(); lit.hasNext();) {
            Lookup l = lit.next();
            Map <Class, Sensor> m = sensors.get (l);
            for (Iterator <Class> mit = m.keySet().iterator(); mit.hasNext();) {
                Class clazz = mit.next();
                Sensor s = m.get (clazz);
                for (Iterator <Reference<Notifiable>> i = s.toNotify.iterator(); i.hasNext();) {
                    if (i.next().get() == null) {
                        i.remove();
                    }
                }
                if (s.toNotify.isEmpty()) {
                    m.remove(clazz);
                }
            }
            if (m.isEmpty()) {
                lit.remove();
            }
        }
    }

    private static class R implements Runnable {
        Map <Sensor, Collection> m = new ConcurrentHashMap();
        <T> R (Collection <T> c, Sensor <T> sensor) {
            m.put (sensor, c);
            EventQueue.invokeLater (this);
        }

        <T> void add (Collection <T> c, Sensor <T> sensor) {
            m.put (sensor, c);
        }

        public void run() {
            synchronized (Sensor.class) {
                Sensor.r = null;
            }
            for (Map.Entry <Sensor, Collection> e : m.entrySet()) {
                Collection <Reference<Notifiable>> toNotify = e.getKey().toNotify;
                for (Iterator <Reference<Notifiable>> i = toNotify.iterator(); i.hasNext();) {
                    Notifiable n = i.next().get();
                    Class targetClass = e.getKey().targetClass;
                    if (n == null) {
                        i.remove();
                    } else {
                        try {
                            n.notify (e.getValue(), targetClass);
                        } catch (Exception ex) {
                            Logger.getLogger (Sensor.class.getName()).log (Level.SEVERE, null, ex);
                        } finally {
                            synchronized (n) {
                                n.notifyAll();
                            }
                        }
                    }
                }
                if (toNotify.isEmpty()) {
                    e.getKey().removeNotify();
                }
            }
            m.clear();
            scanAll();
        }
    }

    private void addNotify() {
        result = lkp.lookupResult(targetClass);
        if (l == null) {
            l = new L();
        }
        result.addLookupListener(l);
        result.allInstances();
    }

    private void removeNotify() {
        Map <Class, Sensor> m = sensors.get (lkp);
        if (m != null) {
            Sensor <T> sensor = m.remove (targetClass);
            assert sensor == this;
            if (m.isEmpty()) {
                sensors.remove (lkp);
            }
        }
        if (l != null) {
            result.removeLookupListener (l);
            l = null;
            result = null;
        }
    }

    public interface Notifiable <T> {
        public void notify (Collection <T> coll, Class target);
    }
}
