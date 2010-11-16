package ij.plugin;

import ijx.IjxMenus;
import java.io.*;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.plugin.*;
import ijx.CentralLookup;

public class PlugInInstaller implements PlugIn {
	public void run(String arg) {

		String pluginsPath = Menus.getPlugInsPath();
		if(pluginsPath == null) {
			String homeDir = Prefs.getHomeDir();
			if(homeDir == null) {
				IJ.error("Need a home!");
				return;
			}
			pluginsPath = homeDir+"/plugins";
			if(!(new File(pluginsPath).mkdir())) {
				IJ.error("Could not create plugins folder ("
					+ pluginsPath +")");
				return;
			}
		}

		OpenDialog od = new OpenDialog("Install PlugIn...", null);
		String dir = od.getDirectory();
		String file = od.getFileName();

		if(file == null) {
			// We can't use it if there is no filename
			return;
		}

		if(!(new File(pluginsPath).equals(new File(dir))))
			if(!filecopy(dir+"/"+file,pluginsPath+"/"+file)) {
				IJ.error("Error copying "+file+" to "+pluginsPath);
				return;
			}

		if(file.endsWith(".java")) {
			if(!Compiler.compileFile(pluginsPath+"/"+file)) {
				IJ.error("Could not compile "+file);
				return;
			}
		}

		// insert into menu

        IjxMenus m = CentralLookup.getDefault().lookup(IjxMenus.class);
		m.updateImageJMenus();
		IJ.showMessage("PluginInstaller","Plugin "+file+" was installed!");
	}

	static boolean filecopy(String from, String to) {
		FileInputStream in = null;
		FileOutputStream out = null;

		try {
			in  = new FileInputStream( from );
			out = new FileOutputStream( to );

			byte buffer[] = new byte[1024];
			for(int count=0;;) {
				count = in.read(buffer);
				if (count<0)
					break; 
				out.write(buffer, 0, count);
			}
		}
		catch( IOException e ) {
			System.err.println(e.toString());
			return false;
		}
		finally {
			if (in != null) {
				try { in.close(); }
				catch( IOException e ) { }
			}
			if (out!= null) {
				try { out.close(); }
				catch( IOException e ) { }
			}
		}
		return true;
	}
}

