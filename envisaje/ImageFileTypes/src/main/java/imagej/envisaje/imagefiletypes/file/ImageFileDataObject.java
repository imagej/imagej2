package imagej.envisaje.imagefiletypes.file;

import java.io.IOException;
import java.io.ObjectInputStream;
import imagej.envisaje.imagefiletypes.ImageFile;
import imagej.envisaje.imagefiletypes.editor.ImageFileEditorTC;
import imagej.envisaje.imagefiletypes.viewer.ImageFileViewerTC;

import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;


public class ImageFileDataObject extends MultiDataObject  {

    private static final long serialVersionUID = 6402484094382407376L;

    private ImageFile ImageFile;
    private SaveCookie saveCookie;
    private ViewCookie viewCookie;
    private EditCookie editCookie;
    
    /* TODO: see java.lang.ref.WeakReference use in ThinletDataObject.
     * Maybe I need to be using this too.
     */

    public ImageFileDataObject(FileObject pf, ImageFileDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);

        saveCookie = new ImageFileSaveCookie(this);
        editCookie = new Edit(this);
        viewCookie = new View(this);

        // adding these here because view and edit are always enabled, unlike
        // the save cookie which is only enabled when the underlying object has 
        // been modified.  See the setModified method to see how this is done.
        CookieSet cookies = getCookieSet();
        cookies.add(getCookie(EditCookie.class));
        cookies.add(getCookie(ViewCookie.class));
    }
    
    /*
     * @see org.openide.loaders.DataObject#createNodeDelegate()
     */
    protected Node createNodeDelegate() {
        /* This is VERY IMPORTANT.  This is how the visual representation
         * (the node, which is displayed in the Files and Projects views)
         * gets created from the data object.
         */
        return new ImageFileNode(this);
    }
    
    /*
     * @see org.openide.loaders.DataObject#getCookie(java.lang.Class)
     */
    public Node.Cookie getCookie(Class clazz) {
        // intercept these calls so we can provide our own implementation
        // for Edit and View (this may not be strictly necessary, but was
        // done in some code for an example I saw.
        if (clazz == EditCookie.class) {
            return editCookie;
        } else if (clazz == ViewCookie.class) {
            return viewCookie;
        }
        
        return super.getCookie(clazz);
    }
    
    /*
     * @see org.openide.loaders.DataObject#setModified(boolean)
     */
    public void setModified(boolean isModified) {
        // I tied the SaveCookie implementation into this such that
        // the Save action is enabled whenever the object is modified.
        if (isModified) {
            if (getCookie(SaveCookie.class) == null) {
                getCookieSet().add(saveCookie);
            }
        } else {
            SaveCookie cookie = (SaveCookie) getCookie(SaveCookie.class);
            if (cookie != null) {
                getCookieSet().remove(cookie);
            }
        }
        super.setModified(isModified);
    }
    
    /**
     * <p>
     * This method returns the ImageFile associated with this data object.  This saves
     * other classes from having to parse this information and keeps the implementation
     * localized to this method.  
     * </p>
     * 
     * @return the ImageFile instance contained within the ImageFileDataObject
     */
    public ImageFile getImageFile() {
        // a template that's a completely empty file doesn't seem to work correctly;
        // the name in the wizard is blank. Having at least one character in the file
        // appears to work around this, so my template contains the word 'EMPTY'
        
        // default to a new empty ImageFile instance, since that will occur when a new one is
        // created from the "empty" template.  OTOH, it might also mean the file was corrupt.
        if (ImageFile == null) {
            try {
                FileObject ImageFileFileObj = getPrimaryFile();
                if (ImageFileFileObj != null && ImageFileFileObj.getSize() > 15) {
                    ObjectInputStream ois = new ObjectInputStream(ImageFileFileObj.getInputStream());
                    ImageFile = (ImageFile) ois.readObject();
                } else {
                    ImageFile = new ImageFile();
                }
            } catch (IOException ioe) {
                // TODO : better error handling needed here
                System.err.println("Could not load ImageFile instance: " + ioe);
            } catch (ClassNotFoundException cnfe) {
                System.err.println("Could not load ImageFile instance: " + cnfe);
            }
        }

        return ImageFile;
    }
    
    /**
     * <p>
     * This class implements the ViewCookie interface to provide an
     * implementation of what happens when someone tries to view
     * a ImageFileDataObject.
     * </p>
     */
    private class View implements ViewCookie {

        ImageFileDataObject ImageFileDataObject;

        View(ImageFileDataObject ImageFileDataObject) {
            this.ImageFileDataObject = ImageFileDataObject;
        }

        /**
         * <p> 
         * called when the user tries to view the ImageFile.  To enable this we 
         * just create a new ImageFileViewerTC (ImageFile viewer top component) with 
         * the new ImageFile and then open and activate it.
         * </p> 
         */
        public void view() {
            ImageFileViewerTC tc = new ImageFileViewerTC(ImageFileDataObject);
            tc.open();
            tc.requestActive();
        }
    }

    /**
     * <p>
     * This class implements the EditCookie interface to provide an
     * implementation of what happens when someone tries to edit
     * a ImageFileDataObject.
     * </p>
     */
    private class Edit implements EditCookie {

        ImageFileDataObject ImageFileDataObject;

        Edit(ImageFileDataObject ImageFileDataObject) {
            this.ImageFileDataObject = ImageFileDataObject;
        }

        /** 
         * called when the user tries to edit the ImageFile.  To enable this we 
         * just create a new ImageFileEditorTC (ImageFile editor top component) with 
         * the new ImageFile and then open and activate it. 
         */
        public void edit() {
            ImageFileEditorTC tc = new ImageFileEditorTC(ImageFileDataObject);
            tc.open();
            tc.requestActive();


            // This would make the property editor the default editor
            // instead of using our own TopComponent
            //        TopComponent c = NbSheet.findDefault();
            //        c.open ();
            //        c.requestActive();
        }
    }
    
}
