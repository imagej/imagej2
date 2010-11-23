import ijx.plugin.api.PlugIn;
import ijx.io.Opener;
import ijx.IJ;
import ijx.Executer;
import ij.plugin.*;
import com.apple.eawt.*;
import java.util.Vector;

/**	This Mac specific plugin handles the "About" and "Quit" items in the Apple menu and opens
	 files dropped on ImageJ and files with creator code "imgJ" that are double-clicked. */ 
public class MacAdapter implements PlugIn, ApplicationListener, Runnable {
	static Vector paths = new Vector();

	public void run(String arg) {
		Application app = new Application();
		app.setEnabledPreferencesMenu(true);
		app.addApplicationListener(this);
	}
    
	public void handleAbout(ApplicationEvent event) {
		IJ.run("About " + IJ.getInstance().getTitle() + "...");
		event.setHandled(true);
	}

	public void handleOpenFile(ApplicationEvent event) {
		paths.add(event.getFilename());
		Thread thread = new Thread(this, "Open");
		thread.setPriority(thread.getPriority()-1);
		thread.start();
	}

	public void handlePreferences(ApplicationEvent event) {
		IJ.error("The ImageJ preferences are in the Edit>Options menu.");
	}

	public void handleQuit(ApplicationEvent event) {
		new Executer("Quit").run(); // works with the CommandListener
		//IJ.getInstance().quit();
	}
  
    public void run() {
        if (paths.size() > 0) {
			(new Opener()).openAndAddToRecent((String) paths.remove(0));
		}
    }

	public void handleOpenApplication(ApplicationEvent event) {}
	public void handleReOpenApplication(ApplicationEvent event) {}
	public void handlePrintFile(ApplicationEvent event) {}

}
