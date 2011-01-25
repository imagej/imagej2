package imagej.envisaje.tests;

import imagej.envisaje.annotations.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author GBH
 */

@Action(position = 1,
displayName = "TryOutput",
// path = "File",
menuBar = true
)
public class TryOutput implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        imagej.envisaje.output.TestOutput.main(null);
    }
}

