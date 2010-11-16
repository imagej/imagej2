package ijx.plugin.extension;


import javax.swing.Action;

/**
 *
 * @author GBH
 */
public interface ExtendedPlugin {

    String getName();

    String getLabel();

    Action[] getActions();

    String getDescription();  // tooltip

    String getHelp();

    String getURL();
}
