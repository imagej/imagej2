package imagej.envisaje.output;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author GBH
 */
public class TestOutput {

    public static void main(String[] args) {
        NotifyUtil.warn("title", "warning message", false);
        NotifyUtil.show("title", "question message", MessageType.QUESTION, true);
        NotifyUtil.info("title", "info message", false);

        NotifyUtil.show("title", "info message", MessageType.INFO, new ActionListenerImpl(), false);
        NotifyUtil.error("title", "error message", false);
        Exception exception = new Exception("Blah");
        NotifyUtil.error("title", "error message", exception, false);
        MessageUtil.showException("message", exception);
        MessageUtil.error("message", exception);
        MessageUtil.error("message");
        MessageUtil.info("message");
        MessageUtil.question("message");
        MessageUtil.plain("message");
    }

    private static class ActionListenerImpl implements ActionListener {

        public ActionListenerImpl() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //do something
        }
    }
}
