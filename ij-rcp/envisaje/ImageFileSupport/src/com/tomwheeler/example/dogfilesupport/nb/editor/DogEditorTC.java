package com.tomwheeler.example.dogfilesupport.nb.editor;

import com.tomwheeler.example.dogfilesupport.nb.file.DogDataObject;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.windows.*;

import com.tomwheeler.example.dogfilesupport.Dog;

/**
 * <p>
 * This class is a NetBeans TopComponent that displays the editor for a
 * dog instance.
 * </p>
 *
 * @author Tom Wheeler
 */
public class DogEditorTC extends CloneableTopComponent implements PropertyChangeListener {

    private static final String MODE = "editor";

    private DogEditor dogEditor;
    private DogDataObject dogDataObj;

    public DogEditorTC(DogDataObject dogDataObj) {
        super();
        setLayout(new BorderLayout());
        setDisplayName("Dog Editor Top Component");
        setIcon(Utilities.loadImage("com/tomwheeler/example/dogfilesupport/nb/resources/dogicon.gif"));
        if (dogDataObj != null) {
            this.dogDataObj = dogDataObj;

            Dog dog = dogDataObj.getDog();
            this.setDisplayName(dog.getName());

            dog.addPropertyChangeListener((PropertyChangeListener) this);
            dogEditor = new DogEditor(dog);
        }

        // listener on the dataobject so we can change the tab name on save
        // (remove the asterisk). The propertyChange method in this class is 
        // attached to the events on the dog instance rather than the dog data object.
        dogDataObj.addPropertyChangeListener(new DataObjectPropListener(this));
        // I tried doing this, which seems better, but didn't work.
        // dogDataObj.getPrimaryFile().addFileChangeListener(new FileChangeAdapter() { ...

        setLayout(new BorderLayout());
        add(dogEditor, BorderLayout.NORTH);

        // this enables the Save action by associating this editor with the
        // selected node.  This is VERY IMPORTANT because it is not otherwise
        // obvious that you need to call this.
        setActivatedNodes(new Node[]{ dogDataObj.getNodeDelegate()} );
    }

    DogDataObject getDataObject() {
        // used by the DataObjectPropListener inner class
        return dogDataObj;
    }

    /*
     * @see org.openide.util.HelpCtx$Provider#getHelpCtx()
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /*
     * @see org.openide.windows.TopComponent#open()
     */
    public void open() {
        Mode mode = WindowManager.getDefault().findMode(MODE);
        if (mode != null) {
            mode.dockInto(this);
        }

        super.open();
    }

    /*
     * @see org.openide.windows.TopComponent#preferredID()
     */
    protected String preferredID() {
        return "tcdogeditor";
    }

    /*
     * @see org.openide.windows.TopComponent#getPersistenceType()
     */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    /*
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {

        // the asterisk is added here, but note that it is removed in the inner
        // DataObjectPropListener class by the listener on the DogDataObject
        dogDataObj.setModified(true);

        // this is the NetBeans idiom for running some code in the AWT Event
        // dispatch thread -- it's roughly equivalent to SwingWorker.invokeLater.
        // If you update the GUI from outside the EDT, you will likely get a
        // dialog box with an IllegalStateException and possibly worse things.
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                // add an asterisk to the tab label, indicating that 'save' is needed
                setDisplayName(dogDataObj.getDog().getName() + " *");
            }
        });
    }

    /* (non-Javadoc)
     * @see org.openide.windows.CloneableTopComponent#createClonedObject()
     */
    protected CloneableTopComponent createClonedObject() {
        return new DogEditorTC(dogDataObj);
    }

    /* (non-Javadoc)
     * @see org.openide.windows.TopComponent#getUndoRedo()
     */
    public UndoRedo getUndoRedo() {
        
        return new DogUndoRedo();
    }

    /**
     * <p>
     * This class provides a shell implementation of an undoable editor.
     * It is always enabled and simply prints messages to standard output
     * whenever the undo or redo actions are invoked.  Still, it provides 
     * a starting point for creating one.  I recommend looking at the 
     * <code>UndoRedoStampFlagManager</code> inner class within the 
     * <code>org.netbeans.modules.properties.PropertiesEditorSupport</code>
     * class' source code to see an example of a working undo manager.
     * </p>
     */
    public class DogUndoRedo extends UndoRedo.Manager {

        public String getUndoPresentationName() {
            return "dog edit";
        }

        public String getRedoPresentationName() {
            return "dog edit";
        }

        public boolean canUndo() {
            return true;
        }

        public boolean canRedo() {
            return true;
        }

        public void undo() throws CannotUndoException {
            System.out.println("Would undo an edit to the dog instance");
        }

        public void redo() throws CannotRedoException {
            System.out.println("Would redo an edit to the dog instance");
        }
    }

    private class DataObjectPropListener implements PropertyChangeListener {
        private DogEditorTC editorTc;

        DataObjectPropListener(DogEditorTC editorTc) {
            this.editorTc = editorTc;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            final DogDataObject ddo = editorTc.getDataObject();
            if (!ddo.isModified()) {

                Mutex.EVENT.writeAccess(new Runnable() {
                    public void run() {
                        // file is saved; remove the asterisk from the tab label
                        editorTc.setDisplayName(ddo.getDog().getName());
                    }
                });
            }
        }
    }
}
