import ij.plugin.*;
import ij.*;
import ij.io.*;
//import com.apple.eawt.*;

/**	This Mac specific plugin handles the "About" and "Quit" items in the Apple menu and opens
	 files dropped on ImageJ and files with creator code "imgJ" that are double-clicked. */ 
public class MacAdapter implements PlugIn {
      //, ApplicationListener, 
      //Runnable {
    String path;

	public void run(String arg) {
//		Application app = new Application();
//		app.setEnabledPreferencesMenu(true);
//		app.addApplicationListener(this);
	}
    
/*
 public void handleAbout(ApplicationEvent event) {
		IJ.run("About ImageJ...");
		event.setHandled(true);
	}

	public void handleOpenFile(ApplicationEvent event) {
		path = event.getFilename();
		Thread thread = new Thread(this, "Open");
		thread.setPriority(thread.getPriority()-1);
		thread.start();
	}

	public void handlePreferences(ApplicationEvent event) {
		IJ.error("The ImageJ preferences are in the Edit>Options menu.");
	}

	public void handleQuit(ApplicationEvent event) {
		IJ.getInstance().quit();
	}
  
    public void run() {
        if (path!=null) IJ.open(path);
    }

	public void handleOpenApplication(ApplicationEvent event) {}
	public void handleReOpenApplication(ApplicationEvent event) {}
	public void handlePrintFile(ApplicationEvent event) {}
*/
}
