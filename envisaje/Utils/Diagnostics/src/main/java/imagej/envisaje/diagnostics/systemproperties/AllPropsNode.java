/*
 * AllPropsNode.java
 *
 * Created on June 13, 2005, 1:20 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package imagej.envisaje.diagnostics.systemproperties;

import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.NewAction;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;


import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Administrator
 */
public class AllPropsNode extends AbstractNode {

	private static ResourceBundle bundle = NbBundle.getBundle(AllPropsNode.class);

	public AllPropsNode() {
		super(new AllPropsChildren());
		setIconBase("org/myorg/systemproperties/allPropsIcon");
		setName("AllPropsNode");
		setDisplayName(bundle.getString("LBL_AllPropsNode"));
		setShortDescription(bundle.getString("HINT_AllPropsNode"));
	}

	public Action[] getActions(boolean context) {
		Action[] result = new Action[]{
			SystemAction.get(RefreshPropsAction.class),
			null,
			SystemAction.get(OpenLocalExplorerAction.class),
			null,
			SystemAction.get(NewAction.class),
			null,
			SystemAction.get(ToolsAction.class),
			SystemAction.get(PropertiesAction.class),};
		return result;
	}

	public HelpCtx getHelpCtx() {
		return new HelpCtx("org.myorg.systemproperties");
	}

	public Node cloneNode() {
		return new AllPropsNode();
	}

	public NewType[] getNewTypes() {
		return new NewType[]{new NewType() {

				public String getName() {
					return bundle.getString("LBL_NewProp");
				}

				public HelpCtx getHelpCtx() {
					return new HelpCtx("org.myorg.systemproperties");
				}

				public void create() throws IOException {
					String title = bundle.getString("LBL_NewProp_dialog");
					String msg = bundle.getString("MSG_NewProp_dialog_key");
					NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(msg, title);
					DialogDisplayer.getDefault().notify(desc);
					String key = desc.getInputText();
					if ("".equals(key)) {
						return;
					}
					msg = bundle.getString("MSG_NewProp_dialog_value");
					desc = new NotifyDescriptor.InputLine(msg, title);
					DialogDisplayer.getDefault().notify(desc);
					String value = desc.getInputText();
					System.setProperty(key, value);
					PropertiesNotifier.changed();
				}
			},
		new NewType() {

				public String getName() {
					return bundle.getString("LBL_NewProp");
				}

				public HelpCtx getHelpCtx() {
					return new HelpCtx("org.myorg.systemproperties");
				}

				public void create() throws IOException {
					String title = bundle.getString("LBL_NewProp_dialog");
					String msg = bundle.getString("MSG_NewProp_dialog_key");
					NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(msg, title);
					DialogDisplayer.getDefault().notify(desc);
					String key = desc.getInputText();
					if ("".equals(key)) {
						return;
					}
					msg = bundle.getString("MSG_NewProp_dialog_value");
					desc = new NotifyDescriptor.InputLine(msg, title);
					DialogDisplayer.getDefault().notify(desc);
					String value = desc.getInputText();
					System.setProperty(key, value);
					PropertiesNotifier.changed();
				}
			}};
	}
}
