package com.tomwheeler.example.dogfilesupport.nb.file;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * <p>
 * This class extends the UniFileLoader class to provide support for loading
 * information in a Dog file.  The <code>createMultiObject</code> method is
 * the &quot;meat&quot; of this class; it creates the data object from the file
 * </p>
 * 
 * <p>
 * A lot of code in this class is tied to information in the 
 * Dogresolver file.  This sets up the MIME type and file
 * extensions for Dog files.
 * </p>
 *
 * @author Tom Wheeler
 */
public class DogDataLoader extends UniFileLoader {

    public static final String REQUIRED_MIME = "text/x-dog";

    private static final long serialVersionUID = 1L;

    public DogDataLoader() {
        super("com.tomwheeler.example.dogfilesupport.nb.file.DogDataObject");
    }

    /*
     * @see org.openide.loaders.DataLoader#defaultDisplayName()
     */
    protected String defaultDisplayName() {
        return "Dog files";
    }

    /*
     * @see org.openide.util.SharedClassObject#initialize()
     */
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    /*
     * @see org.openide.loaders.MultiFileLoader#createMultiObject(org.openide.filesystems.FileObject)
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        // This is the most important method; it returns the data object associated   
        // with this file (or, potentially, associated with some group of files.
        return new DogDataObject(primaryFile, this);
    }

    /*
     * @see org.openide.loaders.DataLoader#actionsContext()
     */
    protected String actionsContext() {
        // specifies the location in the layer.xml file where default actions 
        // for this file type are defined.
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
}
