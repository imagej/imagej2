package implementation.awt;

import ijx.event.EventBus;
import ijx.event.EventBusListener;
import ijx.event.StatusMessage;
import java.awt.Label;

/**
 *  StatusBar that recieves and displays StatusMessages
 * @author GBH
 */
public final class StatusLineAWT extends Label {

    // Senders do this:    EventBus.getDefault().publish(new StatusMessage("Status Message"));

    public StatusLineAWT() {
        EventBus.getDefault().subscribe(StatusMessage.class, listener);
    }
    private final EventBusListener<StatusMessage> listener = new EventBusListener<StatusMessage>() {
        @Override
        public void notify(final StatusMessage msg) {
            if (msg != null) {
                StatusLineAWT.this.setText(msg.getMessage());
            }
        }
    };
}
