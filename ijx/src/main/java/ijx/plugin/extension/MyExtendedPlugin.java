package ijx.plugin.extension;

import ijx.plugin.parameterized.AbstractPlugIn;
import javax.swing.Action;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author GBH
 */
public class MyExtendedPlugin extends AbstractPlugIn implements ExtendedPlugin {
//protected String label;
//protected String name;
//protected String description;
//protected String URL;
//protected Action[] actions;
    public String getLabel() {
        return "MyPlug";
    }

    public String getName() {
        return "My Plugin, its cool";
    }

    public String getDescription() {
        return "The description of this incredible plugin";
    }

    public Action[] getActions() {
        return null;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); // @todo
    }

    public String getHelp() {
        return ""
                + "";
    }

    public String getURL() {
        return "http://www.hoseheads.org";
    }
}
