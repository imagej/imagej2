/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx.exec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class SwingWorkerDemoUsesAll
    extends JFrame
    implements ActionListener {

    private final JButton startButton,  stopButton;
    JScrollPane scrollPane = new JScrollPane();
    private JList listBox = null;
    private DefaultListModel listModel = new DefaultListModel();
    private final JProgressBar progressBar;
    private mySwingWorker swingWorker;

    public SwingWorkerDemoUsesAll() {
        super("SwingWorkerExample");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridLayout(2, 2));
        startButton = makeButton("Start");
        stopButton = makeButton("Stop");
        stopButton.setEnabled(false);
        progressBar = makeProgressBar(0, 99);
        listBox = new JList(listModel);
        scrollPane.setViewportView(listBox);
        getContentPane().add(scrollPane);

        //Display the window.
        pack();
        setVisible(true);
    }
    // Class SwingWorker<T,V>
    // T - the result type returned by this SwingWorker's  doInBackgroundand get methods
    // V - the type used for carrying out intermediate results by thisSwingWorker's publish and process methods
    private class mySwingWorker
        extends javax.swing.SwingWorker<ArrayList<Integer>, Integer> {
        // The first template argument, in this case, ArrayList<Integer>, iswhat
        // is returned by doInBackground(), and by get().
        //
        // The second template argument, in this case, Integer, is what
        // is published with the publish method. It is also the data
        // type which is stored by the java.util.List that is the parameter
        // for the process method, which recieves the information published
        // by the publish method.
        protected ArrayList<Integer> doInBackground() {
            // Returns items of the type given as the first template
            // argument to the SwingWorker class.

            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                System.out.println(
                    "javax.swing.SwingUtilities.isEventDispatchThread()returned true.");
            }

            Integer tmpValue = new Integer(1);
            ArrayList<Integer> list = new ArrayList<Integer>();

            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) { // find every 100th prime, just tomake it slower
                    tmpValue = FindNextPrime(tmpValue.intValue());
                    // isCancelled() returns true if the cancel() method
                    // is invoked on this class. That is the proper way
                    // to stop this thread. See the actionPerformed
                    // method.
                    if (isCancelled()) {
                        System.out.println("isCancelled");
                        return list;
                    }
                }
                // Successive calls to publish are coalesced into ajava.util.List,
                // which is what is received by process, which in this case, is used
                // to update the JProgressBar. Thus, the values passed to publish
                // range from 1 to 100.
                publish(new Integer(i));
                list.add(tmpValue);
            }
            return list;
        }
        // Note, always use java.util.List here, or it will use the wrong list.
        @Override
        protected void process(java.util.List<Integer> progressList) {
            // This method is processing a java.util.List of items
            // given as successive arguments to the publish method.
            // Note that these calls are coalesced into a java.util.List.
            // This list holds items of the type given as the
            // second template parameter type to SwingWorker.
            // Note that the get method below has nothing to do
            // with the SwingWorker get method; it is the List's
            // get method.
            // This would be a good place to update a progress bar.
            if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
                System.out.println(
                    "javax.swing.SwingUtilities.isEventDispatchThread()returned false.");
            }

            Integer percentComplete =
                progressList.get(progressList.size() - 1);
            progressBar.setValue(percentComplete.intValue());
        }

        @Override
        protected void done() {
            System.out.println("doInBackground is complete");
            if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
                System.out.println(
                    "javax.swing.SwingUtilities.isEventDispatchThread()returned false.");
            }

            try {
                // Here, the SwingWorker's get method returns an item
                // of the same type as specified as the first type parameter
                // given to the SwingWorker class.
                ArrayList<Integer> results = get();
                for (Integer i : results) {
                    listModel.addElement(i.toString());
                }

            //repaint();
            } catch (Exception e) {
                System.out.println("Caught an exception: " + e);
            }
        }

        boolean IsPrime(int num) // Checks whether a number is prime
        {
            int i;
            for (i = 2; i <= num / 2; i++) {
                if (num % i == 0) {
                    return false;
                }
            }
            return true;
        }

        protected Integer FindNextPrime(int num) // Returns next prime number from passed arg.
        {
            do {
                if (num % 2 == 0) {
                    num++;
                } else {
                    num += 2;
                }
            } while (!IsPrime(num));
            return new Integer(num);
        }

    }

    private JButton makeButton(String caption) {
        JButton b = new JButton(caption);
        b.setActionCommand(caption);
        b.addActionListener(this);
        getContentPane().add(b);
        return b;
    }

    private JProgressBar makeProgressBar(int min, int max) {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setMinimum(min);
        progressBar.setMaximum(max);
        progressBar.setStringPainted(true);
        progressBar.setBorderPainted(true);
        getContentPane().add(progressBar);
        return progressBar;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("Start")) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            // Note that it creates a new instance of the SwingWorker-derived
            // class. Never reuse an old one.
            (swingWorker = new mySwingWorker()).execute(); // newinstance
        } else if (e.getActionCommand().equalsIgnoreCase("Stop")) {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            swingWorker.cancel(true); // causes isCancelled to return true in doInBackground
            swingWorker = null;
        }
    }

    public static void main(String[] args) {
        // Notice that it kicks it off on the event-dispatching
        // thread, not the main thread.
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new SwingWorkerDemoUsesAll();
            }

        });
    }

}
