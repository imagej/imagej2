package ijx.plugin.frame;

import ijx.gui.IjxWindow;
import ij.plugin.PlugIn;
import java.awt.event.FocusListener;
import java.awt.event.WindowListener;

/**
 *
 * @author GBH
 */
public interface IjxPluginFrame extends IjxWindow, PlugIn, FocusListener, WindowListener {

}
