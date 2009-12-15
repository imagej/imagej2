package ij.plugin;
import ij.IJ;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.applet.Applet;

/**
 * This plugin implements the File/Import/URL command and the commands in the Help menu that 
 * open web pages. It is based on Eric Albert's cross-platform (Windows, Mac OS X and Unix) 
 * BrowserLauncher class.
 * <p>
 * BrowserLauncher is a class that provides one static method, openURL, which opens the default
 * web browser for the current user of the system to the given URL.  It may support other
 * protocols depending on the system -- mailto, ftp, etc. -- but that has not been rigorously
 * tested and is not guaranteed to work.
 * <p>
 * Yes, this is platform-specific code, and yes, it may rely on classes on certain platforms
 * that are not part of the standard JDK.  What we're trying to do, though, is to take something
 * that's frequently desirable but inherently platform-specific -- opening a default browser --
 * and allow programmers (you, for example) to do so without worrying about dropping into native
 * code or doing anything else similarly evil.
 * <p>
 * Anyway, this code is completely in Java and will run on all JDK 1.1-compliant systems without
 * modification or a need for additional libraries.  All classes that are required on certain
 * platforms to allow this to run are dynamically loaded at runtime via reflection and, if not
 * found, will not cause this to do anything other than returning an error when opening the
 * browser.
 * <p>
 * This code is Copyright 1999-2001 by Eric Albert (ejalbert@cs.stanford.edu) and may be
 * redistributed or modified in any form without restrictions as long as the portion of this
 * comment from this paragraph through the end of the comment is not removed.  The author
 * requests that he be notified of any application, applet, or other binary that makes use of
 * this code, but that's more out of curiosity than anything and is not required.  This software
 * includes no warranty.  The author is not repsonsible for any loss of data or functionality
 * or any adverse or unexpected effects of using this software.
 * <p>
 * Credits:
 * <br>Steven Spencer, JavaWorld magazine (<a href="http://www.javaworld.com/javaworld/javatips/jw-javatip66.html">Java Tip 66</a>)
 * <br>Thanks also to Ron B. Yeh, Eric Shapiro, Ben Engber, Paul Teitlebaum, Andrea Cantatore,
 * Larry Barowski, Trevor Bedzek, Frank Miedrich, and Ron Rabakukk
 *
 * @author Eric Albert (<a href="mailto:ejalbert@cs.stanford.edu">ejalbert@cs.stanford.edu</a>)
 * @version 1.4b1 (Released June 20, 2001)
 */
public class BrowserLauncher implements PlugIn {
	/** The com.apple.mrj.MRJFileUtils class */
	private static Class mrjFileUtilsClass;
	/** The openURL method of com.apple.mrj.MRJFileUtils */
	private static Method openURL;
	private static boolean error;
	static {loadClasses();}


	/** Opens the specified URL (default is the ImageJ home page). */
	public void run(String theURL) {
		if (error) return;
		if (theURL==null || theURL.equals(""))
			theURL = IJ.URL;
		Applet applet = IJ.getApplet();
		if (applet!=null) {
			try {
				applet.getAppletContext().showDocument(new URL(theURL), "_blank" );
			} catch (Exception e) {}
			return;
		}
		try {openURL(theURL);}
		catch (IOException e) {}
	}

	/**
	 * Attempts to open the default web browser to the given URL.
	 * @param url The URL to open
	 * @throws IOException If the web browser could not be located or does not run
	 */
	public static void openURL(String url) throws IOException {
		String errorMessage = "";
		if (IJ.isMacOSX()) {
			if (IJ.is64Bit())
				IJ.runMacro("exec('open', '"+url+"')");
			else {
				try {
					Method aMethod = mrjFileUtilsClass.getDeclaredMethod("sharedWorkspace", new Class[] {});
					Object aTarget = aMethod.invoke( mrjFileUtilsClass, new Object[] {});
					openURL.invoke(aTarget, new Object[] { new java.net.URL( url )}); 
				} catch (Exception e) {
					errorMessage = ""+e;
				}
			}
		} else if (IJ.isWindows()) {
			String cmd = "rundll32 url.dll,FileProtocolHandler " + url;
			if (System.getProperty("os.name").startsWith("Windows 2000"))
				cmd = "rundll32 shell32.dll,ShellExec_RunDLL " + url;
			Process process = Runtime.getRuntime().exec(cmd);
			// This avoids a memory leak on some versions of Java on Windows.
			// That's hinted at in <http://developer.java.sun.com/developer/qow/archive/68/>.
			try {
				process.waitFor();
				process.exitValue();
			} catch (InterruptedException ie) {
				throw new IOException("InterruptedException while launching browser: " + ie.getMessage());
			}
		} else {
				// Assume Linux or Unix
				// Based on BareBonesBrowserLaunch (http://www.centerkey.com/java/browser/)
				String[] browsers = {"netscape", "firefox", "konqueror", "mozilla", "opera", "epiphany", "lynx" };
				String browserName = null;
				try {
					for (int count=0; count<browsers.length && browserName==null; count++) {
						String[] c = new String[] {"which", browsers[count]};
						if (Runtime.getRuntime().exec(c).waitFor()==0)
							browserName = browsers[count];
					}
					if (browserName==null)
						ij.IJ.error("BrowserLauncher", "Could not find a browser");
					else
						Runtime.getRuntime().exec(new String[] {browserName, url});
				} catch (Exception e) {
					throw new IOException("Exception while launching browser: " + e.getMessage());
				}
		}
	}
	
	/**
	 * Called by a static initializer to load any classes, fields, and methods 
	 * required at runtime to locate the user's web browser.
	 */
	private static void loadClasses() {
		if (IJ.isMacOSX() && !IJ.is64Bit() && IJ.getApplet()==null) {
			try {
				if (new File("/System/Library/Java/com/apple/cocoa/application/NSWorkspace.class").exists()) {
					ClassLoader classLoader = new URLClassLoader(new URL[]{new File("/System/Library/Java").toURL()});
					mrjFileUtilsClass = Class.forName("com.apple.cocoa.application.NSWorkspace", true, classLoader);
				} else
					mrjFileUtilsClass = Class.forName("com.apple.cocoa.application.NSWorkspace");
				openURL = mrjFileUtilsClass.getDeclaredMethod("openURL", new Class[] { java.net.URL.class });
			} catch (Exception e) {
				IJ.log("BrowserLauncher"+e);
				error = true;
			}
		}
	}

}

