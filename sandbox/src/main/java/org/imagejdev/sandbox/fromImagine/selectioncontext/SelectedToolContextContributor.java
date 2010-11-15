

package org.imagejdev.sandbox.fromImagine.selectioncontext;


// from package org.netbeans.paint.toolsui;

import java.util.Collections;
//import net.dev.java.imagine.spi.tools.Tool;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Timothy Boudreau
 */
public class SelectedToolContextContributor implements SelectionContextContributor {
    private InstanceContent content = new InstanceContent();
    private AbstractLookup lkp = new AbstractLookup (content);

    private static SelectedToolContextContributor INSTANCE = null;

    public SelectedToolContextContributor() {
	INSTANCE = this;
    }

    public Lookup getLookup() {
        return lkp;
    }

    static void setSelectedTool (Tool tool) {
	ensureCreated();
        INSTANCE.content.set(tool == null ? Collections.EMPTY_SET :
            Collections.singleton(tool), null);
    }

    private static void ensureCreated() {
        if (true) {
            return;
        }
	if (INSTANCE == null) {
	    Object o =
                Lookup.getDefault().lookup(SelectionContextContributor.class);
	    if (INSTANCE == null) {
		ErrorManager.getDefault().notify (new IllegalStateException(
			"Implementation of " +
			"SelectedToolLookupProvider not found in default lookup." +
			" Check the META-INF/services directory of ToolsUI," +
			" and make sure it provides a Lookup.Provider.  Probably" +
			" tool selection is broken for the application."));
		INSTANCE = new SelectedToolContextContributor();
	    }
	}
    }
}
