package ijx.gui;

import ijx.event.EventBus;
import ijx.event.EventBusListener;

/**
 *
 * @author GBH
 */
public class StatusBar {

    // Senders do this:    EventBus.getDefault().publish("Status Message");

    // @todo Add component for displaying messages
    public StatusBar() {
            EventBus.getDefault().subscribe(String.class, listener);
    }

    private final EventBusListener<String> listener = new EventBusListener<String>() {

        @Override
        public void notify(final String event) {
            if (event != null) {
                System.out.println("Status msg:: " + event);
            }
        }
    };

}
