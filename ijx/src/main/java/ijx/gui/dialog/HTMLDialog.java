package ijx.gui.dialog;
import ijx.gui.GUI;
import ijx.IJ;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** This is modal dialog box that displays HTML formated text. */
public class HTMLDialog extends JDialog implements ActionListener {
	
	public HTMLDialog(String title, String message) {
		super(IJ.getTopComponentFrame(), title, true);
		ijx.util.Java2.setSystemLookAndFeel();
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		if (message==null) message = "";
		JLabel label = new JLabel(message);
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
		panel.add(label);
		container.add(panel, "Center");
		JButton button = new JButton("OK");
		button.addActionListener(this);
		panel = new JPanel();
		panel.add(button);
		container.add(panel, "South");
		setForeground(Color.black);
		pack();
		GUI.center(this);
		show();
	}
	
	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		dispose();
	}
}
