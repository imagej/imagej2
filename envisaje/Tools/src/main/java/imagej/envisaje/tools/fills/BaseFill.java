/*
 * SolidColorFill.java
 *
 * Created on October 15, 2005, 7:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.tools.fills;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import net.java.dev.colorchooser.ColorChooser;
import imagej.envisaje.api.util.ChangeListenerSupport;
import imagej.envisaje.tools.spi.Fill;
import imagej.envisaje.misccomponents.explorer.Customizable;
import org.openide.util.NbBundle;/**
 * A base class to share a little logic between SolidColorFill and
 * GradientFill without creating the possibility of either being 
 * subclassed in another module (this type of inheritance abuse is
 * *only* legal where the two classes are going to be compiled together - 
 * we shouldn't have external code relying on, for example, what layout
 * manager we'return using or things will blow up badly for them if
 * we make changes here).
 *
 * @author Timothy Boudreau
 */
class BaseFill implements Customizable, Fill, ActionListener {
    private ColorChooser cc = null;
    
     BaseFill() {
    }

    public Paint getPaint() {
        return cc == null ? Color.BLUE : cc.getColor();
    }

    private JComponent customizer;
    public JComponent getCustomizer() {
        if (customizer == null) {
            customizer = createCustomizer();
        }
        return customizer;
    }
    
    private JComponent createCustomizer() {
        cc = new ColorChooser();
        Dimension d = new Dimension (16, 16);
        cc.setMinimumSize(d);
        cc.setPreferredSize(d);
        JLabel lbl = new JLabel (getChooserCaption()); //NOI18N
        lbl.setLabelFor (cc);
//        lbl.setBorder (BorderFactory.createEmptyBorder (5, 5, 5, 5));
        cc.addActionListener (this);
	JPanel result = new JPanel();
	result.setLayout (new FlowLayout());
	result.add (lbl);
	result.add (cc);
	return result;
    }
    
    protected String getChooserCaption() {
        return NbBundle.getMessage (BaseFill.class, 
                "LBL_Foreground"); //NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        //Happens when the user changes the color.  We fire a change event,
        //which our containing SelectAndCustomizePanel will refire, and
        //eventually it will cause the brush preview to repaint.
        fire();
    }

    private ChangeListenerSupport changes = new ChangeListenerSupport(this);
    public void addChangeListener(ChangeListener cl) {
	changes.add(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
	changes.remove(cl);
    }
    
    private void fire() {
	changes.fire();
    }
}
