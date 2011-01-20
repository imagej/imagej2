package imagej.envisaje.toolsui;
import java.awt.EventQueue;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import imagej.envisaje.api.actions.Sensor;
import imagej.envisaje.api.actions.Sensor.Notifiable;
import imagej.envisaje.spi.ToolRegistry;
import imagej.envisaje.spi.tools.Tool;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * Top component which gets all instances of Tool from the default lookup
 * (multiple modules can install tools by putting them in META-INF/services)
 * and displays one JToggleButton for each tool.  When the selected one
 * changes, it will push its associated tool into the lookup in
 * SelectedToolLookupProvider.
 *
 * A paint component listening on the result from this lookup will receive
 * the change and make the newly selected tool active.
 */
public final class ToolsTopComponent extends TopComponent implements Runnable, Notifiable <Tool> {
    private static final long serialVersionUID = 12L;
    private static ToolsTopComponent instance;

    private ToolsTopComponent() {
        Sensor.register(ToolRegistry.getLookup(), Tool.class, this);
        initComponents();
        setName(NbBundle.getMessage(ToolsTopComponent.class, "CTL_ToolsTopComponent")); //NOI18N
        setToolTipText(NbBundle.getMessage(ToolsTopComponent.class, "HINT_ToolsTopComponent")); //NOI18N
        //Winsys hack - don't auto activate this component and send focus
        //away from the editor
        putClientProperty("dontActivate", Boolean.TRUE); //NOI18N
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public void addNotify() {
        super.addNotify();
    }

    public void run() {
        refresh(Lookup.getDefault().lookupAll(Tool.class));
    }

    @Override
    public void open() {
        Mode m = WindowManager.getDefault().findMode("explorer"); //NOI18N

        if (m != null) {
            m.dockInto(this);
        }
        super.open();
    }

    private void initComponents() {
        setLayout(new java.awt.BorderLayout());
    }
    /**
     *
     * Gets default instance. Don't use directly, it reserved for '.settings' file only,
     * i.e. deserialization routines, otherwise you can get non-deserialized instance.
     */
    public static synchronized ToolsTopComponent getDefault() {
        if (instance == null) {
            instance = new ToolsTopComponent();
        }
        return instance;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        EventQueue.invokeLater (this);
    }

    @Override
    public void componentClosed() {
        removeAll();
    }

    @Override
    public Object writeReplace() {
        Tool tool = Utilities.actionsGlobalContext().lookup(Tool.class);
        return new ResolvableHelper(tool);
    }

    @Override
    protected String preferredID() {
        return "ToolsTopComponent"; //NOI18N
    }

    private void refresh(Collection tools) {
        removeAll();
        ButtonGroup group = new ButtonGroup ();
        for (Iterator i=tools.iterator(); i.hasNext();) {
            Tool tool = (Tool) i.next();
            ToolAction action = ToolAction.get (tool);
            AbstractButton btn = action.createButton();
            group.add (btn);
            add (btn);
        }
        invalidate();
        revalidate();
        repaint();
    }

    public void notify(Collection coll, Class target) {
        refresh(coll);
    }

    static final class ResolvableHelper implements java.io.Serializable {
        private static final long serialVersionUID = 2L;
        private final String tool;
        ResolvableHelper (Tool tool) {
            this.tool = tool.getClass().getName();
        }

        public Object readResolve() {
            ToolsTopComponent result = ToolsTopComponent.getDefault();
            if (tool != null) {
                Collection <? extends Tool> tools = Lookup.getDefault().lookupAll(Tool.class);
                for (Tool t : tools) {
                    if (tool.equals(t.getClass().getName())) {
                        SelectedToolContextContributor.setSelectedTool (t);
                        break;
                    }
                }
            }
            return result;
        }
    }
}
