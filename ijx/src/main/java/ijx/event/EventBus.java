/*
From http://netbeans.dzone.com/news/publish-subscribe-netbeans-pla

 * Publisher: e.g.: EventBus.getDefault().publish(new StatusMessage(s));
 *
 * Subscriber:
 * In constructor or initialization:
       EventBus.getDefault().subscribe(StatusMessage.class, listener);
 *
 * and the listener:
    private final EventBusListener<StatusMessage> listener = new EventBusListener<StatusMessage>() {
        @Override
        public void notify(final StatusMessage msg) {
            if (msg != null) {
                StatusLineSwing.this.setText(msg.getMessage());
            }
        }
    };

 Copy/Paste this:
 EventBus.getDefault().subscribe(Topic.class, listener);

    private final EventBusListener<Topic> listener = new EventBusListener<Topic>() {
        @Override
        public void notify(final Topic msg) {
            if (msg != null) {
                //setText(msg.getMessage());
            }
        }
    };
 *
 */
package ijx.event;

import ijx.CentralLookup;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.Lookup.Result;

public class EventBus {

    private static final EventBus instance = new EventBus();
    private final CentralLookup centralLookup = CentralLookup.getDefault();
    private final Map<Class<?>, Result<?>> resultMapByClass = new HashMap<Class<?>, Result<?>>();
    private final Map<EventBusListener<?>, ListenerAdapter<?>> adapterMapByListener =
            new HashMap<EventBusListener<?>, ListenerAdapter<?>>();

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
