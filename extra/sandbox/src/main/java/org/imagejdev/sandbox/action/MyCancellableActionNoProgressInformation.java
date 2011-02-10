package org.imagejdev.sandbox.action;

/* From:
 * http://rubenlaguna.com/wp/2010/01/18/cancellable-tasks-and-progress-indicators-netbeans-platform/
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;

public final class MyCancellableActionNoProgressInformation implements ActionListener {

    //The RequestProcessor has to have  allowInterrupt set to true!!
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private final static Logger LOG = Logger.getLogger(MyCancellableActionNoProgressInformation.class.getName());

    public void actionPerformed(ActionEvent e) {

        Runnable runnable = new Runnable() {

            private final int NUM = 60000;

            public void run() {
                for (int i = 0; i < NUM; i++) {
                    doSomething(i);
                    if (Thread.interrupted()) {
                        LOG.info("the task was CANCELLED");
                        return;
                    }
                }

            }

            private void doSomething(int i) {
                LOG.info("doSomething with " + i);
                return;
            }
        };

        final RequestProcessor.Task theTask = RP.create(runnable);

        final ProgressHandle ph = ProgressHandleFactory.createHandle("performing some task", theTask);
        theTask.addTaskListener(new TaskListener() {
            public void taskFinished(org.openide.util.Task task) {
                //make sure that we get rid of the ProgressHandle
                //when the task is finished
                ph.finish();
            }
        });

        //start the progresshandle the progress UI will show 500s after
        ph.start();

        //this actually start the task
        theTask.schedule(0);
    }

    public static void main(String[] args) {
     (new MyCancellableActionNoProgressInformation()).actionPerformed(null);
  }
}