/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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