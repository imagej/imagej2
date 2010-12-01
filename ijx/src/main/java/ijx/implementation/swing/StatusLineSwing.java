package ijx.implementation.swing;

import ijx.event.EventBus;
import ijx.event.EventBusListener;
import ijx.event.StatusMessage;
import ijx.exec.SwingUtilities;
import ijx.gui.StaticSwingUtils;
import javax.swing.JLabel;

/**
 *  StatusBar that receives and displays StatusMessages using EventBus
 * @author GBH, Nov 2010

 */
public final class StatusLineSwing extends JLabel {

  // Senders do this:    EventBus.getDefault().publish(new StatusMessage("Status Message"));
  //
  public StatusLineSwing() {
    EventBus.getDefault().subscribe(StatusMessage.class, listener);
  }
  private final EventBusListener<StatusMessage> listener = new EventBusListener<StatusMessage>() {

    @Override
    public void notify(final StatusMessage msg) {
      StaticSwingUtils.dispatchToEDT(new Runnable() {
        public void run() {
          if (msg != null) {
            StatusLineSwing.this.setText(msg.getMessage());
          }
        }
      });
    }
  };
}
