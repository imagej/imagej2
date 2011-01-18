package imagej.gui;

import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

public class StatusBar extends JLabel {

	public StatusBar() {
		setBorder(new BevelBorder(BevelBorder.LOWERED));
	}

}
