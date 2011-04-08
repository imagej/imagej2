/*
 * ConvolutionBlur.java
 *
 * Created on August 3, 2006, 10:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.effects;

import java.awt.FlowLayout;
import java.awt.image.Kernel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import imagej.envisaje.spi.effects.Effect;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */

@ServiceProvider(service=Effect.class)


public class ConvolutionBlur extends ConvolutionEffect {

    /** Creates a new instance of ConvolutionBlur */
    public ConvolutionBlur() {
        super ("Convolution Blur");
    }

    protected Kernel createDefaultKernel() {
        float[] data = new float[] {
            1,1,1,
            1,1,1,
            1,1,1,
        };
        Kernel result = new Kernel (3, 3, data);
        return result;
    }

    public void dispose() {
//        ke = null;
    }

    protected Kernel createKernel(int width, int height) {
        if (ke == null) {
            return createDefaultKernel();
        } else {
            return ke.getKernel();
        }
    }

    private KernelEditor ke;
    public final JPanel getCustomizer() {
        ke = new KernelEditor();
        final DimensionEditor de = new DimensionEditor();
        final ChangeListener l = new ChangeListener() {
            public void stateChanged (ChangeEvent ce) {
                if (ce.getSource() == de) {
                    ke.setKernelSize(de.getDimension());
                    fire();
                } else {
                    fire();
                }
            }
        };
        ke.setChangeListener(l);
        de.setChangeListener(l);
        JPanel result = new JPanel();
        result.setLayout (new FlowLayout ());
        result.setBorder (BorderFactory.createEtchedBorder());
        result.add (ke);
        result.add (de);
        result.add (new JLabel ("Matrix Size"));
        return result;
    }

    public boolean canApply() {
        return ke != null && ke.isValid();
    }
}
