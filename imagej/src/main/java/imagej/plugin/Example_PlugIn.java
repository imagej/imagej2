package imagej.plugin;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.Duplicator;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/*
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 * @author Grant Harris gharris at mbl.edu
 */

public class Example_PlugIn extends AbstractPlugIn {

    /* the field name will be used for the dialog, so it starts upcased, and underscores are removed. */
    @Parameter
    public String first_name = "grant";
    @Parameter(label = "Your last name", columns = 15)
    public String last_name = "harris";
    @Parameter(label = "An Integer")
    public Integer n = 1;
    @Parameter(label = "An int")
    public int total = 9;
    @Parameter(label = "A Double", widget = "slider")
    public Double x = 50.0;
    @Parameter(label = "A double", digits = 3)
    public double y = 101.2;
    @Parameter
    public boolean yesOrNo = false;
    @Parameter
    public ImagePlus impIn;
    //
    @Parameter(output = true)
    public ImagePlus impOut;
    @Parameter(output = true)
    public int outputValue = 9;

    public void run(String s) {
        //IJ.showMessage("Good morning, " + first_name + "!");
        System.out.println("Good morning, " + first_name + "!");
    }

    @Override
    public void run() {
        //this.setParameter("impIn", IJ.getImage());
        impIn = IJ.getImage();
        if (impIn != null) {
            impOut = duplicateStack(impIn, "New Copy");
//            impOut = impIn.createImagePlus();
//            impOut.updateAndDraw();

            IJ.runPlugIn("ij.plugin.LutLoader", "fire");
        }
        PlugInFunctions.listParamaters(this);
        //============================================================
        System.out.println("Running with Parameters: \n" + first_name + " " + last_name);
        System.out.println("n = " + n);
        System.out.println("total = " + total);
        System.out.println("y = " + y);
        System.out.println("x = " + x);
        System.out.println("yesOrNo = " + yesOrNo);

        outputValue = (int) ((n + total) * x / y);
        System.out.println("End of run() in " + this.getClass().getName() +".\n");
    }

    public ImagePlus duplicateStack(ImagePlus imp, String newTitle) {
        ImagePlus imp2 = (new Duplicator()).run(imp);
        imp2.setTitle(newTitle);
        return imp2;
    }
    //====================================================================================

    public static void main(String[] args) {
        try {  // launch ImageJ and load a test image
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    ImageJ.main(null); // launch ImageJ
                    ImagePlus imp = IJ.openImage("http://rsb.info.nih.gov/ij/images/blobs.gif");
                    imp.show();
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(Example_PlugIn.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(Example_PlugIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        //event dispach thread
        runOffEDT(args);  // interactively
        //runOnEDT(args);
        //runAsFuture(args);
        //runAsFutureOnEdt(args);
    }

    public static void runOffEDT(String[] args) {
        Example_PlugIn abstractPlugin = new Example_PlugIn();
      //  abstractPlugin.setParameter("impIn", IJ.getImage());

        RunnableAdapter rPlugin = new RunnableAdapter(abstractPlugin);
        //rPlugin.run();
        rPlugin.runInteractively();
        PlugInFunctions.getOutputParameters(rPlugin);
        Map<String, Object> outputMap = rPlugin.getOutputMap();
        if (outputMap != null) {
            System.out.println("outputMap: " + outputMap.toString());
        } else {
            System.out.println("outputMap = null");
        }
    }

    public static void runOnEDT(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Example_PlugIn abstractPlugin = new Example_PlugIn();
                PlugInFunctions.listParamaters(abstractPlugin);
                RunnableAdapter rPlugin = new RunnableAdapter(abstractPlugin);
                //rPlugin.run();
                rPlugin.runInteractively();
                PlugInFunctions.getOutputParameters(rPlugin);
            }
        });

    }

    public static void runAsFuture(String[] args) {
        Example_PlugIn abstractPlugin = new Example_PlugIn();
        // set input parameters
        abstractPlugin.setParameter("impIn", IJ.getImage());
        //
        PlugInFunctions.listParamaters(abstractPlugin);
        //RunnableAdapter rPlugin = new RunnableAdapter(abstractPlugin);
        Map<String, Object> outputMap = null;

        FutureTask<Map<String, Object>> task = new FutureTask<Map<String, Object>>(abstractPlugin);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        System.out.println("\nPerforming the Task, (please wait) ... \n");
        try {
            //         answer = task.get();  // run until complete
            outputMap = task.get(5000, TimeUnit.MILLISECONDS); // timeout in 5 seconds
            //task.get(); // timeout in 5 seconds
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("got interrupted.");
        } catch (TimeoutException e) {
            System.out.println("tired of waiting, timed-out.");
        }
        executor.shutdown();
        System.out.println("Done.");
        if (outputMap != null) {
            System.out.println("outputMap: " + outputMap.toString());
        } else {
            System.out.println("outputMap = null");
        }
    }

    private static void runAsFutureOnEdt(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                runAsFuture(args);
            }
        });

    }
}
