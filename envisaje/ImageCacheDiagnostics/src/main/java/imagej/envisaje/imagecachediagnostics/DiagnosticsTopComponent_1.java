/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.imagecachediagnostics;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
final class DiagnosticsTopComponent_1 extends TopComponent {

    private static DiagnosticsTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";

    private static final String PREFERRED_ID = "DiagnosticsTopComponent";

    private DiagnosticsTopComponent_1() {
        initComponents();
        setName(NbBundle.getMessage(DiagnosticsTopComponent.class, "CTL_DiagnosticsTopComponent"));
        setToolTipText(NbBundle.getMessage(DiagnosticsTopComponent.class, "HINT_DiagnosticsTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        setLayout (new BorderLayout());
        add (new CachePanel(), BorderLayout.CENTER);
        
        setDisplayName ("Image Cache Diagnostics");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized DiagnosticsTopComponent getDefault() {
        if (instance == null) {
            instance = new DiagnosticsTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DiagnosticsTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DiagnosticsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DiagnosticsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DiagnosticsTopComponent) {
            return (DiagnosticsTopComponent) win;
        }
        Logger.getLogger(DiagnosticsTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
    // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
    // TODO add custom code on component closing
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return DiagnosticsTopComponent.getDefault();
        }
    }
}
