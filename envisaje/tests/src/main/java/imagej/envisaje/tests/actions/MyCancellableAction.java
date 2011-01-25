package imagej.envisaje.tests.actions;

/* From:
 * http://rubenlaguna.com/wp/2010/01/18/cancellable-tasks-and-progress-indicators-netbeans-platform/
 */
import imagej.envisaje.annotations.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;


@Action(position = 1,
displayName = "Cancellable",
// path = "IJPlugins/Analysis",
menuBar = true
)

public final class MyCancellableAction implements ActionListener {

    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private final static Logger LOG = Logger.getLogger(MyCancellableAction.class.getName());
    private RequestProcessor.Task theTask = null;

    public void actionPerformed(ActionEvent e) {
        LOG.info("actionperformed");

        final ProgressHandle ph = ProgressHandleFactory.createHandle("task showing progress", new Cancellable() {
            public boolean cancel() {
                return handleCancel();
            }
        });

        Runnable runnable = new Runnable() {
            private final int NUM = 60000;
            public void run() {
                try {
                    ph.start(); //we must start the PH before we swith to determinate
                    ph.switchToDeterminate(NUM);
                    for (int i = 0; i < NUM; i++) {
                        doSomething(i);
                        ph.progress(i);
                        Thread.sleep(0); //throws InterruptedException is the task was cancelled
                    }
                } catch (InterruptedException ex) {
                    LOG.info("the task was CANCELLED");
                    return;
                }
            }
            private void doSomething(int i) {
                LOG.info("doSomething with " + i);
                return;
            }
        };

        theTask = RP.create(runnable); //the task is not started yet
        theTask.addTaskListener(new TaskListener() {
            public void taskFinished(Task task) {
                ph.finish();
            }
        });
        theTask.schedule(0); //start the task
    }

    private boolean handleCancel() {
        LOG.info("handleCancel");
        if (null == theTask) {
            return false;
        }
        return theTask.cancel();
    }

    public static void main(String[] args) {
     (new MyCancellableAction()).actionPerformed(null);
  }
}
