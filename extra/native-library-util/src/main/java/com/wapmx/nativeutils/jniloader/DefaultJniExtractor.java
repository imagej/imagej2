// $Id$
// Copyright 2009 MX Telecom Ltd
package com.wapmx.nativeutils.jniloader;

import java.io.File;
import java.io.IOException;

/**
 * JniExtractor suitable for single application deployments per virtual machine
 * <p>
 * WARNING: This extractor can result in UnsatisifiedLinkError if it is used in more than one classloader.
 * @author Richard van der Hoff <richardv@mxtelecom.com>
 */
public class DefaultJniExtractor extends BaseJniExtractor {
    
    /**
     * this is where native dependencies are extracted to (e.g. tmplib/).
     */
    private File nativeDir;

    public DefaultJniExtractor() throws IOException {
        super(null);
        init("tmplib");
    }

    public DefaultJniExtractor(Class libraryJarClass, String tmplib) throws IOException {
        super(libraryJarClass);
        init(tmplib);
    }
    
    void init(String tmplib) throws IOException {
        nativeDir = new File(System.getProperty("java.library.tmpdir", tmplib));
        // Order of operations is such that we do not error if we are racing with another thread to create the directory.
        nativeDir.mkdirs();
        if (!nativeDir.isDirectory()) {
            throw new IOException("Unable to create native library working directory " + nativeDir);
        }
    }
    
    @Override
    public File getJniDir() {
        return nativeDir;
    }

    @Override
    public File getNativeDir() {
        return nativeDir;
    }

}
