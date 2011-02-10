// $Id: NativeLoader.java 322879 2009-08-13 16:02:00Z markjh $
// Copyright 2006 MX Telecom Ltd

package com.wapmx.nativeutils.jniloader;

import java.io.IOException;

/**
 * Provides a means of loading JNI libraries which are stored within a jar.
 * <p>
 * The library is first extracted to a temporary file, and then loaded with <code>System.load()</code>.
 * <p>
 * The extractor implementation can be replaced, but the default implementation expects to find the library in
 * META-INF/lib/, with its OS-dependent name. It extracts the library underneath a temporary directory, whose name is
 * given by the System property "java.library.tmpdir", defaulting to "tmplib".
 * <p>
 * Debugging can be enabled for the jni extractor by setting the System property "java.library.debug" to 1.
 * <p>
 * This is complicated by the http://java.sun.com/javase/6/docs/technotes/guides/jni/jni-12.html#libmanage -
 * specifically "The same JNI native library cannot be loaded into more than one class loader". In practice this appears
 * to mean "A JNI library on a given absolute path cannot be loaded by more than one classloader". Native libraries that
 * are loaded by the OS dynamic linker as dependencies of JNI libraries are not subject to this restriction.
 * <p>
 * Native libraries that are loaded as dependencies must be extracted using the library identifier a.k.a. soname (which
 * usually includes a major version number) instead of what was linked against (this can be found using ldd on linux or
 * using otool on OS X). Because they are loaded by the OS dynamic linker and not by explicit invocation within Java,
 * this extractor needs to be aware of them to extract them by alternate means. This is accomplished by listing the base
 * filename in a META-INF/lib/AUTOEXTRACT.LIST classpath resource. This is useful for shipping libraries which are used
 * by code which is not itself aware of the NativeLoader system. The application must call {@link #extractRegistered()}
 * at some suitably early point in its initialization (before loading any JNI libraries which might require these
 * dependencies), and ensure that JVM is launched with the LD_LIBRARY_PATH environment variable (or other OS-dependent
 * equivalent) set to include the "tmplib" directory (or other directory as overridden by "java.library.tmpdir" as
 * above).
 * 
 * @author Richard van der Hoff <richardv@mxtelecom.com>
 */
public class NativeLoader {
    private static JniExtractor jniExtractor = null;

    static {
        try {
            /* 
             * We provide two implementations of JniExtractor
             * 
             * The first will work with transitively, dynamically linked libraries with shared global variables 
             *   (e.g. dynamically linked c++) but can only be used by one ClassLoader in the JVM.
             *   
             * The second can be used by multiple ClassLoaders in the JVM but will only work if global variables 
             *   are not shared between transitively, dynamically linked libraries.
             * 
             * For convenience we assume that if the NativeLoader is loaded by the system ClassLoader then it should be 
             *   use the first form, and that if it is loaded by a different ClassLoader then it should use the second.
             */
            if (NativeLoader.class.getClassLoader() == ClassLoader.getSystemClassLoader()) {
                jniExtractor = new DefaultJniExtractor();
            } else {
                jniExtractor = new WebappJniExtractor("Classloader");
            }
        }
        catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Extract the given library from a jar, and load it.
     * <p>
     * The default jni extractor expects libraries to be in META-INF/lib/, with their platform-dependent name.
     * 
     * @param libname platform-independent library name (as would be passed to System.loadLibrary)
     * 
     * @throws IOException if there is a problem extracting the jni library
     * @throws SecurityException if a security manager exists and its <code>checkLink</code> method doesn't allow
     *             loading of the specified dynamic library
     */
    public static void loadLibrary(String libname) throws IOException {
        System.load(jniExtractor.extractJni(libname).getAbsolutePath());
    }

    /**
     * Extract all libraries registered for auto-extraction by way of META-INF/lib/AUTOEXTRACT.LIST resources. The
     * application must call {@link #extractRegistered()} at some suitably early point in its initialization if it is
     * using libraries packaged in this way.
     * 
     * @throws IOException if there is a problem extracting the libraries
     */
    public static void extractRegistered() throws IOException {
        jniExtractor.extractRegistered();
    }

    /**
     * @return the JniExtractor implementation object.
     */
    public static JniExtractor getJniExtractor() {
        return jniExtractor;
    }

    /**
     * @param jniExtractor JniExtractor implementation to use instead of the default.
     */
    public static void setJniExtractor(JniExtractor jniExtractor) {
        NativeLoader.jniExtractor = jniExtractor;
    }
}
