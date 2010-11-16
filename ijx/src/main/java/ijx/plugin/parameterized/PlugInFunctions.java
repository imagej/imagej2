package ijx.plugin.parameterized;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ijx.IjxImagePlus;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 * @author Grant Harris gharris at mbl.edu
 */
public class PlugInFunctions extends ParameterHandler {
    //
    // execute, passing parameters as a set of varargs pairs
    public static Map<String, Object> execute(Runnable plugin, Object... parameters)
            throws PlugInException {
        if ((parameters.length % 2) != 0) {
            throw new IllegalArgumentException("incomplete key/value pair");
        }
        //Class clazz = plugin.getClass();
        for (int i = 0; i < parameters.length; i += 2) {
            setParameter(plugin, (String) parameters[i], parameters[i + 1]);
        }
        plugin.run();
        return getOutputMap(plugin);
    }

    // execute, passing parameters as an inputMap
    // @todo: Use case, testing...
    public static Map<String, Object> execute(Runnable plugin, Map<String, Object> inputMap)
            throws PlugInException {
        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            setParameter(plugin, key, value);
        }
        plugin.run();
        return getOutputMap(plugin);
    }

    // Run it again with the same parameter values
    public static Map<String, Object> runAgain(Runnable plugin) {
        try {
            return execute(plugin, createInputMapFromParameters(plugin));
        } catch (PlugInException ex) {
            Logger.getLogger(PlugInFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    public static void runInteractively(Runnable plugin) {
        if (!showDialog(plugin)) {
            return;
        }
        plugin.run();
        for (ImagePlus image : PlugInFunctions.getOutputImages(plugin)) {
            if (image != null) {
                image.show();
            }
        }
    }

    public static void testRunAsFuture() {
        Example_PlugIn abstractPlugin = new Example_PlugIn();
        // set input parameters
        abstractPlugin.setParameter("impIn", IJ.getImage());
        //
        PlugInFunctions.listParamaters(abstractPlugin);
    }

    public static Map<String, Object> runAsFuture(Callable plugin) {
        //RunnableAdapter rPlugin = new RunnableAdapter(abstractPlugin);
        Map<String, Object> outputMap = null;
        FutureTask<Map<String, Object>> task = new FutureTask<Map<String, Object>>(plugin);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        System.out.println("\nExecuting Plugin: (please wait) ... \n");
        try {
            outputMap = task.get(5000, TimeUnit.MILLISECONDS); // timeout in 5 seconds
            // outputMap = task.get(); // no timeout
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
        return outputMap;
    }

    public static void listParamaters(Runnable plugin) {
        System.out.println("Parameters Listing: type,  label  =  value");
        System.out.println("------------------------------------------------------");
        for (Field field : getParameters(plugin)) {
            System.out.print("  " + field.getType().getSimpleName());
            System.out.print(",  " + getLabel(field));
            Object value = null;
            try {
                value = field.get(plugin);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(PlugInFunctions.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(PlugInFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (value != null) {
                System.out.print("  = " + value.toString());
            } else {
                System.out.print("  = null");
            }
            System.out.println(".");

        }
    }
    // methods to get the attributes for a field

    public static String getLabel(Field field) {
        Parameter parameter = field.getAnnotation(Parameter.class);
        if (parameter != null) {
            String label = parameter.label();
            if (label != null && !label.equals("")) {
                return label;
            }
        }
        return field.getName();
    }

    public static int getDigits(Field field) {
        Parameter parameter = field.getAnnotation(Parameter.class);
        if (parameter != null) {
            int d = parameter.digits();
            return d;
        }
        return 2;
    }

    public static int getColumns(Field field) {
        Parameter parameter = field.getAnnotation(Parameter.class);
        if (parameter != null) {
            int c = parameter.columns();
            return c;
        }
        return 6;
    }

    public static String getUnits(Field field) {
        Parameter parameter = field.getAnnotation(Parameter.class);
        if (parameter != null) {
            String c = parameter.units();
            return c;
        }
        return "";
    }

    public static boolean getRequired(Field field) {
        Parameter parameter = field.getAnnotation(Parameter.class);
        if (parameter != null) {
            boolean r = parameter.required();
            return r;
        }
        return false;
    }

    // Indicates a key used to get/set value from Prefs
    public static String getPersist(Field field) {
        Parameter parameter = field.getAnnotation(Parameter.class);
        if (parameter != null) {
            String c = parameter.persist();
            return c;
        }
        return "";
    }

    public static String getWidget(Field field) {
        Parameter parameter = field.getAnnotation(Parameter.class);
        if (parameter != null) {
            String widget = parameter.widget();
            if (widget != null && !widget.equals("")) {
                return widget;
            }
        }
        return "";
    }

    public static Object getDefault(Field field) {
        // TODO
        return "";
    }

    public static boolean showDialog(Runnable plugin) {
        // TODO: Should plugin have a getName() method, defaulting to the class name?
        GenericDialog dialog = new GenericDialog("Parameters");
        createInputDialogFromParameters(plugin, dialog);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }
        setParameterValuesToInputs(plugin, dialog);
        return true;
    }

    private static Map<String, Object> createInputMapFromParameters(Runnable plugin) throws RuntimeException {
        createInputDialogFromParameters(plugin, null);
        return getInputMap();
    }

    private static Map<String, Object> getInputMap() {
        return inputMap;
    }

    // inputMap with value from Prefs or default for each parameter
    private static Map<String, Object> inputMap = new HashMap<String, Object>();

    private static void createInputDialogFromParameters(Runnable plugin, GenericDialog dialog) throws RuntimeException {
        for (Field field : getInputParameters(plugin)) {
            try {
                Class<?> type = field.getType();
                Object value;
                if (type == String.class) {
                    String initValue = (String) field.get(plugin);
                    if (!getPersist(field).isEmpty()) {
                        initValue = Prefs.get(getPersist(field), initValue);
                    }
                    // @todo: Add BigDecimal, BigInteger, char, Character using StringField
                    if (dialog != null) {
                        dialog.addStringField(getLabel(field), initValue, getColumns(field));
                    } else {
                        inputMap.put(field.getName(), initValue);
                    }
                } else if (isIntType(type)) {
                    // integer / float ?
                    Number initValue = (Number) field.get(plugin);
                    if (!getPersist(field).isEmpty()) {
                        initValue = Prefs.get(getPersist(field), initValue.doubleValue());
                    }
                    if (dialog != null) {
                        addNumericInputWidget(field, dialog, plugin, initValue);
                    } else {
                        inputMap.put(field.getName(), initValue);
                    }

                } else if (isRealType(type)) {
                    // integer / float ?
                    Number initValue = (Number) field.get(plugin);
                    if (!getPersist(field).isEmpty()) {
                        initValue = Prefs.get(getPersist(field), initValue.doubleValue());
                    }
                    if (dialog != null) {
                        addNumericInputWidget(field, dialog, plugin, initValue);
                    } else {
                        inputMap.put(field.getName(), initValue);
                    }

                } else if (isBooleanType(type)) {
                    boolean initValue = (Boolean) field.get(plugin);
                    if (!getPersist(field).isEmpty()) {
                        initValue = Prefs.getBoolean(getPersist(field), initValue);
                    }
                    if (dialog != null) {
                        dialog.addCheckbox(getLabel(field), initValue);
                    } else {
                        inputMap.put(field.getName(), initValue);
                    }

                } else if (type == ImagePlus.class) {
                    if (WindowManager.getCurrentImage() != null) {
                        IjxImagePlus ip = WindowManager.getCurrentImage();
                        field.set(plugin, ip);
                    }
                } else {
                    throw new RuntimeException("TODO!");
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void addNumericInputWidget(Field field, GenericDialog dialog, Runnable plugin, Number initValue)
            throws IllegalAccessException, IllegalArgumentException {
        if (getWidget(field).equalsIgnoreCase("slider")) {
            dialog.addSlider(getLabel(field), 0, 100, initValue.doubleValue());
            if (getWidget(field).equalsIgnoreCase("spinner")) {
                // add create spinner...
            }
        } else {
            dialog.addNumericField(getLabel(field), initValue.doubleValue(),
                    getDigits(field), getColumns(field), getUnits(field));
        }
    }

    private static void setParameterValuesToInputs(Runnable plugin, GenericDialog dialog) {
        for (Field field : getInputParameters(plugin)) {
            try {
                Class<?> type = field.getType();
                if (type == String.class || type == char.class || type == Character.class) {
                    String s = dialog.getNextString();
                    field.set(plugin, s);
                    if (!getPersist(field).isEmpty()) {
                        Prefs.set(getPersist(field), s);
                    }
                } else if (type == int.class || type == Integer.class) {
                    int n = (int) dialog.getNextNumber();
                    field.set(plugin, n);
                    if (!getPersist(field).isEmpty()) {
                        Prefs.set(getPersist(field), n);
                    }
                } else if (type == short.class || type == Short.class) {
                    short n = (short) dialog.getNextNumber();
                    field.set(plugin, n);
                    if (!getPersist(field).isEmpty()) {
                        Prefs.set(getPersist(field), n);
                    }
                } else if (type == long.class || type == Long.class) {
                    long n = (long) dialog.getNextNumber();
                    field.set(plugin, n);
                    if (!getPersist(field).isEmpty()) {
                        Prefs.set(getPersist(field), n);
                    }
                } else if (type == float.class || type == Float.class) {
                    float n = (float) dialog.getNextNumber();
                    field.set(plugin, n);
                    if (!getPersist(field).isEmpty()) {
                        Prefs.set(getPersist(field), n);
                    }
                } else if (type == double.class || type == Double.class) {
                    double n = (double) dialog.getNextNumber();
                    field.set(plugin, n);
                    if (!getPersist(field).isEmpty()) {
                        Prefs.set(getPersist(field), n);
                    }

                } else if (isBooleanType(type)) {
                    boolean b = dialog.getNextBoolean();
                    field.set(plugin, b);
                    if (!getPersist(field).isEmpty()) {
                        Prefs.set(getPersist(field), b);
                    }
                } else if (type == ImagePlus.class) {
                    // @todo
                    //((IjxImagePlus) field.get(plugin)).show();
                } else {
                    System.out.println("skipped  " + field.getName());
                    //throw new RuntimeException("TODO!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isNumericType(Class<?> type) {
        return (isIntType(type) || isRealType(type));
    }

    private static boolean isIntType(Class<?> type) {
        return (type == int.class
                || type == Integer.class
                || type == short.class
                || type == Short.class
                || type == long.class
                || type == Long.class);
    }

    private static boolean isRealType(Class<?> type) {
        return (type == float.class
                || type == Float.class
                || type == double.class
                || type == Double.class);
    }

    private static boolean isBooleanType(Class<?> type) {
        return (type == boolean.class || type == Boolean.class);
    }
}
