package ijx.gui.menu;

import java.awt.Container;
import javax.swing.Action;

/**
 *
 * @author GBH
 */
public interface MenuBuilder {
    void build();
    void setTopMenu(String topMenu);
    void addItem(String menuName, Action action);
    void addSubItem(String menuName, String subMenuName, Action action);
    void removeItem(String menuName);
    Container getMenubar();

}
