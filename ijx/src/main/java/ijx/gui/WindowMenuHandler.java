package ijx.gui;

import ijx.event.ApplicationEvent;
import ijx.event.EventBus;
import ijx.event.EventBusListener;
import javax.swing.JMenu;

/**
 * from JHotDraw
 * @author GBH <imagejdev.org>
 */
/** Updates the menu items in the "Window" menu. */
public class WindowMenuHandler { //implements PropertyChangeListener {
        private JMenu windowMenu;
        public WindowMenuHandler(JMenu windowMenu) {
            this.windowMenu = windowMenu;
            //MDIApplication.this.addPropertyChangeListener(this);
        EventBus.getDefault().subscribe(ApplicationEvent.class, listener);
       updateWindowMenu();
        }


        protected void updateWindowMenu() {
            JMenu m = windowMenu;
            m.removeAll();
//            m.add(getAction(view, ArrangeWindowsAction.CASCADE_ID));
//            m.add(getAction(view, ArrangeWindowsAction.VERTICAL_ID));
//            m.add(getAction(view, ArrangeWindowsAction.HORIZONTAL_ID));
//
//            m.addSeparator();
//
//            for (Iterator i = windows().iterator(); i.hasNext();) {
//                View pr = (View) i.next();
//                if (getAction(pr, FocusWindowAction.ID) != null) {
//                    m.add(getAction(pr, FocusWindowAction.ID));
//                }
//            }
//            if (toolBarActions.size() > 0) {
//                m.addSeparator();
//                for (Action a : toolBarActions) {
//                    JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(a);
//                    ActionUtil.configureJCheckBoxMenuItem(cbmi, a);
//                    m.add(cbmi);
//                }
//            }
        }
    private final EventBusListener<ApplicationEvent> listener = new EventBusListener<ApplicationEvent>() {
        @Override
        public void notify(final ApplicationEvent msg) {
            if (msg != null) {
                if (msg.getType() == ApplicationEvent.Type.WINDOW_ADDED) {
                    //setText(msg.getMessage());
                } else if (msg.getType() == ApplicationEvent.Type.WINDOW_REMOVED) {
                    //
                }
            }
        }
    };
}
