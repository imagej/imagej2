/*
 * ToolSensitiveAction.java
 *
 * Created on October 25, 2006, 10:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import imagej.envisaje.api.actions.Sensor.Notifiable;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Any subclass needs two public constructors - one no-arg and one taking an
 * arg of Lookup
 *
 * @author Tim Boudreau
 */
public abstract class GenericContextSensitiveAction <T> implements ContextAwareAction {
    protected final Lookup lookup;
    protected Class <T> targetClass;
    protected GenericContextSensitiveAction(Lookup lookup, Class <T> c) {
        this.lookup = lookup == null ?
            Utilities.actionsGlobalContext() :
            lookup;
        if (this.lookup == null) {
            throw new NullPointerException ("Null lookup!"); //NOI18N
        }
        init (c);
        Collection <? extends T> coll = this.lookup.lookupAll(c);
        setEnabled (checkEnabled (coll, c));
    }

    protected GenericContextSensitiveAction(Class c) {
        this ((Lookup) null, c);
    }

    protected GenericContextSensitiveAction(String bundleKey, Class c) {
        this ((Lookup) null, c);
        if (bundleKey != null) {
            String name;
            try {
                name = NbBundle.getMessage (getClass(), bundleKey);
            } catch (MissingResourceException mre) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                        "Missing string from Bundle.properties in package" + //NOI18N
                        "with " + getClass().getName(), mre); //NOI18N
                name = bundleKey;
            }
            setDisplayName (name);
        }
    }

    protected GenericContextSensitiveAction (Lookup lkp) {
        this.lookup = lkp;
        if (this.lookup == null) {
            throw new NullPointerException ("Null lookup!");
        }
    }

    private void init (Class c) {
        if (c == null) {
            throw new NullPointerException ("Passed class is null"); //NOI18N
        }
        this.targetClass = c;
        Sensor.register(lookup, c, n);
    }

    protected final Class getClassesNeededInLookupForEnablement() {
        return targetClass;
    }

    public Action createContextAwareInstance(Lookup lookup) {
        Class clazz = getClass();
        try {
            Constructor c = clazz.getConstructor(Lookup.class);
            GenericContextSensitiveAction result =
                    (GenericContextSensitiveAction) c.newInstance(lookup);
            result.init (targetClass);
            String name = (String) getValue(Action.NAME);
            if (name != null) {
                result.setDisplayName(name);
            }
            Icon icon = (Icon) getValue (Action.SMALL_ICON);
            if (icon != null) {
                result.setIcon(icon);
            }
            return result;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    clazz + " does not have a constructor that takes a Lookup", //NOI18N
                    ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(clazz.getName()).log(Level.SEVERE,
                    clazz + " does not have a constructor that takes a Lookup", //NOI18N
                    ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(clazz.getName()).log(Level.SEVERE,
                    clazz + " does not have a constructor that takes a Lookup", //NOI18N
                    ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(clazz.getName()).log(Level.SEVERE,
                    clazz + " does not have a constructor that takes a Lookup", //NOI18N
                    ex);
        } catch (SecurityException ex) {
            throw new AssertionError (ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(clazz.getName()).log(Level.SEVERE,
                    clazz + " does not have a constructor that takes a Lookup", //NOI18N
                    ex);
        }
        return this;
    }

    //Yes, it's a microoptimization, but why not...
    private Map <String, Object> map;
    public final Object getValue (String key) {
        return map == null ? null : map.get(key);
    }
    
    public final void putValue (String key, Object val) {
        Object old = map == null ? null : map.get(key);
        if (map == null) map = new HashMap<String, Object>();
        map.put (key, val);
        firePropertyChange(key, old, val);
    }
    /*
    private static final int ARR_PROP_COUNT = 6;
    private final String[] keys = new String[ARR_PROP_COUNT];
    private final Object[] vals = new Object[ARR_PROP_COUNT];
    public final Object getValue(String key) {
        Object result = null;
        int last = 0;
        for (last = 0; last < keys.length; last++) {
            if (keys[last] == null) {
                break;
            }
            if (key.equals(keys[last])) {
                result = vals[last];
                break;
            }
        }
        return result != null ? result : map == null ? null : map.get(key);
    }

    public final void putValue(String key, Object value) {
        int last = 0;
        for (last = 0; last < keys.length; last++) {
            if (keys[last] == null) {
                keys[last] = key;
                vals[last] = value;
                firePropertyChange (key, null, value);
                return;
            }
            if (key.equals(keys[last])) {
                Object old = vals[last];
                vals[last] = value;
                if (old != value) {
                    firePropertyChange (key, old, value);
                }
                return;
            }
            if (map == null) {
                map = new HashMap<String, Object>();
            }
            Object old = map.put (key, value);
            if (old != value) {
                firePropertyChange (key, old, value);
            }
        }
    }
     */ 

    public final void setEnabled(boolean b) {
        boolean was = isEnabled();
        enabled = b;
        if (enabled != was) {
            firePropertyChange ("enabled", Boolean.valueOf(was),
                    Boolean.valueOf (enabled));
        }
    }

    private boolean enabled = true;
    public final boolean isEnabled() {
        return enabled;
    }

    private void firePropertyChange (String s, Object o, Object n) {
        PropertyChangeListener[] ll = (PropertyChangeListener[])
                l.toArray(new PropertyChangeListener[0]);
        if (ll.length != 0) {
            PropertyChangeEvent evt = new PropertyChangeEvent (this, s, o, n);
            for (int i = 0; i < ll.length; i++) {
                ll[i].propertyChange(evt);
            }
        }
    }

    private final List <PropertyChangeListener> l =
            Collections.synchronizedList (new LinkedList <PropertyChangeListener> ());

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        l.add (listener);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        l.remove (listener);
    }

    private final N n = new N();
    final class N implements Notifiable {
        public final void notify(Collection coll, Class clazz) {
            boolean old = enabled;
            enabled = checkEnabled(coll, clazz);
            if (old != enabled) {
                firePropertyChange ("enabled", Boolean.valueOf(old),  //NOI18N
                        Boolean.valueOf(enabled));
            }
        }
    }

    protected boolean checkEnabled(Collection <? extends T> coll, Class clazz) {
        return !coll.isEmpty();
    }

    protected final void setDisplayName (String name) {
        putValue (Action.NAME, name);
    }

    protected final void setDescription (String desc) {
        putValue (Action.SHORT_DESCRIPTION, desc);
    }

    protected final void setIcon (Icon icon) {
        putValue (Action.SMALL_ICON, icon);
    }

    protected final void setIcon (Image img) {
        Icon icon = new ImageIcon (img);
        setIcon (icon);
    }

    public final void actionPerformed (ActionEvent ae) {
        T t = (T) lookup.lookup(targetClass);
        performAction (t);
    }

    protected abstract void performAction(T t);
}
