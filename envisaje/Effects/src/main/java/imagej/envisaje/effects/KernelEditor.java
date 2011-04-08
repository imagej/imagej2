/*
 * KernelEditor.java
 *
 * Created on August 3, 2006, 10:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.effects;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.image.Kernel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Tim Boudreau
 */
public class KernelEditor extends JPanel implements DocumentListener {

    private Dimension kernelSize = new Dimension (2,2);

    /** Creates a new instance of KernelEditor */
    public KernelEditor() {
//        setOpaque(true);
//        setBackground (Color.BLUE);
        setMinimumSize (new Dimension(200, 200));
        setKernelSize (new Dimension (3,3));
    }

    public void setKernelSize (Dimension d) {
        if (!d.equals(kernelSize)) {
            this.kernelSize = new Dimension(d);
            setLayout (new GridLayout (d.width, d.height));
            Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            int refocusIndex = 0;
            if (isAncestorOf(c) && c instanceof JTextField) {
                refocusIndex = ((Integer) ((JTextField)c).getClientProperty(
                        "index")).intValue();
            }
            removeAll();
            int ix = 0;
            boolean focused = false;
            for (int i=0; i < d.height; i++) {
                for (int j=0; j < d.width; j++) {
                    JTextField jtf = new JTextField();
                    jtf.getDocument().addDocumentListener(this);
                    jtf.setText ("1");
                    jtf.setMinimumSize(new Dimension (50, 20));
                    jtf.putClientProperty("index", new Integer(ix));
                    ix++;
                    add (jtf);
                    if (j == refocusIndex) {
                        jtf.requestFocus();
                        focused = true;
                    }
                }
            }
            if (!focused && getComponentCount() > 0) {
                getComponent(0).requestFocus();
            }
            invalidate();
            revalidate();
            repaint();
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension (200, 200);
    }

    private int indexOfDocument (Document d) {
        Component[] c = getComponents();
        for (int i=0; i < c.length; i++) {
            if (((JTextComponent) c[i]).getDocument() == d) {
                Integer in = (Integer) ((JTextComponent) c[i]).getClientProperty("index");
                return in.intValue();
            }
        }
        return -1;
    }

    private void change() {
        Component[] c = getComponents();
        boolean wasValid = valid;
        valid = true;
        for (int i=0; i < c.length; i++) {
            try {
                JTextComponent cc = (JTextComponent) c[i];
                valFrom (cc, true);
            } catch (Exception e) {
                valid = false;
                break;
            }
        }
        if (listener != null && wasValid != valid) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    private boolean valid;
    public boolean isValid() {
        return valid;
    }

    public void setChangeListener (ChangeListener l) {
        this.listener = l;
    }

    private ChangeListener listener;

    public Kernel getKernel() {
        float[] vals = new float [kernelSize.width * kernelSize.height];
        Component[] c = getComponents();
        for (int i=0; i < c.length; i++) {
            int index = ((Integer) ((JTextComponent) c[i]).getClientProperty("index")).intValue();
            vals[index] = valFrom ((JTextComponent) c[i], false);
        }
//        if (kernelSize.width == 3 && kernelSize.height == 3) {
//            float factor = 1F / (vals[5] - 1) - (kernelSize.width * kernelSize.height);
//            System.err.println("factor " + factor);
//            for (int i = 0; i < vals.length; i++) {
//                vals[i] *= factor;
//            }
//        }
        Kernel result = new Kernel (kernelSize.width, kernelSize.height, vals);
        return result;
    }

    private float valFrom (JTextComponent jtc, boolean throwit) {
        try {
            return Float.parseFloat(jtc.getText());
        } catch (NumberFormatException e) {
            if (throwit) {
                throw e;
            }
//            e.printStackTrace();
            return 0;
        }
    }

    public void insertUpdate(DocumentEvent e) {
        change();
    }

    public void removeUpdate(DocumentEvent e) {
        change();
    }

    public void changedUpdate(DocumentEvent e) {
        change();
    }
}
