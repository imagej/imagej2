package ijx.gui.dialog;
import ijx.WindowManager;
import ijx.IJ;

import ijx.gui.GUI;
import ijx.gui.MultiLineLabel;
import ijx.gui.IjxWindow;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;


/**
* This is a non-modal dialog box used to ask the user to perform some task
* while a macro or plugin is running. It implements the waitForUser() macro
* function. It is based on Michael Schmid's Wait_For_User plugin.
*/
public class WaitForUserDialog extends Dialog implements ActionListener, KeyListener {
	protected Button button;
	protected MultiLineLabel label;
	static protected int xloc=-1, yloc=-1;
	private boolean escPressed;
	
	public WaitForUserDialog(String title, String text) {
		super((Frame)getFrame(), title, false);
		label = new MultiLineLabel(text, 175);
		if (!IJ.isLinux()) label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        GridBagLayout gridbag = new GridBagLayout(); //set up the layout
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        c.insets = new Insets(6, 6, 0, 6); 
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        add(label,c); 
		button = new Button("  OK  ");
		button.addActionListener(this);
		button.addKeyListener(this);
        c.insets = new Insets(2, 6, 6, 6); 
        c.gridx = 0; c.gridy = 2; c.anchor = GridBagConstraints.EAST;
        add(button, c);
		setResizable(false);
		addKeyListener(this);
		pack();
		if (xloc==-1)
			GUI.center(this);
		else
			setLocation(xloc, yloc);
		if (IJ.isJava15()) try {
			// Call setAlwaysOnTop() using reflection so this class can be compiled with Java 1.4
			Class windowClass = Class.forName("java.awt.Window");
			Method setAlwaysOnTop = windowClass.getDeclaredMethod("setAlwaysOnTop", new Class[] {Boolean.TYPE});
			Object[] arglist = new Object[1]; arglist[0]=new Boolean(true);
			setAlwaysOnTop.invoke(this, arglist);
		} catch (Exception e) { }
	}
	
	public WaitForUserDialog(String text) {
		this("Action Required", text);
	}

	public void show() {
		super.show();
		IJ.beep();
		synchronized(this) {  //wait for OK
			try {wait();}
			catch(InterruptedException e) {return;}
		}
	}
	
	static IjxWindow getFrame() {
		IjxWindow win = WindowManager.getCurrentWindow();
		if (win==null) win = IJ.getTopComponent();
		return win;
	}

    public void close() {
        synchronized(this) { notify(); }
        xloc = getLocation().x;
        yloc = getLocation().y;
		setVisible(false);
		dispose();
    }

	public void actionPerformed(ActionEvent e) {
		close();
	}
	
	public void keyPressed(KeyEvent e) { 
		int keyCode = e.getKeyCode(); 
		IJ.setKeyDown(keyCode); 
		if (keyCode==KeyEvent.VK_ENTER || keyCode==KeyEvent.VK_ESCAPE) {
			escPressed = keyCode==KeyEvent.VK_ESCAPE;
			close();
		}
	}
	
	public boolean escPressed() {
		return escPressed;
	}
	
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode(); 
		IJ.setKeyUp(keyCode); 
	}
	
	public void keyTyped(KeyEvent e) {}

}
