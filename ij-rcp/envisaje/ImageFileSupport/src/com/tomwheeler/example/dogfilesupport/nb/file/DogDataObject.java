package com.tomwheeler.example.dogfilesupport.nb.file;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;

import com.tomwheeler.example.dogfilesupport.Dog;
import com.tomwheeler.example.dogfilesupport.nb.editor.DogEditorTC;
import com.tomwheeler.example.dogfilesupport.nb.viewer.DogViewerTC;

public class DogDataObject extends MultiDataObject  {

    private static final long serialVersionUID = 6402484094382407376L;

    private Dog dog;
    private SaveCookie saveCookie;
    private ViewCookie viewCookie;
    private EditCookie editCookie;
    
    /* TODO: see java.lang.ref.WeakReference use in ThinletDataObject.
     * Maybe I need to be using this too.
     */

    public DogDataObject(FileObject pf, DogDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);

        saveCookie = new DogSaveCookie(this);
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
        return new DogNode(this);
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
     * This method returns the dog associated with this data object.  This saves
     * other classes from having to parse this information and keeps the implementation
     * localized to this method.  
     * </p>
     * 
     * @return the dog instance contained within the DogDataObject
     */
    public Dog getDog() {
        // a template that's a completely empty file doesn't seem to work correctly;
        // the name in the wizard is blank. Having at least one character in the file
        // appears to work around this, so my template contains the word 'EMPTY'
        
        // default to a new empty dog instance, since that will occur when a new one is
        // created from the "empty" template.  OTOH, it might also mean the file was corrupt.
        if (dog == null) {
            try {
                FileObject dogFileObj = getPrimaryFile();
                if (dogFileObj != null && dogFileObj.getSize() > 15) {
                    ObjectInputStream ois = new ObjectInputStream(dogFileObj.getInputStream());
                    dog = (Dog) ois.readObject();
                } else {
                    dog = new Dog();
                }
            } catch (IOException ioe) {
                // TODO : better error handling needed here
                System.err.println("Could not load Dog instance: " + ioe);
            } catch (ClassNotFoundException cnfe) {
                System.err.println("Could not load Dog instance: " + cnfe);
            }
        }

        return dog;
    }
    
    /**
     * <p>
     * This class implements the ViewCookie interface to provide an
     * implementation of what happens when someone tries to view
     * a DogDataObject.
     * </p>
     */
    private class View implements ViewCookie {

        DogDataObject dogDataObject;

        View(DogDataObject dogDataObject) {
            this.dogDataObject = dogDataObject;
        }

        /**
         * <p> 
         * called when the user tries to view the dog.  To enable this we 
         * just create a new DogViewerTC (dog viewer top component) with 
         * the new dog and then open and activate it.
         * </p> 
         */
        public void view() {
            DogViewerTC tc = new DogViewerTC(dogDataObject);
            tc.open();
            tc.requestActive();
        }
    }

    /**
     * <p>
     * This class implements the EditCookie interface to provide an
     * implementation of what happens when someone tries to edit
     * a DogDataObject.
     * </p>
     */
    private class Edit implements EditCookie {

        DogDataObject dogDataObject;

        Edit(DogDataObject dogDataObject) {
            this.dogDataObject = dogDataObject;
        }

        /** 
         * called when the user tries to edit the dog.  To enable this we 
         * just create a new DogEditorTC (dog editor top component) with 
         * the new dog and then open and activate it. 
         */
        public void edit() {
            DogEditorTC tc = new DogEditorTC(dogDataObject);
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
