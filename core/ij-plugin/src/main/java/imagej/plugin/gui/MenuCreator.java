package imagej.plugin.gui;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public interface MenuCreator<T> {

	void createMenus(ShadowMenu root, T target);

}
