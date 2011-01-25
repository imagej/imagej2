package imagej.envisaje.output;

/**
 * from http://qbeukes.blogspot.com/2009/11/netbeans-platform-notifications.html
 * Quintin Beukes
 * Apache License 2.0.
 * 
 */

import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.NotifyDescriptor;

/**
 *
 * @author qbeukes.blogspot.com, used by metalklesk
 */
public enum MessageType {
    PLAIN   (NotifyDescriptor.PLAIN_MESSAGE,       null),
    INFO    (NotifyDescriptor.INFORMATION_MESSAGE, "info.png"),
    QUESTION(NotifyDescriptor.QUESTION_MESSAGE,    "question.png"),
    ERROR   (NotifyDescriptor.ERROR_MESSAGE,       "error.png"),
    WARNING (NotifyDescriptor.WARNING_MESSAGE,     "warning.png");

    private int notifyDescriptorType;

    private Icon icon;

    private MessageType(int notifyDescriptorType, String resourceName) {
        this.notifyDescriptorType = notifyDescriptorType;
        if (resourceName == null) {
            icon = new ImageIcon();
        } else {
            icon = loadIcon(resourceName);
        }
    }

    private static Icon loadIcon(String resourceName) {
        URL resource = MessageType.class.getResource("images/" + resourceName);
        System.out.println(resource);
        if (resource == null) {
            return new ImageIcon();
        }

        return new ImageIcon(resource);
    }

    int getNotifyDescriptorType() {
        return notifyDescriptorType;
    }

    Icon getIcon() {
        return icon;
    }
}