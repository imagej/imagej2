package imagej.envisaje.imagefiletypes.viewer;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import imagej.envisaje.imagefiletypes.ImageFile;
import imagej.envisaje.imagefiletypes.file.ImageFileDataObject;
import org.openide.util.*;
import org.openide.windows.*;

/**
 * <p>
 * This class is a NetBeans TopComponent that displays the viewer for a ImageFile
 * instance.
 * </p>
 * 
 * @author Tom Wheeler
 */
public class ImageFileViewerTC extends CloneableTopComponent implements PropertyChangeListener {

    private static final long serialVersionUID = 4363008431206380604L;

    private static final String MODE = "explorer";

    private ImageFileViewerPanel ImageFileViewerPanel;
    private ImageFileDataObject ImageFileDataObject;
    private ImageFile ImageFile;

    public ImageFileViewerTC(ImageFileDataObject ImageFileDataObj) {
        super();
        setLayout(new BorderLayout());
        setDisplayName("ImageFile Viewer Top Component");
        setIcon(Utilities.loadImage("/imagej/envisaje/imagefiletypes/resources/ImageFileicon.gif"));

        this.ImageFileDataObject = ImageFileDataObj;
        if (ImageFileDataObj != null) {
            ImageFile = ImageFileDataObj.getImageFile();
            
            ImageFile.addPropertyChangeListener((PropertyChangeListener) this);
            setDisplayName(ImageFile.getName());

            ImageFileViewerPanel = new ImageFileViewerPanel();
        }

        updateWithImageFileProperties(); // initially populate it
        setLayout(new BorderLayout());
        add(ImageFileViewerPanel, BorderLayout.CENTER);
    }

    /** updates the ImageFile viewer panel with changes to the current ImageFile instance */
    private void updateWithImageFileProperties() {
        if (ImageFile != null) {
            ImageFileViewerPanel.setNameValue(ImageFile.getName());
            ImageFileViewerPanel.setAgeValue(String.valueOf(ImageFile.getType()));
            ImageFileViewerPanel.setSexValue(ImageFile.getSex().name());
            ImageFileViewerPanel.setBreedValue(ImageFile.getBreed().name());
            ImageFileViewerPanel.setPlaysFetchValue(String.valueOf(ImageFile.getPlaysFetch()));
        } else {
            ImageFileViewerPanel.setNameValue(null);
            ImageFileViewerPanel.setAgeValue(null);
            ImageFileViewerPanel.setSexValue(null);
            ImageFileViewerPanel.setBreedValue(null);
            ImageFileViewerPanel.setPlaysFetchValue(null);
        }
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

    protected String preferredID() {
        return "tcImageFileviewer";
    }

    /*
     * (non-Javadoc)
     * @see org.openide.windows.TopComponent#getPersistenceType()
     */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    protected boolean asynchronous() {
        return false;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // change the tab title in sync with changes on the ImageFile
        // this is the NetBeans idiom for running some code in the AWT event
        // dispatch thread -- it's roughly equivalent to SwingWorker.invokeLater.
        // If you update the GUI from outside the EDT, you will likely get a
        // dialog box with an IllegalStateException and possibly worse things.
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                setDisplayName(ImageFile.getName());
                updateWithImageFileProperties();
            }
        });
    }

    /** To enable cloning of the top component (see the context menu on this component's editor tab) */
    protected CloneableTopComponent createClonedObject() {
        return new ImageFileViewerTC(ImageFileDataObject);
    }
}
