package ij.io;
import ijx.IjxMenus;
import ij.*;
import ij.gui.*;
import ijx.CentralLookup;
import java.io.*;
import java.net.URL;
import java.net.*;

/** Intalls plugins (.jar and .class files) that have been dragged
	and dropped on the ImageJ window, or opened using 
	File/Open or the open() macro function. */
class PluginInstaller {

	boolean install(String path) {
		boolean isURL = path.startsWith("http://") || path.startsWith("https://");
		byte[] data = null;
		String name = path;
		if (isURL) {
			URL url = null;
			try {
				url = new URL(path);
			} catch (Exception e) {
				IJ.error(""+e);
				return false;
			}
			int index = path.lastIndexOf("/");
			if (index!=-1 && index<=path.length()-1)
					name = path.substring(index+1);
			data = download(url);
		} else {
			File f = new File(path);
			name = f.getName();
			data = download(f);
		}
		if (data==null) return false;
        IjxMenus menus = CentralLookup.getDefault().lookup(IjxMenus.class);
		SaveDialog sd = new SaveDialog("Save Plugin...", menus.getPlugInsPath(), name, null);
		String name2 = sd.getFileName();
		if (name2==null) return false;
		String dir = sd.getDirectory();
		if (!savePlugin(new File(dir,name), data))
			return false;
		menus.updateImageJMenus();
		return true;
	}
	
	boolean savePlugin(File f, byte[] data) {
		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(data, 0, data.length);
			out.close();
		} catch (IOException e) {
			IJ.error("Plugin Installer", ""+e);
			return false;
		}
		return true;
	}

	byte[] download(URL url) {
		byte[] data;
		try {
			URLConnection uc = url.openConnection();
			int len = uc.getContentLength();
			IJ.showStatus("Downloading "+url.getFile());
			InputStream in = uc.getInputStream();
			data = new byte[len];
			int n = 0;
			while (n < len) {
				int count = in.read(data, n, len - n);
				if (count<0)
					throw new EOFException();
				n += count;
				IJ.showProgress(n, len);
			}
			in.close();
		} catch (IOException e) {
			return null;
		}
		return data;
	}
	
	byte[] download(File f) {
		if (!f.exists()) {
			IJ.error("Plugin Installer", "File not found: "+f);
			return null;
		}
		byte[] data = null;
		try {
			int len = (int)f.length();
			InputStream in = new BufferedInputStream(new FileInputStream(f));
			DataInputStream dis = new DataInputStream(in);
			data = new byte[len];
			dis.readFully(data);
			dis.close();
		}
		catch (Exception e) {
			IJ.error("Plugin Installer", ""+e);
			data = null;
		}
		return data;
	}

}
