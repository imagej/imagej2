package com.tomwheeler.example.dogfilesupport.nb.viewer;

import com.tomwheeler.example.dogfilesupport.Dog;
import com.tomwheeler.example.dogfilesupport.nb.file.DogDataObject;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.*;
import org.openide.windows.*;

/**
 * <p>
 * This class is a NetBeans TopComponent that displays the viewer for a dog
 * instance.
 * </p>
 * 
 * @author Tom Wheeler
 */
public class DogViewerTC extends CloneableTopComponent implements PropertyChangeListener {

    private static final long serialVersionUID = 4363008431206380604L;

    private static final String MODE = "explorer";

    private DogViewerPanel dogViewerPanel;
    private DogDataObject dogDataObject;
    private Dog dog;

    public DogViewerTC(DogDataObject dogDataObj) {
        super();
        setLayout(new BorderLayout());
        setDisplayName("Dog Viewer Top Component");
        setIcon(Utilities.loadImage("com/tomwheeler/example/dogfilesupport/nb/resources/dogicon.gif"));

        this.dogDataObject = dogDataObj;
        if (dogDataObj != null) {
            dog = dogDataObj.getDog();
            
            dog.addPropertyChangeListener((PropertyChangeListener) this);
            setDisplayName(dog.getName());

            dogViewerPanel = new DogViewerPanel();
        }

        updateWithDogProperties(); // initially populate it
        setLayout(new BorderLayout());
        add(dogViewerPanel, BorderLayout.CENTER);
    }

    /** updates the dog viewer panel with changes to the current dog instance */
    private void updateWithDogProperties() {
        if (dog != null) {
            dogViewerPanel.setNameValue(dog.getName());
            dogViewerPanel.setAgeValue(String.valueOf(dog.getAge()));
            dogViewerPanel.setSexValue(dog.getSex().name());
            dogViewerPanel.setBreedValue(dog.getBreed().name());
            dogViewerPanel.setPlaysFetchValue(String.valueOf(dog.getPlaysFetch()));
        } else {
            dogViewerPanel.setNameValue(null);
            dogViewerPanel.setAgeValue(null);
            dogViewerPanel.setSexValue(null);
            dogViewerPanel.setBreedValue(null);
            dogViewerPanel.setPlaysFetchValue(null);
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
        return "tcdogviewer";
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
        // change the tab title in sync with changes on the dog
        // this is the NetBeans idiom for running some code in the AWT event
        // dispatch thread -- it's roughly equivalent to SwingWorker.invokeLater.
        // If you update the GUI from outside the EDT, you will likely get a
        // dialog box with an IllegalStateException and possibly worse things.
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                setDisplayName(dog.getName());
                updateWithDogProperties();
            }
        });
    }

    /** To enable cloning of the top component (see the context menu on this component's editor tab) */
    protected CloneableTopComponent createClonedObject() {
        return new DogViewerTC(dogDataObject);
    }
}
