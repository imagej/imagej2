package imagej.tool;


/*
* MouseWheelEventDemo.java
*/

import javax.swing.*;

public class MouseWheelEventDemo {


    public static void main(String[] args) {
      
       
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {

       
        //Create and set up the window.
        JFrame frame = new JFrame();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ProbeTool probeTool = new ProbeTool();
        probeTool.setJFrame( frame );
        ToolHandler toolHandler = new ToolHandler( frame, probeTool );
       
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

}