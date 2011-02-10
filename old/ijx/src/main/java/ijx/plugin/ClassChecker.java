package  ijx.plugin;
import ijx.plugin.api.PlugIn;
import imagej.util.StringSorter;
import ijx.Menus;
import ijx.Prefs;
import ijx.IJ;
import java.io.*;
import java.util.*;

/** Checks for duplicate class and JAR files in the plugins folders and deletes older duplicates. */
public class ClassChecker implements PlugIn {
	String[] paths;
	String[] names;

	public void run(String arg) {
		//long start = System.currentTimeMillis();
		deleteDuplicates();
		//IJ.log("Time: "+(System.currentTimeMillis()-start));
	}
	
	void deleteDuplicates() {
		getPathsAndNames();
		if (paths==null || paths.length<2) return;
		String[] sortedNames = new String[names.length];
		for (int i=0; i<names.length; i++)
			sortedNames[i] = names[i];
		StringSorter.sort(sortedNames);
		for (int i=0; i<sortedNames.length-1; i++) {
			if (sortedNames[i].equals(sortedNames[i+1]))
				delete(sortedNames[i]);
		}
	}
	
	void delete(String name) {
		String path1=null, path2=null;
		File file1, file2;
		long date1, date2;
		for (int i=0; i<names.length; i++) {
			if (path1==null && names[i].equals(name)) {
				path1 = paths[i] +names[i];
//IJ.log("path1: "+i+"   "+name+"   "+path1+"   "+paths[i]+"   "+names[i]);
			} else if (path2==null && names[i].equals(name)) {
				path2 = paths[i] +names[i];
//IJ.log("path2: "+i+"   "+name+"   "+path2+"   "+paths[i]+"   "+names[i]);
			}
			if (path1!=null && path2!=null) {
				file1 = new File(path1);
				file2 = new File(path2);
				if (file1==null || file2==null) return;
				date1 = file1.lastModified();
				date2 = file2.lastModified();
				if (date1<date2) {
					write(path1);
					file1.delete();
				} else {
					write(path2);
					file2.delete();
				}
				break;
			}
		}
	}

	void write(String path) {
		IJ.log("Deleting duplicate plugin: "+path);
	}

	/** Gets lists of all the class and jar files in the plugins
	     folder and subfolders of the plugins folder. */
	void getPathsAndNames() {
		String path = Menus.getPlugInsPath();
		if (path==null) return;
		File f = new File(path);
		String[] list = f.list();
		if (list==null) return;
		Vector v1 = new Vector(1000);
		Vector v2 = new Vector(1000);
		for (int i=0; i<list.length; i++) {
			String name = list[i];
			if (name.endsWith(".class") || name.endsWith(".jar")) {
				v1.addElement(path);
				v2.addElement(name);
			} else
				getSubdirectoryFiles(path, name, v1, v2);
		}
		paths = new String[v1.size()];
		v1.copyInto((String[])paths);
		names = new String[v2.size()];
		v2.copyInto((String[])names);
	}

	/** Looks for class and jar files in a subfolders of the plugins folder. */
	void getSubdirectoryFiles(String path, String dir, Vector v1, Vector v2) {
		//IJ.write("getSubdirectoryClassFiles: "+path+dir);
		if (dir.endsWith(".java")) return;
		File f = new File(path, dir);
		if (!f.isDirectory()) return;
		String[] list = f.list();
		if (list==null) return;
		dir += Prefs.separator;
		for (int i=0; i<list.length; i++) {
			String name = list[i];
			if (name.endsWith(".class") || name.endsWith(".jar")) {
				v1.addElement(path+dir);
				v2.addElement(name);
			}
		}
	}

}

