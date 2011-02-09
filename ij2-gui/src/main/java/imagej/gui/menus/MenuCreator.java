package imagej.gui.menus;

public interface MenuCreator<T> {

	void createMenus(ShadowMenu root, T target);

}
