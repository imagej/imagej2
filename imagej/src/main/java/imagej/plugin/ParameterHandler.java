package ijx.plugin.parameterized;

import ij.ImagePlus;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 * @author Grant Harris gharris at mbl.edu
 */
public class ParameterHandler {

    public ParameterHandler() {
    }

    public static Iterable<Field> getInputParameters(Runnable plugin) {
        return getParameters(plugin, PlugInFunctions.inputs);
    }

    public static void setParameter(Runnable plugin, String key, Object value) {
        try {
            Class clazz = plugin.getClass();
            Field field = clazz.getField(key);
            Parameter annotation = field.getAnnotation(Parameter.class);
            if (annotation == null) {
                throw new IllegalArgumentException("field \'" + key + "\' is not a plugin parameter");
            }
            if (annotation.output()) {
                throw new IllegalArgumentException("field \'" + key + "\' is an output field");
            }
            field.set(plugin, value);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Invalid key: " + key);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Field is not public: " + key);
        }
    }

    public static Iterable<Field> getOutputParameters(Runnable plugin) {
        return getParameters(plugin, PlugInFunctions.outputs);
    }

    public static Iterable<Field> getParameters(Runnable plugin, ParameterFilter filter) {
        return new ParameterIterable(plugin, filter);
    }

    public static Iterable<Field> getParameters(Runnable plugin) {
        return getParameters(plugin, PlugInFunctions.all);
    }

    public static Iterable<ImagePlus> getOutputImages(Runnable plugin) {
        List<ImagePlus> result = new ArrayList<ImagePlus>();
        for (Field field : getOutputParameters(plugin)) {
            if (field.getType() == ImagePlus.class) {
                try {
                    result.add((ImagePlus) field.get(plugin));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static Map<String, Object> getOutputMap(Runnable plugin) {
        // @todo: Mightn't this oughta be ConcurrentHashMap ?
        Map<String, Object> result = new HashMap<String, Object>();
        for (Field field : getOutputParameters(plugin)) {
            try {
                result.put(field.getName(), field.get(plugin));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // Parameter Filters -----------------------------------------------------------------
    public interface ParameterFilter {

        public boolean matches(Parameter parameter);
    }

    protected final static ParameterFilter all = new ParameterFilter() {

        public boolean matches(Parameter parameter) {
            return true;
        }
    };

    protected final static ParameterFilter inputs = new ParameterFilter() {

        public boolean matches(Parameter parameter) {
            return !parameter.output();
        }
    };

    protected final static ParameterFilter outputs = new ParameterFilter() {

        public boolean matches(Parameter parameter) {
            return parameter.output();
        }
    };

    protected static class ParameterIterable implements Iterable<Field> {

        Field[] fields;
        ParameterFilter filter;

        ParameterIterable(Field[] fields, ParameterFilter filter) {
            this.fields = fields;
            this.filter = filter;
        }

        ParameterIterable(Runnable plugin, ParameterFilter filter) {
            this(plugin.getClass().getFields(), filter);
        }

        public Iterator<Field> iterator() {
            return new ParameterIterator(fields, filter);
        }
    }

    protected static class ParameterIterator implements Iterator<Field> {

        int counter;
        Field[] fields;
        ParameterFilter filter;

        ParameterIterator(Field[] fields, ParameterFilter filter) {
            this.fields = fields;
            this.filter = filter;
            counter = -1;
            findNext();
        }

        void findNext() {
            while (++counter < fields.length) {
                Parameter parameter = fields[counter].getAnnotation(Parameter.class);
                if (parameter == null) {
                    continue;
                }
                if (filter.matches(parameter)) {
                    return;
                }
            }
        }

        public boolean hasNext() {
            return counter < fields.length;
        }

        public Field next() {
            Field result = fields[counter];
            findNext();
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
