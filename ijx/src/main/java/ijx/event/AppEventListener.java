
package ijx.event;

/**
 *  Recieves and displays ApplicationEvents
 * @author GBH
 */
public final class AppEventListener  {

    public AppEventListener() {
        EventBus.getDefault().subscribe(ApplicationEvent.class, listener);
    }
    private final EventBusListener<ApplicationEvent> listener = new EventBusListener<ApplicationEvent>() {
        @Override
        public void notify(final ApplicationEvent msg) {
            if (msg != null) {
                System.out.println(msg.getType());
            }
        }
    };
}
