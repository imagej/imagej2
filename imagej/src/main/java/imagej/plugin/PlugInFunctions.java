package imagej.plugin;

import ij.ImagePlus;
import ij.WindowManager;

import ij.gui.GenericDialog;

import java.lang.reflect.Field;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 * @author Grant Harris gharris at mbl.edu
 */

public class PlugInFunctions extends ParameterHandler {

    public static Map<String, Object> run(Runnable plugin, Object... parameters)
            throws PlugInException {
        if ((parameters.length % 2) != 0) {
            throw new IllegalArgumentException("incomplete key/value pair");
        }
        Class clazz = plugin.getClass();
        for (int i = 0; i < parameters.length; i += 2) {
            setParameter(plugin, (String) parameters[i], parameters[i + 1]);
        }
        plugin.run();
        return getOutputMap(plugin);
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
    
    public static void runInteractively(Runnable plugin, String dialogName) {
        if (!showDialog(plugin, dialogName)) {
            return;
        }
        plugin.run();
        for (ImagePlus image : PlugInFunctions.getOutputImages(plugin)) {
            if (image != null) {
                image.show();
            }
        }
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
    /* 
    boolean required() default false;  //++ gbh
     */

    public static Object getDefault(Field field) {
        // TODO
        return "";
    }

    public static boolean showDialog(Runnable plugin) {
        GenericDialog dialog = new GenericDialog("Parameters");
        return showDialog( dialog, plugin);
    }
    

    public static boolean showDialog(Runnable plugin, String dialogName) {
        GenericDialog dialog = new GenericDialog(dialogName);
        return showDialog( dialog, plugin);
    }
    
    private static boolean showDialog(GenericDialog dialog, Runnable plugin)
    {
    	createInputDialogFromParameters(plugin, dialog);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }
        setInputParameterValues(plugin, dialog);
        
        return true;
    }

    private static void createInputDialogFromParameters(Runnable plugin, GenericDialog dialog) throws RuntimeException {
        for (Field field : getInputParameters(plugin)) {
            try {
                if (field.getType() == String.class) {
                    // Add BigDecimal, BigInteger, char, Character using StringField
                    dialog.addStringField(getLabel(field), (String) field.get(plugin), getColumns(field));
                } else if (isNumericType(field.getType())) {
                    createNumericInputWidget(field, dialog, plugin);
                } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    dialog.addCheckbox(getLabel(field), (Boolean) field.get(plugin));
                } else if (field.getType() == ImagePlus.class) {
                    if (WindowManager.getCurrentImage() != null) {
                        ImagePlus ip = WindowManager.getCurrentImage();
                        field.set(plugin, ip);
                    }
                } else {
                    throw new RuntimeException("TODO!");
                }
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(PlugInFunctions.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(PlugInFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void createNumericInputWidget(Field field, GenericDialog dialog, Runnable plugin)
            throws IllegalAccessException, IllegalArgumentException {
        if (getWidget(field).equalsIgnoreCase("slider")) {
            dialog.addSlider(getLabel(field), 0, 100, ((Number) field.get(plugin)).doubleValue());
            if (getWidget(field).equalsIgnoreCase("spinner")) {
                // add create spinner...
            }
        } else {
            dialog.addNumericField(getLabel(field), ((Number) field.get(plugin)).doubleValue(),
                    getDigits(field), getColumns(field), getUnits(field));
        }
    }

    private static boolean isNumericType(Class<?> type) {
        return (type == int.class
                || type == Integer.class
                || type == short.class
                || type == Short.class
                || type == long.class
                || type == Long.class
                || type == float.class
                || type == Float.class
                || type == double.class
                || type == Double.class);
    }

    private static void setInputParameterValues(Runnable plugin, GenericDialog dialog) {
        for (Field field : getInputParameters(plugin)) {
            try {
                if (field.getType() == String.class || field.getType() == char.class || field.getType() == Character.class) {
                    field.set(plugin, dialog.getNextString());
                } else if (field.getType() == int.class || field.getType() == Integer.class) {
                    field.set(plugin, (int) dialog.getNextNumber());
                } else if (field.getType() == short.class || field.getType() == Short.class) {
                    field.set(plugin, (short) dialog.getNextNumber());
                } else if (field.getType() == long.class || field.getType() == Long.class) {
                    field.set(plugin, (long) dialog.getNextNumber());
                } else if (field.getType() == float.class || field.getType() == Float.class) {
                    field.set(plugin, (float) dialog.getNextNumber());
                } else if (field.getType() == double.class || field.getType() == Double.class) {
                    field.set(plugin, (double) dialog.getNextNumber());
                } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    field.set(plugin, (boolean) dialog.getNextBoolean());
                } else if (field.getType() == ImagePlus.class) {
                    //((ImagePlus) field.get(plugin)).show();
                } else if (field.getType().isArray() )
                {
                //TODO:Add arrays?
                }else {
                    System.out.println("skipped  " + field.getName());
                    //throw new RuntimeException("TODO!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

	
}

  