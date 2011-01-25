package imagej.envisaje.output;

/**
 *
 * @author GBH
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.ErrorManager;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;

/**
 *
 * @author qbeukes.blogspot.com, used by metalklesk
 */
public class NotifyUtil {

    private NotifyUtil() {
    }

    /**
     * Show message with the specified type and action listener
     */
    public static void show(String title, String message, MessageType type,
            ActionListener actionListener, boolean clear) {
        Notification n = (Notification) NotificationDisplayer.getDefault().notify(
                title, type.getIcon(), message, actionListener);
        if (clear == true) {
            n.clear();
        }
    }

    /**
     * Show message with the specified type and a default action which displays the
     * message using {@link MessageUtil} with the same message type
     */
    public static void show(String title, final String message, final MessageType type, boolean clear) {
        ActionListener actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MessageUtil.show(message, type);
            }
        };

        show(title, message, type, actionListener, clear);
    }

    /**
     * Show an information notification
     * @param message
     */
    public static void info(String title, String message, boolean clear) {
        show(title, message, MessageType.INFO, clear);
    }

    /**
     * Show an error notification
     * @param message
     */
    public static void error(String title, String message, boolean clear) {
        show(title, message, MessageType.ERROR, clear);
    }

    /**
     * Show an error notification for an exception
     * @param message
     * @param exception
     */
    public static void error(String title, final String message, final Throwable exception, boolean clear) {
        ActionListener actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ErrorManager.getDefault().notify(exception);
                //MessageUtil.showException(message, exception);
            }
        };

        show(title, message, MessageType.ERROR, actionListener, clear);
    }

    /**
     * Show an warning notification
     * @param message
     */
    public static void warn(String title, String message, boolean clear) {
        show(title, message, MessageType.WARNING, clear);
    }

    /**
     * Show an plain notification
     * @param message
     */
    public static void plain(String title, String message, boolean clear) {
        show(title, message, MessageType.PLAIN, clear);
    }
}
