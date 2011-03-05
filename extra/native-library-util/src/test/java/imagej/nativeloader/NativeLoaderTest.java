//
// NativeLoaderTest.java
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

package imagej.nativeloader;

import imagej.nativelibrary.NativeLibraryUtil;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Aivar Grislis
 */
public class NativeLoaderTest {

    public NativeLoaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of loadLibrary method, of class NativeLibraryUtil.
     */
    /*
    @Test
    public void testLoadLibrary() {
        System.out.println("loadLibrary");
        Class libraryJarClass = null;
        String libname = "";
        NativeLibraryUtil.loadLibrary(libraryJarClass, libname);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of extractNativeLibraryToPath method, of class NativeLibraryUtil.
     */
    /*
    @Test
    public void testExtractNativeLibraryToPath() {
        System.out.println("extractNativeLibraryToPath");
        Class libraryJarClass = null;
        String libname = "";
        boolean expResult = false;
        boolean result = NativeLibraryUtil.extractNativeLibraryToPath(libraryJarClass, libname);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
    
    /**
     * Test of isOnLibraryPath method, of class NativeLibraryUtil.
     */
    @Test
    public void testIsOnLibraryPath() {
        System.out.println("isOnLibraryPath");
        String directory = "no-such-directory";
        boolean expResult = false;
        boolean result = NativeLibraryUtil.isOnLibraryPath(directory);
        assertEquals(expResult, result);
    }

    /**
     * Test of addToLibraryPath method, of class NativeLibraryUtil.
     */
    @Test
    public void testAddToLibraryPath() {
        System.out.println("addToLibraryPath");
        String directory = "abc";
        boolean expResult = false;
        boolean result = NativeLibraryUtil.isOnLibraryPath(directory);
        assertEquals(expResult, result);

        expResult = true;
        result = NativeLibraryUtil.addToLibraryPath(directory);
        assertEquals(expResult, result);

        expResult = true;
        result = NativeLibraryUtil.isOnLibraryPath(directory);
        assertEquals(expResult, result);
    }

    /**
     * Test of findWritableDirectoryOnPath method, of class NativeLibraryUtil.
     */
    @Test
    public void testFindWritableDirectoryOnPath() {
        System.out.println("findWritableDirectoryOnPath");
        /*
        String result = NativeLibraryUtil.findWritableDirectoryOnPath();
        System.out.println("found writable directory \"" + result + "\"");
        // assume we'll find one, usually "."
        assertNotNull(result);*/
    }

    /**
     * Test of isWritableDirectory method, of class NativeLibraryUtil.
     */
    @Test
    public void testIsWritableDirectory() {
        System.out.println("isWritableDirectory");
        String directory;
        boolean expResult;
        boolean result;

        try {
            File temp = File.createTempFile("dummy", null);
            temp.deleteOnExit();
            directory = temp.getAbsolutePath();
            directory = directory.substring(0, directory.lastIndexOf('/'));
            System.out.println("temporary directory is \"" + directory + "\"");
            expResult = true;
            result = NativeLibraryUtil.isWritableDirectory(directory);
            assertEquals(expResult, result);
        }
        catch (IOException e) {

        }

        directory = "no-such-directory";
        expResult = false;
        result = NativeLibraryUtil.isWritableDirectory(directory);
        assertEquals(expResult, result);
    }
}
