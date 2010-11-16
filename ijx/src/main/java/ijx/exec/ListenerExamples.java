package ijx.exec;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

/**
 * ListenerExamples
 *
 * @author Brian Goetz and Tim Peierls
 */
public class ListenerExamples {
    public ListenerExamples() {
        longRunningTaskWithFeedback();
//        this.backgroundRandom();
//        this.longRunningTask();
        //taskWithCancellation();

    }
    private static ExecutorService exec = Executors.newCachedThreadPool();
    private final JButton colorButton = new JButton("Change color");
    private final Random random = new Random();

    private void backgroundRandom() {
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colorButton.setBackground(new Color(random.nextInt()));
            }
        });
    }
    private final JButton computeButton = new JButton("Big computation");

    private void longRunningTask() {
        computeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exec.execute(new Runnable() {
                    public void run() {
                        /* Do big computation */
                        for (long i = 0; i < 999999999; i++) {
                        }
                    }
                });
            }
        });
    }
    private final JButton button = new JButton("Do");
    private final JLabel label = new JLabel("idle");

    private void longRunningTaskWithFeedback() {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button.setEnabled(false);
                label.setText("busy");
                exec.execute(new Runnable() {
                    public void run() {
                        try {
                            /* Do big computation */
                            for (long i = 0; i < 999999999; i++) {
                            }
                        } finally {
                            GuiExecutor.instance().execute(new Runnable() {
                                public void run() {
                                    button.setEnabled(true);
                                    label.setText("idle");
                                }
                            });
                        }
                    }
                });
            }
        });
    }
    private final JButton startButton = new JButton("Start");
    private final JButton cancelButton = new JButton("Cancel");
    private Future<?> runningTask = null; // thread-confined

    private void taskWithCancellation() {
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (runningTask == null) {
                    runningTask = exec.submit(new Runnable() {
                        int toDo = 9999999;

                        public void run() {
                            System.out.println("Running task 999999");
                            while (moreWork()) {
                                if (Thread.currentThread().isInterrupted()) {
                                    cleanUpPartialWork();
                                    break;
                                }
                                doSomeWork();
                            }
                        }

                        private boolean moreWork() {
                            toDo--;
                            return toDo > 0;
                        }

                        private void cleanUpPartialWork() {
                            GuiExecutor.instance().execute(new Runnable() {
                                public void run() {
                                    System.out.println("Cancelled.");
                                }
                            });

                        }

                        private void doSomeWork() {
                            System.out.println("DoinSomeWork");
                            for (long i = 0; i < 999999999; i++) {
                            }
                        }
                    });
                }
                ;
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (runningTask != null) {
                    runningTask.cancel(true);
                }
            }
        });
    }

    private void runInBackground(final Runnable task) {
        startButton.addActionListener(new ActionListener() {
            // setup for cancellation
            public void actionPerformed(ActionEvent e) {
                class CancelListener implements ActionListener {
                    BackgroundTask<?> task;

                    public void actionPerformed(ActionEvent event) {
                        if (task != null) {
                            task.cancel(true);
                        }
                    }
                }
                final CancelListener listener = new CancelListener();


                listener.task = new BackgroundTask<Void>() {
                    public Void runTask() {
                        while (moreWork() && !isCancelled()) {
                            doSomeWork();
                        }
                        return null;
                    }

                    private boolean moreWork() {
                        return false;
                    }

                    private void doSomeWork() {
                        for (long i = 0; i < 999999999; i++) {
                        }
                    }

                    public void onCompletion(boolean cancelled, String s, Throwable exception) {
                        cancelButton.removeActionListener(listener);
                        label.setText("done");
                    }
                };
                cancelButton.addActionListener(listener);
                exec.execute(task);
            }
        });
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setLayout(new FlowLayout());
        ListenerExamples le = new ListenerExamples();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(le.button);
        f.add(le.label);
        f.add(le.startButton);
        f.add(le.cancelButton);
        f.add(le.colorButton);
        f.add(le.computeButton);
        System.out.println(le.startButton.getActionListeners());
        f.pack();
        f.setVisible(true);
    }
}
