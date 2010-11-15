package org.imagejdev.sandbox.lookup.EventBus;

/**
 *
 * @author GBH
 */
public class ReceiverA {

    // the listener is registered like this:
    // subscribe(topic, listener) - topic is a type.

    public ReceiverA() {
        EventBus.getDefault().subscribe(EventA.class, listener);
    }

    private final EventBusListener<EventA> listener = new EventBusListener<EventA>() {

        @Override
        public void notify(final EventA event) {
            if (event != null) {
                // do something
                System.out.println("ReceiverA:: " + event.getMsg());
            }
        }
    };
}
