package imagej.tool;

import javax.swing.JFrame;

/**
 * TODO
 *
 * @author Rick Lentz
 */
public class MouseWheelEventDemo {

	public static void main(String[] args) {
		createAndShowGUI();
	}

	/** Create the GUI and show it. */
	private static void createAndShowGUI() {
		// create and set up the window
		JFrame frame = new JFrame();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//ProbeTool probeTool = new ProbeTool();
		//probeTool.setJFrame(frame);
		//ToolHandler toolHandler = new ToolHandler(frame, probeTool);

		// display the window
		frame.pack();
		frame.setVisible(true);
	}

}