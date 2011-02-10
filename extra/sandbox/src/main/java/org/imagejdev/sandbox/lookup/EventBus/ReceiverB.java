package org.imagejdev.sandbox.lookup.EventBus;

/**
 *
 * @author GBH
 */
public class ReceiverB {

    // the listener is registered like this:
    // subscribe(topic, listener) - topic is a type.

    public ReceiverB() {
        EventBus.getDefault().subscribe(EventB.class, listener);
    }

    private final EventBusListener<EventB> listener = new EventBusListener<EventB>() {

        @Override
        public void notify(final EventB event) {
            if (event != null) {
                // do something
                System.out.println("ReceiverB:: " + event.getMsg());
            }
        }
    };
}
