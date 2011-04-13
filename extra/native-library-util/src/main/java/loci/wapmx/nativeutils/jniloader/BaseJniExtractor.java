//
// BaseJniExtractor.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

// This code is derived from Richard van der Hoff's mx-native-loader project:
// http://opensource.mxtelecom.com/maven/repo/com/wapmx/native/mx-native-loader/1.7/
// See NOTICE.txt for details.

// Copyright 2006 MX Telecom Ltd

package loci.wapmx.nativeutils.jniloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;

import ij.IJ;
import java.io.FilenameFilter;

import loci.wapmx.nativeutils.MxSysInfo;

/**
 * @author Richard van der Hoff <richardv@mxtelecom.com>
 */
public abstract class BaseJniExtractor implements JniExtractor {
    private static final String JAVA_TMPDIR = "java.io.tmpdir";
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
    public File extractJni(String libPath, String libname) throws IOException {
        String mappedlibName = System.mapLibraryName(libname);
        System.out.println("mappedLib is " + mappedlibName);
        /*
         * On Darwin, the default mapping is to .jnilib; but we use .dylibs so that library interdependencies are
         * handled correctly. if we don't find a .jnilib, try .dylib instead.
         */
        URL lib = null;
        
        // if no class specified look for resources in the jar of this class
        if (null == libraryJarClass) {
            libraryJarClass = this.getClass();
        }

        lib = libraryJarClass.getClassLoader().getResource(libPath + mappedlibName);
        if (null == lib) {
            if (mappedlibName.endsWith(".jnilib")) {
                lib = this.getClass().getClassLoader().getResource(
                        libPath + mappedlibName.substring(0, mappedlibName.length() - 7) + ".dylib");
                if (null != lib) {
                    mappedlibName = mappedlibName.substring(0, mappedlibName.length() - 7) + ".dylib";
                }
            }

        }

        if (null != lib) {
            System.out.println("URL is " + lib.toString());
            System.out.println("URL path is " + lib.getPath());
            return extractResource(getJniDir(), lib, mappedlibName);
        }
        else {
            IJ.log("Couldn't find resource " + libPath + " " + mappedlibName);
            throw new IOException("Couldn't find resource " + libPath + " " + mappedlibName);
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
    File extractResource(File dir, URL resource, String outputName) throws IOException {

        InputStream in = resource.openStream(); //TODO there's also a getResourceAsStream

        // create a temporary file with same suffix (i.e. ".dylib")
        String prefix = outputName;
        String suffix = null;
        int lastDotIndex = outputName.lastIndexOf('.');
        if (-1 != lastDotIndex) {
            prefix = outputName.substring(0, lastDotIndex);
            suffix = outputName.substring(lastDotIndex);
        }
        
        // clean up leftover libraries from previous runs
        deleteLeftoverFiles(prefix, suffix);

        // make a temporary file with ".tmp" suffix
        File outfile = File.createTempFile(prefix, null);
        if (debug)
            System.err.println("Extracting '" + resource + "' to '" + outfile.getAbsolutePath() + "'");

        // copy resource stream to temporary file
        FileOutputStream out = new FileOutputStream(outfile);
        copy(in, out);
        out.close();
        in.close();

        // rename temporary file with our suffix
        // (necessary because createTempFile only guarantees three chars of suffix)
        File tmpDirectory = new File(System.getProperty(JAVA_TMPDIR));
        outfile.renameTo(new File(tmpDirectory, prefix + suffix));

        // note that this doesn't always work:
        outfile.deleteOnExit();
        
        return outfile;
    }

    /**
     * Looks in the temporary directory for leftover versions of temporary shared
     * libraries.
     * <p>
     * If a temporary shared library is in use by another instance it won't delete.
     * <p>
     * There is a very unlikely race condition if another instance created a
     * temporary shared library and now I delete it just before it tries to load
     * it.
     * <p>
     * Another issue is that createTempFile only guarantees to use the first
     * three characters of the prefix, so I could delete a similarly-named
     * temporary shared library if I haven't loaded it yet.
     *
     * @param prefix
     * @param suffix
     */
    void deleteLeftoverFiles(final String prefix, final String suffix) {
        File tmpDirectory = new File(System.getProperty(JAVA_TMPDIR));
        File[] files = tmpDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix) && name.endsWith(suffix);
            }
        });
        for (File file : files) {
            // attempt to delete
            try {
                file.delete();
            }
            catch (SecurityException e) {
                // not likely
            }
        }
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
