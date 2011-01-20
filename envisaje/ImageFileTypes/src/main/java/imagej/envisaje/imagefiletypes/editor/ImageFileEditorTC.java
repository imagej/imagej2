package imagej.envisaje.imagefiletypes.editor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import imagej.envisaje.imagefiletypes.ImageFile;
import imagej.envisaje.imagefiletypes.file.ImageFileDataObject;

import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.windows.*;


/**
 * <p>
 * This class is a NetBeans TopComponent that displays the editor for a
 * ImageFile instance.
 * </p>
 *
 * @author Tom Wheeler
 */
public class ImageFileEditorTC extends CloneableTopComponent implements PropertyChangeListener {

    private static final String MODE = "editor";

    private ImageFileEditor ImageFileEditor;
    private ImageFileDataObject ImageFileDataObj;

    public ImageFileEditorTC(ImageFileDataObject ImageFileDataObj) {
        super();
        setLayout(new BorderLayout());
        setDisplayName("ImageFile Editor Top Component");
        setIcon(Utilities.loadImage("/imagej/envisaje/imagefiletypes/resources/ImageFileicon.gif"));
        if (ImageFileDataObj != null) {
            this.ImageFileDataObj = ImageFileDataObj;

            ImageFile ImageFile = ImageFileDataObj.getImageFile();
            this.setDisplayName(ImageFile.getName());

            ImageFile.addPropertyChangeListener((PropertyChangeListener) this);
            ImageFileEditor = new ImageFileEditor(ImageFile);
        }

        // listener on the dataobject so we can change the tab name on save
        // (remove the asterisk). The propertyChange method in this class is 
        // attached to the events on the ImageFile instance rather than the ImageFile data object.
        ImageFileDataObj.addPropertyChangeListener(new DataObjectPropListener(this));
        // I tried doing this, which seems better, but didn't work.
        // ImageFileDataObj.getPrimaryFile().addFileChangeListener(new FileChangeAdapter() { ...

        setLayout(new BorderLayout());
        add(ImageFileEditor, BorderLayout.NORTH);

        // this enables the Save action by associating this editor with the
        // selected node.  This is VERY IMPORTANT because it is not otherwise
        // obvious that you need to call this.
        setActivatedNodes(new Node[]{ ImageFileDataObj.getNodeDelegate()} );
    }

    ImageFileDataObject getDataObject() {
        // used by the DataObjectPropListener inner class
        return ImageFileDataObj;
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
        return "tcImageFileeditor";
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
        // DataObjectPropListener class by the listener on the ImageFileDataObject
        ImageFileDataObj.setModified(true);

        // this is the NetBeans idiom for running some code in the AWT Event
        // dispatch thread -- it's roughly equivalent to SwingWorker.invokeLater.
        // If you update the GUI from outside the EDT, you will likely get a
        // dialog box with an IllegalStateException and possibly worse things.
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                // add an asterisk to the tab label, indicating that 'save' is needed
                setDisplayName(ImageFileDataObj.getImageFile().getName() + " *");
            }
        });
    }

    /* (non-Javadoc)
     * @see org.openide.windows.CloneableTopComponent#createClonedObject()
     */
    protected CloneableTopComponent createClonedObject() {
        return new ImageFileEditorTC(ImageFileDataObj);
    }

    /* (non-Javadoc)
     * @see org.openide.windows.TopComponent#getUndoRedo()
     */
    public UndoRedo getUndoRedo() {
        
        return new ImageFileUndoRedo();
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
    public class ImageFileUndoRedo extends UndoRedo.Manager {

        public String getUndoPresentationName() {
            return "ImageFile edit";
        }

        public String getRedoPresentationName() {
            return "ImageFile edit";
        }

        public boolean canUndo() {
            return true;
        }

        public boolean canRedo() {
            return true;
        }

        public void undo() throws CannotUndoException {
            System.out.println("Would undo an edit to the ImageFile instance");
        }

        public void redo() throws CannotRedoException {
            System.out.println("Would redo an edit to the ImageFile instance");
        }
    }

    private class DataObjectPropListener implements PropertyChangeListener {
        private ImageFileEditorTC editorTc;

        DataObjectPropListener(ImageFileEditorTC editorTc) {
            this.editorTc = editorTc;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            final ImageFileDataObject ddo = editorTc.getDataObject();
            if (!ddo.isModified()) {

                Mutex.EVENT.writeAccess(new Runnable() {
                    public void run() {
                        // file is saved; remove the asterisk from the tab label
                        editorTc.setDisplayName(ddo.getImageFile().getName());
                    }
                });
            }
        }
    }
}
