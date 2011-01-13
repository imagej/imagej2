package org.imagejdev.imagine.api.toolcustomizers;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SliderUI;
import org.imagejdev.imagine.api.toolcustomizers.CustomizerFactory;
import org.imagejdev.imagine.spi.tools.Customizer;
import org.imagejdev.imagine.toolcustomizers.BooleanCustomizer;
import org.imagejdev.imagine.toolcustomizers.ColorCustomizer;
import org.imagejdev.imagine.toolcustomizers.FontCustomizer;
import org.imagejdev.imagine.toolcustomizers.TextCustomizer;
import org.imagejdev.misccomponents.LDPLayout;
import org.imagejdev.misccomponents.PopupSliderUI;
import org.imagejdev.misccomponents.SharedLayoutPanel;
import org.imagejdev.misccomponents.SimpleSliderUI.StringConverter;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * General handler for getting customizers for specific types.  Basic supported
 * types are Boolean, String, Font, all Number types.  Enums are supported
 * in a generic way by providing a customizer with a combobox containing all
 * of the enum instances (FontStyles is an example;  any such enum type should
 * override <code>toString()</code> to return a localized name.
 * <p/>
 * <b>General usage:</b>
 * <br/>
 * Getting a customizer for a number type:
 * <pre>
 * Customizer&lt;Float&gt; strokeCustomizer = Customizers.getCustomizer(Float.class, Constants.STROKE, 1F, 24F);
 * </pre>
 * Getting a customizer for a boolean:
 * <pre>
 * Customizer&lt;Boolean&gt; fillCustoizer = Customizers.getCustomizer(Boolean.class, Constants.FILL);
 * </pre>
 * For types other than the ones mentioned above, modules can register instances of
 * CustomizerFactory in the default lookup.
 *
 * @author Tim Boudreau
 */
public final class Customizers {

    private Customizers() {}

    private static final Map<Class, Map<String, Customizer>> map = new HashMap<Class, Map<String, Customizer>>();

    /**
     * Get a customizer for a number type.  The type may be anything that extends java.lang.Number, e.g.
     * Integer, Long, Short, Byte, Float, Double
     * @param type The number type
     * @param name The name of the customizer - this is used both for title (i.e. it should be a localized string) and
     *             to look up the last known value for this name/type combination in NbPreferences.
     * @param start The beginning of the range of numbers this customizer should allow as values
     * @param end The end of the range of values this customizer should allow as values
     * @return A customizer that can supply a component for editing the value of this name+type combination.
     */
    public synchronized static <T extends Number> Customizer<T> getCustomizer(Class<T> type, String name, T start, T end) {
        // @todo Does this need to be forced onto EDT ?
        // assert EventQueue.isDispatchThread();
        assert type == Integer.class || type == Short.class || type == Long.class ||
                type == Byte.class || type == Double.class || type == Float.class;
        if (start.doubleValue() > end.doubleValue()) {
            T hold = start;
            start = end;
            end = hold;
        }
        Map<String, Customizer> m = map.get(type);
        Customizer<T> result = null;
        if (m != null) {
            result = m.get(name);
        } else {
            m = new HashMap<String, Customizer>();
            map.put(type, m);
        }
        if (result == null) {
            result = new NumberCustomizer(type, name, start, end);
            m.put(name, result);
        } else {
            NumberCustomizer<T> c = (NumberCustomizer<T>) result;
            c.setRange(start, end);
        }
        return result;
    }

    /**
     * Get a customizer for a particular type.
     *
     * @param type The type of object the customizer customizes
     * @param name The name of the customizer.  This should be a <i>localized</i> name that
     *             can be shown to the user in a window titlebar or similar.
     * @return A customizer, if the type is known, or if there is a CustomizerFactory in
     *         the default lookup that can create customizers for this type.
     */
    public synchronized static <T> Customizer<T> getCustomizer (Class<T> type, String name) {
        if (Number.class.isAssignableFrom(type)) {
            if (type == Float.class) {
                return (Customizer<T>) getCustomizer (Float.class, name, Float.MIN_VALUE, Float.MAX_VALUE);
            } else if (type == Double.class) {
                return (Customizer<T>) getCustomizer (Double.class, name, Double.MIN_VALUE, Double.MAX_VALUE);
            } else if (type == Integer.class) {
                return (Customizer<T>) getCustomizer (Integer.class, name, Integer.MIN_VALUE, Integer.MAX_VALUE);
            } else if (type == Long.class) {
                return (Customizer<T>) getCustomizer (Long.class, name, Long.MIN_VALUE, Long.MAX_VALUE);
            } else if (type == Short.class) {
                return (Customizer<T>) getCustomizer (Short.class, name, Short.MIN_VALUE, Short.MAX_VALUE);
            } else if (type == Byte.class) {
                return (Customizer<T>) getCustomizer (Byte.class, name, Byte.MIN_VALUE, Byte.MAX_VALUE);
            }
        }
        Map<String, Customizer> m = map.get(type);
        boolean created = false;
        if (m == null) {
            m = new HashMap<String, Customizer>();
            created = true;
        }
        Customizer<T> result = m.get(name);
        if (result == null) {
            result = createCustomizer (type, name);
            if (result != null) {
                m.put (name, result);
            }
        }
        if (created) {
            map.put(type, m);
        }
        return result;
    }

    private static <T> Customizer<T> createCustomizer (Class<T> type, String name) {
        Customizer<T> result = null;
        if (Enum.class.isAssignableFrom(type)) {
            result = new EnumCustomizer (type, name);
        } else if (type == String.class) {
            result = (Customizer<T>) new TextCustomizer(name);
        } else if (type == Boolean.class) {
            result = (Customizer<T>) new BooleanCustomizer(name);
        } else if (type == Font.class) {
            result = (Customizer<T>) new FontCustomizer (name);
        } else if (type == Color.class) {
            result = (Customizer<T>) new ColorCustomizer (name);
        } else {
            CustomizerFactory fac = findFactory (type);
            if (fac != null) {
                result = fac.getCustomizer(type, name, new Object[0]);
            }
        }
        return result;
    }

    private static CustomizerFactory findFactory (Class type) {
        Collection<? extends CustomizerFactory> facs = Lookup.getDefault().lookupAll(CustomizerFactory.class);
        for (CustomizerFactory f : facs) {
            if (f.supportsType(type)) {
                return f;
            }
        }
        return null;
    }

    //Package private for unit tests
    static final class NumberCustomizer<T extends Number> implements Customizer<T>, StringConverter {

        private final Class<T> clazz;
        protected T start;
        protected T end;
        private final JSlider slider = new JSlider();
        private final JLabel lbl;

        NumberCustomizer(Class<T> type, final String name, T start, T end) {
            this.clazz = type;
            setRange(start, end);
            lbl = new JLabel(name);
            slider.setUI((SliderUI) PopupSliderUI.createUI(slider));
            slider.putClientProperty("converter", this);
            setRange (start, end);
            Preferences p = NbPreferences.forModule(NumberCustomizer.class);
            String prefsKey = name + "." + clazz.getSimpleName();
            if (type == Float.class) {
                Float f = p.getFloat(prefsKey, start.floatValue());
                set ((T)f);
            } else if (type == Double.class) {
                Double d = p.getDouble(prefsKey, start.doubleValue());
                set ((T)d);
            } else if (type == Integer.class) {
                Integer i = p.getInt(prefsKey, start.intValue());
                set ((T)i);
            } else if (type == Long.class) {
                Long l = p.getLong(prefsKey, start.longValue());
                set ((T)l);
            } else if (type == Short.class) {
                Short s = (short) p.getInt(prefsKey, start.intValue());
                set ((T)s);
            } else if (type == Byte.class) {
                byte[] b = p.getByteArray(prefsKey, new byte[] { start.byteValue() });
                set ((T)new Byte(b[0]));
            } else {
                throw new AssertionError();
            }

            slider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    Class type = NumberCustomizer.this.clazz;
                    String prefsKey = name + "." + clazz.getSimpleName();
                    Preferences p = NbPreferences.forModule(NumberCustomizer.class);
                    if (type == Float.class) {
                        Float f = (Float) value();
                        p.putFloat(prefsKey, f);
                    } else if (type == Double.class) {
                        Double d = (Double) value();
                        p.putDouble(prefsKey, d);
                    } else if (type == Integer.class) {
                        Integer i = (Integer) value();
                        p.putInt(prefsKey, i);
                    } else if (type == Long.class) {
                        Long l = (Long) value();
                        p.putLong(prefsKey, l);
                    } else if (type == Short.class) {
                        Short s = (Short) value();
                        p.putInt(prefsKey, s.intValue());
                    } else if (type == Byte.class) {
                        Byte b = (Byte) value();
                        p.putByteArray(prefsKey, new byte[] { b });
                    } else {
                        throw new AssertionError();
                    }
                }
            });
        }

        void set (T value) {
            if (clazz != Float.class && clazz != Double.class) {
                slider.setValue(value.intValue());
            } else {
                double max = end.doubleValue();
                double min = start.doubleValue();
                double val = value.doubleValue();

                int sMax = slider.getMaximum();
                int sMin = slider.getMinimum();

                double relPos = (val - min);
                double range = max - min;

                double factor = range / relPos;

                double sRange = sMax - sMin;
                double sVal = sMin + (sRange * factor);
                slider.setValue ((int) Math.max(Math.min(sMax, sVal), sMin));
            }
        }

        //For unit tests
        JSlider slider() {
            return slider;
        }

        void setRange(T start, T end) {
            this.start = start;
            this.end = end;
            if (Integer.class == clazz) {
                slider.setMinimum(start.intValue());
                slider.setMaximum(end.intValue());
            } else {
                slider.setMinimum(0);
                slider.setMaximum(1000);
            }
        }

        public String getName() {
            return lbl.getText();
        }

        public JComponent[] getComponents() {
            JPanel result = new SharedLayoutPanel();
            result.add(lbl);
            result.add(slider);
            return new JComponent[] { result };
        }

        public T valueOf(int val) {
            double v = val;
            double range = end.doubleValue() - start.doubleValue();
            Double result = new Double(start.doubleValue() + (range / (((double) slider.getMaximum() - (double)slider.getMinimum())) * val));
            if (clazz == Float.class) {
                return (T) new Float(result.floatValue());
            } else if (clazz == Double.class) {
                return (T) new Double(result.doubleValue());
            } else if (clazz == Integer.class) {
                return (T) new Integer(result.intValue());
            } else if (clazz == Long.class) {
                return (T) new Long(result.longValue());
            } else if (clazz == Short.class) {
                return (T) new Short(result.shortValue());
            } else if (clazz == Byte.class) {
                return (T) new Byte(result.byteValue());
            } else {
                throw new AssertionError();
            }
        }

        public T value() {
            return valueOf(slider.getValue());
        }

        public T get() {
            if (clazz == Float.class) {
                return (T) new Float(value().floatValue());
            } else if (clazz == Double.class) {
                return (T) new Double(value().doubleValue());
            } else if (clazz == Integer.class) {
                return (T) new Integer(value().intValue());
            } else if (clazz == Long.class) {
                return (T) new Long(value().longValue());
            } else if (clazz == Short.class) {
                return (T) new Short(value().shortValue());
            } else if (clazz == Byte.class) {
                return (T) new Byte(value().byteValue());
            } else {
                throw new AssertionError();
            }
        }

        public String valueToString(JSlider sl) {
            String result = "" + value();
            if (result.length() > 3) {
                int ix = result.indexOf ('.');
                if (ix >= 0) {
                    result = result.substring(0, Math.min(result.length() - 1,
                            ix + 3));
                }
            }
            return result;
        }

        public int maxChars() {
            if (clazz == Float.class || clazz == Double.class) {
                Long beg = start.longValue();
                Long stp = end.longValue();
                return Math.max (beg.toString().length(), stp.toString().length()) + 3;
            } else {
                return Math.max (start.toString().length(), end.toString().length());
            }
        }

        public JComponent getComponent() {
            return getComponents()[0];
        }
    }

    private static final class EnumCustomizer<T extends Enum> implements Customizer<T>, ActionListener {
        private final Class<T> type;
        private final String name;
        private final ComboBoxModel mdl;
        EnumCustomizer (Class<T> type, String name) {
            this.type = type;
            this.name = name;
            mdl = new DefaultComboBoxModel(type.getEnumConstants());
        }

        public JComponent getComponent() {
            JPanel pnl = new JPanel(new LDPLayout());
            pnl.add (new JLabel(name));
            JComboBox box = new JComboBox();
            box.setModel (mdl);
            pnl.add (box);
            String selectionName = getPreferences().get(getKey(), null);
            if (selectionName != null) {
                for (T t : type.getEnumConstants()) {
                    if (selectionName.equals(t.name())) {
                        box.setSelectedItem(t);
                        break;
                    }
                }
            }
            box.addActionListener(this);
            return pnl;
        }

        public String getName() {
            return name;
        }

        public T get() {
            return (T) mdl.getSelectedItem();
        }

        public void actionPerformed(ActionEvent e) {
            JComboBox box = (JComboBox) e.getSource();
            T t = (T) box.getSelectedItem();
            String selectionName = t.name();
            getPreferences().put(getKey(), selectionName);
        }

        private String getKey() {
            String key = name + "." + type.getName();
            return key;
        }

        private Preferences getPreferences() {
            Preferences prefs = NbPreferences.forModule(EnumCustomizer.class);
            return prefs;
        }
    }
}
