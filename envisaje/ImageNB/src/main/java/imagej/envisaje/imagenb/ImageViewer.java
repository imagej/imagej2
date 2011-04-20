/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package imagej.envisaje.imagenb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;

/**
 * Top component providing a viewer for images.
 * @author Petr Hamernik, Ian Formanek, Lukas Tadial
 * @author Marian Petras
 */
public class ImageViewer extends CloneableTopComponent {

    /** Serialized version UID. */
    static final long serialVersionUID =6960127954234034486L;
    
    /** <code>ImageDataObject</code> which image is viewed. */
    private ImageDataObject storedObject;
    
    /** Viewed image. */
    private NBImageIcon storedImage;
    
    /** Component showing image. */
    private JPanel panel;
    
    /** Scale of image. */
    private double scale = 1.0D;
    
    /** On/off grid. */
    private boolean showGrid = false;
    
    /** Increase/decrease factor. */
    private final double changeFactor = Math.sqrt(2.0D);
    
    /** Grid color. */
    private final Color gridColor = Color.black;
    
    /** Listens for name changes. */
    private PropertyChangeListener nameChangeL;
    
    /** collection of all buttons in the toolbar */
    private final Collection/*<JButton>*/ toolbarButtons
                                          = new ArrayList/*<JButton>*/(11);
    
    
    /** Default constructor. Must be here, used during de-externalization */
    public ImageViewer () {
        super();
    }
    
    /** Create a new image viewer.
     * @param obj the data object holding the image
     */
    public ImageViewer(ImageDataObject obj) {
        initialize(obj);
    }
    
    /** Overriden to explicitely set persistence type of ImageViewer
     * to PERSISTENCE_ONLY_OPENED */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    /** Reloads icon. */
    protected void reloadIcon() {
        resizePanel();
        panel.repaint();
    }
    
    /** Initializes member variables and set listener for name changes on DataObject. */
    private void initialize(ImageDataObject obj) {
        TopComponent.NodeName.connect (this, obj.getNodeDelegate ());
        setToolTipText(FileUtil.getFileDisplayName(obj.getPrimaryFile()));
        
        storedObject = obj;
            
        // force closing panes in all workspaces, default is in current only
        setCloseOperation(TopComponent.CLOSE_EACH);
        
        /* try to load the image: */
        String errMsg = loadImage(storedObject);
        
        /* compose the whole panel: */
        JToolBar toolbar = createToolBar();
        Component view;
        if (storedImage != null) {
            view = createImageView();
        } else {
            view = createMessagePanel(errMsg);
            setToolbarButtonsEnabled(false);
        }
        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);
        add(toolbar, BorderLayout.NORTH);

        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_ImageViewer"));        
        
        nameChangeL = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (DataObject.PROP_COOKIE.equals(evt.getPropertyName()) ||
                DataObject.PROP_NAME.equals(evt.getPropertyName())) {
                    updateNameInEDT();
                }
            }
        };
        
        obj.addPropertyChangeListener(WeakListeners.propertyChange(nameChangeL, obj));
        
        setFocusable(true);
    }

    /**
     * Updates name in the Event Dispatch Thread.
     * @see Bug #181283
     */
    private void updateNameInEDT() {
        if(SwingUtilities.isEventDispatchThread()) {
            updateName();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    updateName();
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /**
     */
    private Component createImageView() {
        panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(
                    storedImage.getImage(),
                    0,
                    0,
                    (int)(getScale () * storedImage.getIconWidth ()),
                    (int)(getScale () * storedImage.getIconHeight ()),
                    0,
                    0,
                    storedImage.getIconWidth(),
                    storedImage.getIconHeight(),
                    this
                );

                if(showGrid) {
                    int x = (int)(getScale () * storedImage.getIconWidth ());
                    int y = (int)(getScale () * storedImage.getIconHeight ());

                    double gridDistance = getScale();

                    if(gridDistance < 2) 
                        // Disable painting of grid if no image pixels would be visible.
                        return;

                    g.setColor(gridColor);

                    double actualDistance = gridDistance;
                    for(int i = (int)actualDistance; i < x ;actualDistance += gridDistance, i = (int)actualDistance) {
                        g.drawLine(i,0,i,(y-1));
                    }

                    actualDistance = gridDistance;
                    for(int j = (int)actualDistance; j < y; actualDistance += gridDistance, j = (int)actualDistance) {
                        g.drawLine(0,j,(x-1),j);
                    }
                }

            }

        };
        // vlv: print
        panel.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        panel.putClientProperty("print.name", getToolTipText()); // NOI18N

        storedImage.setImageObserver(panel);
        panel.setPreferredSize(new Dimension(storedImage.getIconWidth(), storedImage.getIconHeight() ));

        return new JScrollPane(panel);
    }
    
    /**
     */
    private Component createMessagePanel(final String msg) {
        JPanel msgPanel = new JPanel(new java.awt.GridBagLayout());
        msgPanel.add(new JLabel(msg),
                     new java.awt.GridBagConstraints(
                             0,    0,                  //gridx, gridy
                             1,    1,                  //gridwidth, gridheight
                             1.0d, 1.0d,               //weightx, weighty
                             java.awt.GridBagConstraints.CENTER,   //anchor
                             java.awt.GridBagConstraints.NONE,     //fill
                             new java.awt.Insets(0, 0, 0, 0),      //insets
                             10,   10));               //ipadx, ipady
        return msgPanel;
    }
    
    /**
     */
    void updateView(final ImageDataObject imageObj) {
        boolean wasValid = (storedImage != null);
        String errMsg = loadImage(imageObj);
        boolean isValid = (storedImage != null);
        
        if (wasValid && isValid) {
            reloadIcon();
            return;
        }
        
        Component view = (storedImage != null) ? createImageView()
                                               : createMessagePanel(errMsg);
        remove(0);
        add(view, BorderLayout.CENTER, 0);
        if (wasValid != isValid) {
            setToolbarButtonsEnabled(isValid);
        }
    }
    
    /**
     * Enables or disables all toolbar buttons.
     *
     * @param  enabled  <code>true</code> if all buttons should be enabled,
     *                  <code>false</code> if all buttons should be disabled
     */
    private void setToolbarButtonsEnabled(final boolean enabled) {
        assert toolbarButtons != null;
        
        final Iterator/*<JButton>*/ it = toolbarButtons.iterator();
        while (it.hasNext()) {
            ((JButton) it.next()).setEnabled(enabled);
        }
    }
    
    /**
     * Loads an image from the given <code>ImageDataObject</code>.
     * If the image is loaded successfully, it is stored
     * to field {@link #storedImage}. The field is <code>null</code>ed
     * in case of any failure.
     *
     * @param  imageObj  <code>ImageDataObject</code> to load the image from
     * @return  <code>null</code> if the image was loaded successfully,
     *          or a localized error message in case of failure
     */
    private String loadImage(final ImageDataObject imageObj) {
        String errMsg;
        try {
            storedImage = NBImageIcon.load(imageObj);
            if (storedImage != null) {
                errMsg = null;
            } else {
                errMsg = NbBundle.getMessage(ImageViewer.class,
                                             "MSG_CouldNotLoad");       //NOI18N
            }
        } catch (IOException ex) {
            storedImage = null;
            errMsg = NbBundle.getMessage(ImageViewer.class,
                                         "MSG_ErrorWhileLoading");      //NOI18N
        }
        assert (storedImage == null) != (errMsg == null);
        return errMsg;
    }
    
    /** Creates toolbar. */
    private JToolBar createToolBar() {
        // Definition of toolbar.
        JToolBar toolBar = new JToolBar();
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        toolBar.setFloatable (false);
        toolBar.setName (NbBundle.getBundle(ImageViewer.class).getString("ACSN_Toolbar"));
        toolBar.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACSD_Toolbar"));
            JButton outButton = new JButton(SystemAction.get(ZoomOutAction.class));
            outButton.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomOut"));
            outButton.setMnemonic(NbBundle.getBundle(ImageViewer.class).getString("ACS_Out_BTN_Mnem").charAt(0));
            outButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACSD_Out_BTN"));
            outButton.setText("");
        toolBar.add(outButton);       
        toolbarButtons.add(outButton);
            JButton inButton = new JButton(SystemAction.get(ZoomInAction.class));
            inButton.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomIn"));
            inButton.setMnemonic(NbBundle.getBundle(ImageViewer.class).getString("ACS_In_BTN_Mnem").charAt(0));
            inButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACSD_In_BTN"));
            inButton.setText("");
        toolBar.add(inButton);
        toolbarButtons.add(inButton);
        toolBar.addSeparator(new Dimension(11, 0));
        
        JButton button;
        
        toolBar.add(button = getZoomButton(1,1));
        toolbarButtons.add(button);
        toolBar.addSeparator(new Dimension(11, 0));
        toolBar.add(button = getZoomButton(1,3));
        toolbarButtons.add(button);
        toolBar.add(button = getZoomButton(1,5));
        toolbarButtons.add(button);
        toolBar.add(button = getZoomButton(1,7));
        toolbarButtons.add(button);
        toolBar.addSeparator(new Dimension(11, 0));
        toolBar.add(button = getZoomButton(3,1));
        toolbarButtons.add(button);
        toolBar.add(button = getZoomButton(5,1));
        toolbarButtons.add(button);
        toolBar.add(button = getZoomButton(7,1));
        toolbarButtons.add(button);
        toolBar.addSeparator(new Dimension(11, 0));
//        SystemAction sa = SystemAction.get(CustomZoomAction.class);
//        sa.putValue (Action.SHORT_DESCRIPTION, NbBundle.getBundle(ImageViewer.class).getString("LBL_CustomZoom"));
        toolBar.add (button = getZoomButton ());
        toolbarButtons.add(button);
        toolBar.addSeparator(new Dimension(11, 0));
        toolBar.add(button = getGridButton());
        toolbarButtons.add(button);

        for (Iterator it = toolbarButtons.iterator(); it.hasNext(); ) {
            ((JButton) it.next()).setFocusable(false);
        }

        return toolBar;
    }
    
    /** Updates the name and tooltip of this top component according to associated data object. */
    private void updateName () {
        // update name
        String name = storedObject.getNodeDelegate().getDisplayName();
        setName(name);
        // update tooltip
        FileObject fo = storedObject.getPrimaryFile();
        setToolTipText(FileUtil.getFileDisplayName(fo));
    }

    /** Docks the table into the workspace if top component is valid.
     *  (Top component may become invalid after deserialization)
     */
    public void open(Workspace workspace){
        if (discard()) return;

        Workspace realWorkspace = (workspace == null)
                                  ? WindowManager.getDefault().getCurrentWorkspace()
                                  : workspace;
        dockIfNeeded(realWorkspace);
        boolean modeVisible = false;
        TopComponent[] tcArray = editorMode(realWorkspace).getTopComponents();
        for (int i = 0; i < tcArray.length; i++) {
            if (tcArray[i].isOpened(realWorkspace)) {
                modeVisible = true;
                break;
            }
        }
        if (!modeVisible) {
            openOtherEditors(realWorkspace);
        }
        super.open(workspace);
        openOnOtherWorkspaces(realWorkspace);
    }
    
    /**
     */
    protected String preferredID() {
        return getClass().getName();
    }

    private void superOpen(Workspace workspace) {
        super.open(workspace);
    }


    /** Utility method, opens this top component on all workspaces
     * where editor mode is visible and which differs from given
     * workspace.  */
    private void openOnOtherWorkspaces(Workspace workspace) {
        Workspace[] workspaces = WindowManager.getDefault().getWorkspaces();
        Mode curEditorMode = null;
        Mode tcMode = null;
        for (int i = 0; i < workspaces.length; i++) {
            // skip given workspace
            if (workspaces[i].equals(workspace)) {
                continue;
            }
            curEditorMode = workspaces[i].findMode(CloneableEditorSupport.EDITOR_MODE);
            tcMode = workspaces[i].findMode(this);
            if (
                !isOpened(workspaces[i]) &&
                curEditorMode != null &&
                (
                    tcMode == null ||
                    tcMode.equals(curEditorMode)
                )
            ) {
                // candidate for opening, but mode must be already visible
                // (= some opened top component in it)
                TopComponent[] tcArray = curEditorMode.getTopComponents();
                for (int j = 0; j < tcArray.length; j++) {
                    if (tcArray[j].isOpened(workspaces[i])) {
                        // yep, open this top component on found workspace too
                        pureOpen(this, workspaces[i]);
                        break;
                    }
                }
            }
        }
    }

    /** Utility method, opens top components which are opened
     * in editor mode on some other workspace.
     * This method should be called only if first top component is
     * being opened in editor mode on given workspace  */
    private void openOtherEditors(Workspace workspace) {
        // choose candidates for opening
        Set topComps = new HashSet(15);
        Workspace[] wsArray = WindowManager.getDefault().getWorkspaces();
        Mode curEditorMode = null;
        TopComponent[] tcArray = null;
        for (int i = 0; i < wsArray.length; i++) {
            curEditorMode = wsArray[i].findMode(CloneableEditorSupport.EDITOR_MODE);
            if (curEditorMode != null) {
                tcArray = curEditorMode.getTopComponents();
                for (int j = 0; j < tcArray.length; j++) {
                    if (tcArray[j].isOpened(wsArray[i])) {
                        topComps.add(tcArray[j]);
                    }
                }
            }
        }
        // open choosed candidates
        for (Iterator iter = topComps.iterator(); iter.hasNext(); ) {
            pureOpen((TopComponent)iter.next(), workspace);
        }
    }
        
    /** Utility method, calls super version of open if given
     * top component is of Editor type, or calls regular open otherwise.
     * The goal is to prevent from cycle open call between
     * Editor top components  */
    private void pureOpen(TopComponent tc,Workspace workspace) {
        if (tc instanceof ImageViewer) {
            ((ImageViewer)tc).dockIfNeeded(workspace);
            ((ImageViewer)tc).superOpen(workspace);
        } else {
            tc.open(workspace);
        }
    }

    /** Dock this top component to editor mode if it is not docked
     * in some mode at this time  */
    private void dockIfNeeded(Workspace workspace) {
        // dock into editor mode if possible
        Mode ourMode = workspace.findMode(this);
        if (ourMode == null) {
            editorMode(workspace).dockInto(this);
        }
    }

    private Mode editorMode(Workspace workspace) {
        Mode ourMode = workspace.findMode(this);
        if (ourMode == null) {
            ourMode = workspace.createMode(
                          CloneableEditorSupport.EDITOR_MODE, getName(),
                          CloneableEditorSupport.class.getResource(
                              "/org/openide/resources/editorMode.gif" // NOI18N
                          )
                      );
        }
        return ourMode;
    }
    
    /** Gets HelpContext. */
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
        
    /** This component should be discarded if the associated environment
     *  is not valid.
     */
    private boolean discard () {
        return storedObject == null;
    }

    protected boolean closeLast() {
        ((ImageOpenSupport)storedObject.getCookie(ImageOpenSupport.class)).lastClosed();
        return true;
    }

    /** Serialize this top component. Serializes its data object in addition
     * to common superclass behaviour.
     * @param out the stream to serialize to
     */
    public void writeExternal (ObjectOutput out)
    throws IOException {
        super.writeExternal(out);
        out.writeObject(storedObject);
    }
    
    /** Deserialize this top component.
     * Reads its data object and initializes itself in addition
     * to common superclass behaviour.
     * @param in the stream to deserialize from
     */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        storedObject = (ImageDataObject)in.readObject();
        // to reset the listener for FileObject changes
        ((ImageOpenSupport)storedObject.getCookie(ImageOpenSupport.class)).prepareViewer();
        initialize(storedObject);
    }
    
    /** Creates cloned object which uses the same underlying data object. */
    protected CloneableTopComponent createClonedObject () {
        return new ImageViewer(storedObject);
    }
    
    /** Overrides superclass method. Gets actions for this top component. */
    public SystemAction[] getSystemActions() {
        SystemAction[] oldValue = super.getSystemActions();
        SystemAction fsa = null;
        try {
            ClassLoader l = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = getClass().getClassLoader();
            }
            Class c = Class.forName("org.openide.actions.FileSystemAction", true, l).asSubclass(SystemAction.class); // NOI18N
            fsa = (SystemAction) SystemAction.findObject(c, true);
        } catch (Exception ex) {
            // there are no filesystem actions
        }

        return SystemAction.linkActions(new SystemAction[] {
            SystemAction.get(ZoomInAction.class),
            SystemAction.get(ZoomOutAction.class),
            SystemAction.get(CustomZoomAction.class),
            fsa,
            null},
            oldValue);
    }
    
    /** Overrides superclass method. Gets <code>Icon</code>. */
    public Image getIcon () {
        return ImageUtilities.loadImage("org/netbeans/modules/image/imageObject.png"); // NOI18N
    }
    
    /** Draws zoom in scaled image. */
    public void zoomIn() {
        scaleIn();
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Draws zoom out scaled image. */
    public void zoomOut() {
        double oldScale = scale;
        
        scaleOut();
        
         // You can't still make picture smaller, but bigger why not?
        if(!isNewSizeOK()) {
            scale = oldScale;
            
            return;
        }
        
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Resizes panel. */
    private void resizePanel() {
        panel.setPreferredSize(new Dimension(
            (int)(getScale () * storedImage.getIconWidth ()),
            (int)(getScale () * storedImage.getIconHeight()))
        );
        panel.revalidate();
    }
    
    /** Tests new size of image. If image is smaller than  minimum
     *  size(1x1) zooming will be not performed.
     */
    private boolean isNewSizeOK() {
        if (((getScale () * storedImage.getIconWidth ()) > 1) &&
            ((getScale () * storedImage.getIconWidth ()) > 1)
        ) return true;
        return false;
    }
    
    /** Perform zoom with specific proportion.
     * @param fx numerator for scaled
     * @param fy denominator for scaled
     */
    public void customZoom(int fx, int fy) {
        double oldScale = scale;
        
        scale = (double)fx/(double)fy;
        if(!isNewSizeOK()) {
            scale = oldScale;
            
            return;
        }
        
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Return zooming factor.*/
    private double getScale () {
        return scale;
    }
    
    /** Change proportion "out"*/
    private void scaleOut() {
        scale = scale / changeFactor;
    }
    
    /** Change proportion "in"*/
    private void scaleIn() {
        double oldComputedScale = getScale ();
        
        scale = changeFactor * scale;
        
        double newComputedScale = getScale();
        
        if (newComputedScale == oldComputedScale)
            // Has to increase.
            scale = newComputedScale + 1.0D;
    }
    
    /** Gets zoom button. */
    private JButton getZoomButton(final int xf, final int yf) {
        // PENDING buttons should have their own icons.
        JButton button = new JButton(""+xf+":"+yf); // NOI18N
        if (xf < yf)
            button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomOut") + " " + xf + " : " + yf);
        else
            button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomIn") + " " + xf + " : " + yf);
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_Zoom_BTN"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customZoom(xf, yf);
            }
        });
        
        return button;
    }
    
    private JButton getZoomButton() {
        // PENDING buttons should have their own icons.
        JButton button = new JButton(NbBundle.getBundle(CustomZoomAction.class).getString("LBL_XtoY")); // NOI18N
        button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_CustomZoom"));
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_Zoom_BTN"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                CustomZoomAction sa = (CustomZoomAction) SystemAction.get(CustomZoomAction.class);
                sa.performAction ();
            }
        });
        
        return button;
    }
    
    /** Gets grid button.*/
    private JButton getGridButton() {
        // PENDING buttons should have their own icons.
        final JButton button = new JButton(" # "); // NOI18N
        button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ShowHideGrid"));
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_Grid_BTN"));
        button.setMnemonic(NbBundle.getBundle(ImageViewer.class).getString("ACS_Grid_BTN_Mnem").charAt(0));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showGrid = !showGrid;
                panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
            }
        });
        
        return button;
    }
    
}
