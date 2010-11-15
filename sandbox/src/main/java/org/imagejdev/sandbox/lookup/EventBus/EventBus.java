/*
From http://netbeans.dzone.com/news/publish-subscribe-netbeans-pla

 */
package org.imagejdev.sandbox.lookup.EventBus;

import java.util.HashMap;
import java.util.Map;
import org.openide.util.Lookup.Result;

public class EventBus {

    private static final EventBus instance = new EventBus();
    private final CentralLookup centralLookup = CentralLookup.getDefault();
    private final Map<Class<?>, Result<?>> resultMapByClass = new HashMap<Class<?>, Result<?>>();
    private final Map<EventBusListener<?>, ListenerAdapter<?>> adapterMapByListener = new HashMap<EventBusListener<?>, ListenerAdapter<?>>();

    private EventBus() {
    }

    public static EventBus getDefault() {
        return instance;
    }

    public void publish(final Object object) {
        if (object == null) {
            throw new IllegalArgumentException("object is mandatory");
        }
        for (final Object old : centralLookup.lookupAll(object.getClass())) {
            centralLookup.remove(old);
        }
        if (object != null) {
            centralLookup.add(object);
        }
    }

    public void unpublish(final Class<?> topic) {
        for (final Object old : centralLookup.lookupAll(topic)) {
            centralLookup.remove(old);
        }
    }

    public synchronized <T> void subscribe(final Class<T> topic, final EventBusListener<T> listener) {
        Result<?> result = resultMapByClass.get(topic);
        if (result == null) {
            result = centralLookup.lookupResult(topic);
            resultMapByClass.put(topic, result);
            result.allInstances();
        }
        final ListenerAdapter<T> adapter = new ListenerAdapter<T>(listener);
        adapterMapByListener.put(listener, adapter);
        result.addLookupListener(adapter);
    }

    public synchronized <T> void unsubscribe(final Class<T> topic, final EventBusListener<T> listener) {
        final Result<?> result = resultMapByClass.get(topic);
        if (result == null) {
            throw new IllegalArgumentException(String.format("Never subscribed to %s", topic));
        }
        final ListenerAdapter<T> adapter = (ListenerAdapter<T>) adapterMapByListener.remove(listener);
        result.removeLookupListener(adapter);
    }
}
