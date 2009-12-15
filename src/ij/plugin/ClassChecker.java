package  ij.plugin;
import ij.*;
import ij.util.*;
import java.io.*;
import java.util.*;

/** Checks for duplicate class files in the plugins directory and deletes older duplicates. */
public class ClassChecker implements PlugIn {

	char separatorChar = Prefs.separator.charAt(0);

	public void run(String arg) {
		deleteDuplicates();
	}
	
	void deleteDuplicates() {
		String[] paths = getClassFiles();
		if (paths==null)
			return;
		String name;
		File file1, file2;
		long date1, date2;
		for (int i=0; i<paths.length; i++) {
			name = getName(paths[i]);
			if (name.endsWith("classx"))
				continue;
			for (int j=i+1; j<paths.length; j++) {
				if (paths[j].endsWith(name)) {
					file1 = new File(paths[i]);
					file2 = new File(paths[j]);
					if (file1==null || file2==null)
						continue;
					date1 = file1.lastModified();
					date2 = file2.lastModified();
					if (date1<date2) {
						write(paths[i]);
						file1.delete();
						break;
					} else if (date2<date1) {
						write(paths[j]);
						paths[j] += "x";
						file2.delete();
					} else {
						if (paths[i].endsWith("plugins"+name)) {
							write(paths[i]);
							file1.delete();
							break;
						} else if (paths[j].endsWith("plugins"+name)) {
							write(paths[j]);
							paths[j] += "x";
							file2.delete();
						}
					}
				}
			}
		}
	}

	void write(String path) {
		IJ.log("Deleting duplicate class: "+path);
	}

	public String getName(String path) {
		int index = path.lastIndexOf(separatorChar);
		return (index < 0) ? path : path.substring(index);
	}

	/** Returns a list of all the class files in the plugins
	     folder and subfolders of the plugins folder. */
	String[] getClassFiles() {
		String path = Menus.getPlugInsPath();
		if (path==null)
			return null;
		File f = new File(path);
		String[] list = f.list();
		if (list==null) return null;
		Vector v = new Vector();
		for (int i=0; i<list.length; i++) {
			String name = list[i];
			boolean isClassFile = name.endsWith(".class");
			if (isClassFile) {
				//className = className.substring(0, className.length()-6); 
				v.addElement(path+name);
			} else {
				if (!isClassFile)
					getSubdirectoryClassFiles(path, name, v);
			}
		}
		list = new String[v.size()];
		v.copyInto((String[])list);
		return list;
	}

	/** Looks for class files in a subfolders of the plugins folder. */
	void getSubdirectoryClassFiles(String path, String dir, Vector v) {
		//IJ.write("getSubdirectoryClassFiles: "+path+dir);
		if (dir.endsWith(".java"))
			return;
		File f = new File(path, dir);
		if (!f.isDirectory())
			return;
		String[] list = f.list();
		if (list==null)
			return;
		dir += Prefs.separator;
		for (int i=0; i<list.length; i++) {
			String name = list[i];
			if (name.endsWith(".class")) {
				//name = name.substring(0, name.length()-6); // remove ".class"
				v.addElement(path+dir+name);
				//IJ.write("File: "+f+"/"+name);
			}
		}
	}

}

