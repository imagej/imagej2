package org.imagejdev.misccomponents;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import org.openide.util.Utilities;

/**
 * A custom ComboBoxUI to use with ChoiceView to make a combo box
 * that looks like a button with an icon, and shows no text.  The
 * popup works in the usual way, but also shows only icons.  To
 * accomplish this, what we really do is just replace the standard
 * cell renderer with one that wraps the original, extracts the
 * icon and displays it in its own way.
 *
 * @author Timothy Boudreau
 */
public final class IconButtonComboBoxUI extends BasicComboBoxUI implements PropertyChangeListener, FocusListener  {
    /** Creates a new instance of PopupButtonChooser */
    public IconButtonComboBoxUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new IconButtonComboBoxUI();
    }

    protected ListCellRenderer createRenderer() {
        return new Renderer(comboBox.getRenderer());
    }

    protected JButton createArrowButton() {
        return null;
    }

    public void configureArrowButton() {
        //do nothing
    }
    
    protected void installComponents() {
	//do nothing
    }    
    
    protected ComboPopup createPopup() {
	return new Pop(comboBox);
    }
    
    private static final class Pop extends BasicComboPopup {
	public Pop (JComboBox box) {
	    super (box);
	}
	
	protected void configurePopup() {
	    super.configurePopup();
	    setBorderPainted( true );
	    setBorder(BorderFactory.createLineBorder(UIManager.getColor("controlShadow")));
	}
    }

    public void installUI(JComponent c) {
	super.installUI(c);
	c.addPropertyChangeListener("renderer", this); //NOI18N
	comboBox.setRenderer (new Renderer (comboBox.getRenderer()));
	comboBox.setBackground (UIManager.getColor("control")); //NOI18N
	Insets ins = border.getBorderInsets(c);
	c.setBorder (BorderFactory.createEmptyBorder(ins.top, ins.left,
		ins.bottom, ins.right));
	c.addMouseListener (mma);
	c.addFocusListener(this);
    }
    
    public void uninstallUI(JComponent c) {
	super.uninstallUI(c);
	c.removePropertyChangeListener("renderer", this);
	c.removeMouseListener (mma);
	c.removeFocusListener(this);
    }    
    
    private MMA mma = new MMA();
    private class MMA extends MouseAdapter {
	public void mouseExited(MouseEvent e) {
	    containsMouse = false;
	    comboBox.repaint();
	}

	public void mouseEntered(MouseEvent e) {
	    containsMouse = true;
	    comboBox.repaint();
	}
    }
    private boolean containsMouse;
    
    private static Icon downArrow = new ImageIcon (
	    Utilities.loadImage ("org/imagejdev/misccomponents/downarrow.png"));

    private static Icon downArrowLit = new ImageIcon (
	    Utilities.loadImage ("org/imagejdev/misccomponents/downarrowlit.png"));
    
    public void paintCurrentValue(Graphics g, Rectangle bounds, boolean lead) {
	Object o = comboBox.getSelectedItem();
	Icon ic = containsMouse ? downArrowLit : downArrow;
	
	Insets ins = getInsets();
	int w = comboBox.getWidth() - (ins.right + ins.left);
	int h = comboBox.getHeight() - (ins.top + ins.bottom);
	int x = ((w / 2) - (ic.getIconWidth() / 2)) + ins.left;
	int y = ((h / 2) - (ic.getIconHeight() / 2)) + ins.top;
	y -= 4;
	x -= 4;

	if (o != null) {
	    Component c = 
		    comboBox.getRenderer().getListCellRendererComponent(listBox, o, -1,
		    false, false);
	    
	    if (c instanceof JLabel) {  //Theoretically someone could set the renderer
		Icon icon = ((JLabel) c).getIcon();
		if (icon != null) {
		    icon.paintIcon(comboBox, g, x, y);
		}
	    }
	}
	
	int arrx = comboBox.getWidth() - (ins.right + ic.getIconWidth());
	int arry = comboBox.getHeight() - (ins.bottom + ic.getIconHeight());
	
	ic.paintIcon(comboBox, g, arrx, arry);
	if (comboBox.hasFocus()) {
	    g.setColor (UIManager.getColor("controlShadow")); //NOI18N
	    g.drawRect(0, 0, comboBox.getWidth()-1, comboBox.getHeight()-1);
	}
    }

    public Dimension getMinimumSize( JComponent c ) {
	Dimension result = new Dimension (24, 24);
	Object o = comboBox.getSelectedItem();
	if (o != null) {
	    Component comp = 
		    comboBox.getRenderer().getListCellRendererComponent(listBox, o, -1,
		    false, false);
	    
	    if (comp instanceof Renderer) {  //Theoretically someone could set the renderer
		Icon ic = ((Renderer) comp).getIcon();
		if (ic != null) {
		    int sz = Math.max (ic.getIconWidth(), ic.getIconHeight());
		    result.width = Math.max (result.width, sz);
		    result.height = Math.max (result.height, sz);
		}
	    }
	}
	Insets ins = c.getInsets();
	result.width += ins.left + ins.right + 2;
	result.height += ins.top + ins.bottom + 2;
	return result;
    }

    public void propertyChange(PropertyChangeEvent evt) {
	//Whenever the renderer changes, wrap it in our renderer
	if ("renderer".equals(evt.getPropertyName()) && !(evt.getNewValue() instanceof Renderer)) {
	    comboBox.setRenderer (new Renderer ((ListCellRenderer) evt.getNewValue()));
	}
    }

    public void focusGained(FocusEvent e) {
	comboBox.repaint();
    }

    public void focusLost(FocusEvent e) {
	comboBox.repaint();
    }
    
    private static Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
    /**
     * A renderer which wraps the cell renderer set on the combo box,
     * extracts whatever icon it would display and just displays that.
     */
    private static class Renderer extends DefaultListCellRenderer {
	private ListCellRenderer orig;
	
	Renderer (ListCellRenderer orig) {
	    this.orig = orig;
	}
	
	public boolean isOpaque () {
	    return false;
	}
	
	public void setText (String s) {
	    //do nothing
	}
	
	protected void firePropertyChange(String p, Object o, Object n) {
	    //do nothing - performance optimization
	}
	
	public void paint (Graphics g) {
	    Color c = getBackground();
	    g.setColor(c);
	    g.fillRect (0, 0, getWidth(), getHeight());
	    Icon ic = getIcon();
	    if (ic != null) {
		int x = (getWidth() / 2) - (ic.getIconWidth() / 2);
		int y = (getHeight() / 2) - (ic.getIconHeight() / 2);
		ic.paintIcon(this, g, x, y);
	    }
	    getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());
	}
        
        public Component getListCellRendererComponent(JList list, Object value, 
                          int index, boolean isSelected, boolean leadSelection) {
        
            Renderer result = (Renderer) 
                super.getListCellRendererComponent(list, value, 
                index, isSelected, leadSelection);
	    
	    Component other = orig.getListCellRendererComponent(list,
		    value, index, isSelected, leadSelection);
	    
	    if (!isSelected || index==-1) {
		setBackground (UIManager.getColor("control")); //NOI18N
	    } else {
		setBackground (Color.WHITE);
	    }
	    if (other instanceof JLabel) {
		JLabel jl = (JLabel) other;
		setIcon (jl.getIcon());
		setToolTipText(jl.getText());
		if (index != -1) {
		    setBorder (border);
		} else {
		    Insets ins = border.getBorderInsets(jl);
		    setBorder (BorderFactory.createEmptyBorder (ins.top,
			    ins.left, ins.bottom, ins.right));
		}
		return result;
	    }
             return other;
        }
        
        public Dimension getPreferredSize() {
            Dimension result = new Dimension (24, 24);
            Icon ic = getIcon();
            if (ic != null) {
                result.width = Math.max (ic.getIconWidth(), result.width);
                result.height = Math.max (ic.getIconHeight(), result.height);
            }
	    Insets ins = getInsets();
	    result.width += ins.left + ins.right;
	    result.height += ins.top + ins.bottom;
            return result;
        }
	
	public Dimension getMinimumSize() {
	    return getPreferredSize();
	}
	
	public Dimension getMaximumSize() {
	    return getPreferredSize();
	}
    }
}
