package ijx.event;

import java.util.HashMap;
import java.util.Map;

/**
 * Use: e.g. EventBus.getDefault().publish(new ApplicationEvent(AppEventType.STARTING));
 *
 * @author GBH <imagejdev.org>
 */
public class ApplicationEvent {
    private ApplicationEvent.Type type;

    public ApplicationEvent(ApplicationEvent.Type type) {
        this.type = type;
    }

    public ApplicationEvent.Type getType() {
        return type;
    }

    /**
     * AppEventType: Application level events
     * @author GBH <imagejdev.org>
     */
    public enum Type {
        STARTING,
        STARTED,
        ERROR,
        QUITING,
        EXITED,
        MODULE_ADDED,
        FILE_OPENED,
        WINDOW_ADDED,
        WINDOW_REMOVED;

        @Override
        public String toString() {
            //only capitalize the first letter
            String s = super.toString();
            return s.substring(0, 1) + s.substring(1).toLowerCase();
        }
    }
}
