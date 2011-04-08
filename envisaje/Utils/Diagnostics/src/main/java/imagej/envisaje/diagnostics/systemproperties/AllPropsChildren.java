/*
 * AllPropsChildren.java
 *
 * Created on June 13, 2005, 2:07 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package imagej.envisaje.diagnostics.systemproperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Administrator
 */
public class AllPropsChildren extends Children.Keys {

	private ChangeListener listener;

	protected void addNotify() {
		refreshList();
		PropertiesNotifier.addChangeListener(listener = new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				refreshList();
			}
		});
	}

	protected void removeNotify() {
		if (listener != null) {
			PropertiesNotifier.removeChangeListener(listener);
			listener = null;
		}
		setKeys(Collections.EMPTY_SET);
	}

	protected Node[] createNodes(Object key) {
		return new Node[]{new OnePropNode((String) key)};
	}

	private void refreshList() {
		List keys = new ArrayList();
		Properties p = System.getProperties();
		Enumeration e = p.propertyNames();
		while (e.hasMoreElements()) {
			keys.add(e.nextElement());
		}
		Collections.sort(keys);
		setKeys(keys);
	}
}
