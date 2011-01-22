// $Id$
// Copyright 2009 MX Telecom Ltd
package com.wapmx.nativeutils.jniloader;

import java.io.File;
import java.io.IOException;

/** 
 * JniExtractor suitable for multiple application deployments on the same virtual machine (such as webapps)
 * <p>Designed to avoid the restriction that jni library can be loaded by at most one classloader at a time.
 * <p>Works by extracting each library to a different location for each classloader.
 * <p>WARNING: This can expose strange and wonderful bugs in jni code. These bugs generally stem from transitive
 * dependencies of the jni library and can be solved by linking these dependencies statically to form a single library 
 * @author <a href="mailto:markjh@mxtelecom.com">markjh</a>
 */
public class WebappJniExtractor extends BaseJniExtractor {

    private File nativeDir;
    private File jniSubDir;

    /**
     * @param classloaderName is a friendly name for your classloader which will be embedded in the directory name
     * of the classloader-specific subdirectory which will be created.
     */
    public WebappJniExtractor(String classloaderName) throws IOException {
        nativeDir = new File(System.getProperty("java.library.tmpdir", "tmplib"));
        // Order of operations is such thatwe do not error if we are racing with another thread to create the directory.
        nativeDir.mkdirs();
        if (!nativeDir.isDirectory()) {
            throw new IOException("Unable to create native library working directory " + nativeDir);
        }

        long now = System.currentTimeMillis();
        File trialJniSubDir;
        int attempt = 0;
        while (true) {
            trialJniSubDir = new File(nativeDir, classloaderName + "." + now + "." + attempt);
            if (trialJniSubDir.mkdir()) 
                break;
            if (trialJniSubDir.exists()) {
                attempt++;
                continue;
            }
            throw new IOException("Unable to create native library working directory " + trialJniSubDir);
        }
        jniSubDir = trialJniSubDir;
        jniSubDir.deleteOnExit();
    }
    
    @Override
    protected void finalize() throws Throwable {
        File[] files = jniSubDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
        jniSubDir.delete();
    }

    @Override
    public File getJniDir() {
        return jniSubDir;
    }

    @Override
    public File getNativeDir() {
        return nativeDir;
    }
}
