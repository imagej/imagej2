package controller;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import workspace.Workspace;



public class Main {

	/** The single instance of the Workspace Controller **/
	protected static Workspace workspace;

	protected JPanel workspacePanel;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Create a new WorkspaceController
				// WorkspaceController wc = new WorkspaceController();
				JPanel workspacePanel;
				workspacePanel = new JPanel();
				workspacePanel.setLayout(new BorderLayout());

				System.out.println("Creating GUI...");

				// Create and set up the window.
				JFrame frame = new JFrame("ImageJ Work Flow Plugin");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				frame.setBounds(100, 100, 800, 500);

				// create a javascript handler
				ScriptEngineManager mgr = new ScriptEngineManager();
				ScriptEngine engine = mgr.getEngineByName("JavaScript");

				// Now we have a script engine instance that
				// can execute some JavaScript
				try {
					String javaScript = "";
					java.net.URL jsURL = Main.class.getResource("web/pipe.edit.js");
					engine.eval(jsURL.openStream().toString());
					//engine.eval("print('Hello, world!')");
				} catch (ScriptException ex) {
					ex.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JPanel topPane = new JPanel();
				frame.add(topPane, BorderLayout.PAGE_START);
				// frame.add(wc.getWorkspacePanel(), BorderLayout.CENTER);

				frame.setVisible(true);
				
				
			}
		});

	}

}
