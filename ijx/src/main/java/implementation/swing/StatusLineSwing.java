package implementation.swing;

import ijx.event.EventBus;
import ijx.event.EventBusListener;
import ijx.event.StatusMessage;
import javax.swing.JLabel;

/**
 *  StatusBar that recieves and displays StatusMessages
 * @author GBH
 */
public final class StatusLineSwing extends JLabel {

    // Senders do this:    EventBus.getDefault().publish(new StatusMessage("Status Message"));

    public StatusLineSwing() {
        EventBus.getDefault().subscribe(StatusMessage.class, listener);
    }
    private final EventBusListener<StatusMessage> listener = new EventBusListener<StatusMessage>() {
        @Override
        public void notify(final StatusMessage msg) {
            if (msg != null) {
                StatusLineSwing.this.setText(msg.getMessage());
            }
        }
    };
}
