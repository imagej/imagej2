package org.imagejdev.sandbox.lookup;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * Plugin manager for standalone swing apps
Thursday, July 15, 2010 8:52 AM
 *
 * http://ragha-infomaniac.blogspot.com/2009/06/plugin-manager-for-standalone-swing.html
 *
Lookup API provides a more features than the typical ServiceLoader mechanism introduced in JDK 6.
 * It allows you to listen to changes using LookupListener. If your not aware of Lookup API,
 * please read about it here before continuing any further. For a more comprehensive tutorial
 * on the subject, check out this screencast

Lookup API provides amazing decoupling capabilities to your application, even in standalone
 * and non visual applications. The problem comes when you have to install or remove modules
 * from your applications (implementations of an interface). In Netbeans platform the plugin
 * manager would automatically do this for you. To utilize the benefits of LookupListener, one
 * has to add the module jar files to the classpath (at run time).

This however cannot be achieved directly, here's a reflection hack to get it done:
 * 
 */
public class PluginManager {
    /**
     * Rescans the given folder and adds all the Jar files (plugins)
     * to class path...simply ignores if a jar file is already added...
     *
     * @param path The folder containing plugins...
     * @throws java.io.IOException
     */
    public void rescan(String path) throws IOException {
        File pluginFolder = new File(path);
        File plugins[] = pluginFolder.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getPath().endsWith(".jar")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        for (File f : plugins) {
            addPlugin(f.toURI().toURL());
        }
    }

    /**
     * The url (preferably jar) to be added to the classpath.
     * This is like an install plugin thingy...
     * Must be used in place of {@link #rescan(java.lang.String) rescan} method
     * if a single new plugin is to be installed...gives good performance ups
     *
     * @param url The url to be added to classpath
     */
    public void addPlugin(URL url) throws IOException {
        addURLToClassPath(url);
    }
    /**
     * To avoid un-necessary object creation on method calls...
     */
    private static final Class[] parameters = new Class[]{URL.class};

    /**
     * Adds a URL, preferably a JAR file to classpath. If URL already exists,
     * then this method simply returns...
     *
     * @param u The URL t be added to classpath
     * @throws java.io.IOException
     */
    private void addURLToClassPath(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();

        boolean isAdded = false;
        for (URL url : sysloader.getURLs()) {
            if (url.equals(u)) {
                isAdded = true;
            }
        }
        if (isAdded) {
            return;
        }

        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
    /* In the above code we are adding a URL (typically pointing to a jar file)
    to the system classloader by invoking its private method via reflection.

    In your plugin manager...all you have to do is paste the jar file to a
    certain folder (say user.dir/plugins), then execute the following code
    to add jars to classpath:
     *
     */

    /**
     * Rescans the given folder and adds all the Jar files (plugins)
     * to class path...simply ignores if a jar file is already added...
     *
     * @param path The folder containing plugins...
     * @throws java.io.IOException
     */
//    public void rescan(String path) throws IOException {
//        File pluginFolder = new File(path);
//        File plugins[] = pluginFolder.listFiles(new FileFilter() {
//            public boolean accept(File pathname) {
//                if (pathname.getPath().endsWith(".jar")) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });
//
//        for (File f : plugins) {
//            addPlugin(f.toURI().toURL());
//        }
//    }

    /**
     * The url (preferably jar) to be added to the classpath.
     * This is like an install plugin thingy...
     * Must be used in place of {@link #rescan(java.lang.String) rescan} method
     * if a single new plugin is to be installed...gives good performance ups
     *
     * @param url The url to be added to classpath
     */
//    public void addPlugin(URL url) throws IOException {
//        addURLToClassPath(url);
//    }
    /*
     * That's it! the newly registered service providers can be caught in
     resultChanged(...) method of the LookupListener. Similarly you can remove
     items from the classpath to deactivate the plugins from your application,
     i'll leave that as your home work assignment :)
     */
}
