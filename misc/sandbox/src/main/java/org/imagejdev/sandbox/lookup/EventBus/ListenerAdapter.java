package org.imagejdev.sandbox.lookup.EventBus;

import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public class ListenerAdapter<T> implements LookupListener {

    private final EventBusListener eventBusListener;

    public ListenerAdapter(final EventBusListener eventBusListener) {
        this.eventBusListener = eventBusListener;
    }

    public void resultChanged(final LookupEvent event) {
        final Lookup.Result result = (Lookup.Result) event.getSource();

        if (!result.allInstances().isEmpty()) {
            eventBusListener.notify((T) result.allInstances().iterator().next());
        } else {
            eventBusListener.notify(null);
        }
    }
}
