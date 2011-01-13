package org.imagejdev.imagefiletypes.file;

import java.io.*;
import org.imagejdev.imagefiletypes.ImageFile;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;


/**
 * <p>
 * This class implements the SaveCookie interface to provide a
 * <code>save()</code> method that will save the ImageFile instance
 * to an external file.
 * </p>
 *
 * @author Tom Wheeler
 */
public class ImageFileSaveCookie implements SaveCookie {
    ImageFileDataObject ImageFileDatObj;

    public ImageFileSaveCookie(ImageFileDataObject ImageFileDatObj) {
        this.ImageFileDatObj = ImageFileDatObj;
    }

    /* 
     * @see org.openide.cookies.SaveCookie#save()
     */
    public void save() throws IOException {
        // This is the actual code that saves the ImageFile object to disk.  Our example
        // just uses serialization, but you could include code here to save in
        // whatever format you wanted, although you'd need to ensure that the 
        // code in the ImageFileDataObject is capable of reading it. 
        
        // first check error conditions
        if (ImageFileDatObj == null) {
            throw new IOException("No ImageFile to save!");
        } else if (!ImageFileDatObj.isModified()) { 
            return; 
        }

        // everything's OK; start writing the file
        FileObject ImageFileFileObj = ImageFileDatObj.getPrimaryFile();
        
        // you must use the NetBeans locking mechanism as shown here or
        // you will get an Exception (or possibly a corrupt file).
        FileLock lock = ImageFileFileObj.lock();
        if (!lock.isValid()) { 
            throw new IOException("Invalid lock"); 
        }

        try {
            // We should be able to just get the output stream from the DataObject, but
            // according to Tim Boudreau of the NetBeans team, there is a bug which results
            // in an IOException at org.netbeans.modules.masterfs.filebasedfs.fileobjects.MutualExclusion
            // Support.addResource(MutualExclusionSupport.java:67)
            //
            // The workaround, for now, is shown here
			// TODO: I wrote this code under NB 4.1, so I need to make sure this is still valid for 5.0
            File f = FileUtil.toFile(ImageFileFileObj);
            FileOutputStream fos = new FileOutputStream(f);
            BufferedOutputStream baos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            ImageFile ImageFile = ImageFileDatObj.getImageFile();
            if (ImageFile != null) {
                oos.writeObject(ImageFile);
                oos.flush();
            }
        } finally {
            lock.releaseLock();
        }

        // this will disable the save action (not automatically by default, 
        // but based on how I coded the setModified method in the DataObject)
        ImageFileDatObj.setModified(false);
    }
}
