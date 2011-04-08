//
// WebappJniExtractor.java
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

// Copyright 2009 MX Telecom Ltd

package loci.wapmx.nativeutils.jniloader;

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
