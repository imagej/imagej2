
package ijx.event;

/**
 * StatusMessage for passing messages to the status bar using the EventBus
 * @author GBH <imagejdev.org>
 */
public class StatusMessage {
    private String message;

    public StatusMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }


}
