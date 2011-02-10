// $Id: BaseJniExtractor.java 325571 2009-08-19 15:35:24Z markjh $
// Copyright 2006 MX Telecom Ltd

package com.wapmx.nativeutils.jniloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;

import com.wapmx.nativeutils.MxSysInfo;

/**
 * @author Richard van der Hoff <richardv@mxtelecom.com>
 */
public abstract class BaseJniExtractor implements JniExtractor {
    private static boolean debug = false;
    static {
        // initialise the debug switch
        String s = System.getProperty("java.library.debug");
        if (s != null && (s.toLowerCase().startsWith("y") || s.startsWith("1")))
            debug = true;
    }
    private Class libraryJarClass;

    /**
     * We use a resource path of the form META-INF/lib/${mx.sysinfo}/ This way native builds for multiple architectures
     * can be packaged together without interfering with each other And by setting mx.sysinfo the jvm can pick the
     * native libraries appropriate for itself.
     */
    private String[] nativeResourcePaths;

    public BaseJniExtractor() throws IOException {
        init(null);
    }

    public BaseJniExtractor(Class libraryJarClass) throws IOException {
        init(libraryJarClass);
    }

    private void init(Class libraryJarClass) throws IOException {
        this.libraryJarClass = libraryJarClass;

        String mxSysInfo = MxSysInfo.getMxSysInfo();
        
        if (mxSysInfo != null) {
            nativeResourcePaths = new String[] { "META-INF/lib/" + mxSysInfo + "/",
                    "META-INF/lib/" };
        }
        else {
            nativeResourcePaths = new String[] { "META-INF/lib/" };
        }
    }

    /**
     * this is where native dependencies are extracted to (e.g. tmplib/).
     * 
     * @return native working dir
     */
    public abstract File getNativeDir();

    /**
     *  this is where JNI libraries are extracted to (e.g. tmplib/classloaderName.1234567890000.0/).
     * 
     * @return jni working dir
     */
    public abstract File getJniDir();

    /** {@inheritDoc} */
    public File extractJni(String libname) throws IOException {
        String mappedlib = System.mapLibraryName(libname);
        System.out.println("mappedLib is " + mappedlib);
        /*
         * On Darwin, the default mapping is to .jnilib; but we use .dylibs so that library interdependencies are
         * handled correctly. if we don't find a .jnilib, try .dylib instead.
         */
        URL lib = null;
        
        // if no class specified look for resources in the jar of this class
        if (null == libraryJarClass) {
            libraryJarClass = this.getClass();
        }

        for (int i = 0; i < nativeResourcePaths.length; i++) {
            System.out.println("nativeResourcesPaths[" + i + "] is " + nativeResourcePaths[i]);
            lib = libraryJarClass.getClassLoader().getResource(nativeResourcePaths[i] + mappedlib);
            if (lib != null)
                break;
            if (mappedlib.endsWith(".jnilib")) {
                lib = this.getClass().getClassLoader().getResource(
                        nativeResourcePaths[i] + mappedlib.substring(0, mappedlib.length() - 7) + ".dylib");
                if (lib != null) {
                    mappedlib = mappedlib.substring(0, mappedlib.length() - 7) + ".dylib";
                    break;
                }
            }
        }

        if (null != lib) {
            System.out.println("URL is " + lib.toString());
            System.out.println("URL path is " + lib.getPath());
        }
        System.out.println("jni dir is " + getJniDir().toString());

   // THis is a protected method:     System.out.println("ClassLoader.finLibrary returns " + this.getClass().getClassLoader().findLibrary(mappedlib));

        if (lib != null) {
            return extractResource(getJniDir(), lib, mappedlib);
        }
        else {
            throw new IOException("Couldn't find jni library " + mappedlib + " on the classpath");
        }
    }

    /** {@inheritDoc} */
    public void extractRegistered() throws IOException {
        if (debug) System.err.println("Extracting libraries registered in classloader " + this.getClass().getClassLoader());
        for (int i = 0; i < nativeResourcePaths.length; i++) {
            Enumeration<URL> resources = this.getClass().getClassLoader().getResources(
                    nativeResourcePaths[i] + "AUTOEXTRACT.LIST");
            while (resources.hasMoreElements()) {
                URL res = resources.nextElement();
                if (debug) System.err.println("Extracting libraries listed in " + res);
                BufferedReader r = new BufferedReader(new InputStreamReader(res.openStream(), "UTF-8"));
                String line;
                while ((line = r.readLine()) != null) {
                    URL lib = null;
                    for (int j = 0; j < nativeResourcePaths.length; j++) {
                        lib = this.getClass().getClassLoader().getResource(nativeResourcePaths[j] + line);
                        if (lib != null)
                            break;
                    }
                    if (lib != null) {
                        extractResource(getNativeDir(), lib, line);
                    }
                    else {
                        throw new IOException("Couldn't find native library " + line + "on the classpath");
                    }
                }
            }
        }
    }

    /**
     * Extract a resource to the tmp dir (this entry point is used for unit testing)
     * 
     * @param dir the directory to extract the resource to
     * @param resource the resource on the classpath
     * @param outputname the filename to copy to (within the tmp dir)
     * @return the extracted file
     * @throws IOException
     */
    File extractResource(File dir, URL resource, String outputname) throws IOException {
        InputStream in = resource.openStream(); //TODO there's also a getResourceAsStream
        File outfile = new File(dir, outputname);
        // Create a new file rather than writing into old file
        File outfiletemp = File.createTempFile(outputname, null, getJniDir());
        if (debug)
            System.err.println("Extracting '" + resource + "' to '" + outfile.getAbsolutePath() + "'");
        FileOutputStream out = new FileOutputStream(outfiletemp);
        copy(in, out);
        out.close();
        in.close();
        outfiletemp.renameTo(outfile);
        outfile.deleteOnExit();
        return outfile;
    }

    /**
     * copy an InputStream to an OutputStream.
     * 
     * @param in InputStream to copy from
     * @param out OutputStream to copy to
     * @throws IOException if there's an error
     */
    static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] tmp = new byte[8192];
        int len = 0;
        while (true) {
            len = in.read(tmp);
            if (len <= 0) {
                break;
            }
            out.write(tmp, 0, len);
        }
    }
}
