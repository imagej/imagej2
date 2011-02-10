package imagej.plugin.gui;

public interface MenuCreator<T> {

	void createMenus(ShadowMenu root, T target);

}
