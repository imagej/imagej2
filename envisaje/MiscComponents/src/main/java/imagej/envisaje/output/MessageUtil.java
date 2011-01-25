package imagej.envisaje.output;

/**
 *
 * @author GBH
 */

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author qbeukes.blogspot.com, used by metalklesk
 */
public class MessageUtil {

    private MessageUtil() {}

    /**
    * @return The dialog displayer used to show message boxes
    */
    public static DialogDisplayer getDialogDisplayer() {
        return DialogDisplayer.getDefault();
    }

    /**
    * Show a message of the specified type
    *
    * @param message
    * @param messageType As in {@link NotifyDescription} message type constants.
    */
    public static void show(String message, MessageType messageType) {
        getDialogDisplayer().notify(new NotifyDescriptor.Message(message,
        messageType.getNotifyDescriptorType()));
    }

    /**
    * Show an exception message dialog
    *
    * @param message
    * @param exception
    */
    public static void showException(String message, Throwable exception) {
        getDialogDisplayer().notify(new NotifyDescriptor.Exception(exception, message));
    }

    /**
    * Show an information dialog
    * @param message
    */
    public static void info(String message) {
        show(message, MessageType.INFO);
    }

    /**
    * Show an error dialog
    * @param message
    */
    public static void error(String message) {
        show(message, MessageType.ERROR);
    }

    /**
    * Show an error dialog for an exception
    * @param message
    * @param exception
    */
    public static void error(String message, Throwable exception) {
        showException(message, exception);
    }

    /**
    * Show an question dialog
    * @param message
    */
    public static void question(String message) {
        show(message, MessageType.QUESTION);
    }

    /**
    * Show an warning dialog
    * @param message
    */
    public static void warn(String message) {
        show(message, MessageType.WARNING);
    }

    /**
    * Show an plain dialog
    * @param message
    */
    public static void plain(String message) {
        show(message, MessageType.PLAIN);
    }
}