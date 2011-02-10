package ijx.event;

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

    /* Regarding threading...
     *
     * void resultChanged(LookupEvent ev) -- A change in lookup occured.
     Please note that this method should never block since it might be called
     from lookup implementation internal threads. If you block here you are
     in risk that the thread you wait for might in turn to wait for the
     lookup internal thread to finish its work.
     *
     */
}
