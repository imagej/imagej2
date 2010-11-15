package org.imagejdev.sandbox.actioncontext;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
//import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
//import org.openide.windows.TopComponent;
//import org.openide.windows.WindowManager;
//import org.vehicle.domain.Vehicle;

public class InstallVehicleAction extends AbstractAction
        implements ContextAwareAction, LookupListener, Presenter.Toolbar {

    private Lookup.Result<Vehicle> result;
    private JButton toolbarBtn;

    public InstallVehicleAction() {
        this(Utilities.actionsGlobalContext());
    }

    public InstallVehicleAction(Lookup context) {
        super("Install Vehicle...",
                new ImageIcon(ImageUtilities.loadImage("/org/vehicle/actions/Orange Ball.png")));
        //Get the Vehicle from the Lookup:
        result = context.lookupResult(Vehicle.class);
        result.addLookupListener(this);
        resultChanged(new LookupEvent(result));
        //Use this to completely remove the menu when disabled:
//        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (null != result && 0 < result.allInstances().size()) {
            Vehicle v = result.allInstances().iterator().next();
            if (null != v) {
                //Do something to install the vehicle...
                System.out.println("Installed: " + v.getVehicleName());
//                DialogDisplayer.getDefault().notify(
//                        new NotifyDescriptor.Message("Installed: " + v.getVehicleName()));
            }
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new InstallVehicleAction(context);
    }

    //This comes from the Toolbar.Presenter, which we need
    //to access the toolbar button so that we can enable/disable it,
    //note that below it is disabled by default,
    //enabled only in the ResultChanged:
    @Override
    public Component getToolbarPresenter() {
        if (null == toolbarBtn) {
            toolbarBtn = new JButton(this);
        }
        toolbarBtn.setEnabled(false);
        return toolbarBtn;
    }

    @Override
    public void resultChanged(LookupEvent arg0) {
        //Enable the action and the button
        //based on current availability & standby:
        if (result.allInstances().size() > 0) {
            Vehicle v = result.allInstances().iterator().next();
            setEnabled(v.isAvailable() && v.isStandby());
            if (null != toolbarBtn) {
                toolbarBtn.setEnabled(v.isAvailable() && v.isStandby());
            }
        }
        //Activate the VehicleView window, to make it the current window,
        //so that a change in the Properties window causes an immediate
        //update of the menu item and toolbar button:
//        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
//            @Override
//            public void run() {
//                TopComponent tc = WindowManager.getDefault().findTopComponent("VehicleView");
//                tc.open();
//                tc.requestActive();
//            }
//        });
    }

    public static void main(String[] args) {
        List<Vehicle> vehs = new ArrayList<Vehicle>();
        createKeys(vehs);


    }

    protected static boolean createKeys(List<Vehicle> toPopulate) {
       Vehicle v1 = new Vehicle("Boat", true, false);
       Vehicle v2 = new Vehicle("Ship", false, true);
       Vehicle v3 = new Vehicle("Raft", true, true);
       toPopulate.add(v1);
       toPopulate.add(v2);
       toPopulate.add(v3);
       return true;
    }
}
