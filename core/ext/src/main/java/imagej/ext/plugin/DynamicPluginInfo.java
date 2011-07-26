//
// DynamicPluginInfo.java
//

package imagej.ext.plugin;

import imagej.ImageJ;
import imagej.ext.MenuPath;
import imagej.ext.UIDetails;
import imagej.ext.module.AbstractModuleInfo;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleException;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleItem;

/**
 * Helper class for maintaining a {@link DynamicPlugin}'s associated
 * {@link ModuleInfo}.
 * <p>
 * The {@link PluginService} has a plain {@link PluginInfo} object in its index,
 * populated from the {@link DynamicPlugin}'s @{@link Plugin} annotation. So
 * this class adapts that object, delegating to it for the {@link UIDetails}
 * methods. The plain {@link PluginInfo} cannot be used as-is, however, because
 * we need to override the {@link ModuleInfo} methods as well as provide new
 * functionality such as {@link AbstractModuleInfo#addInput(ModuleItem)}.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class DynamicPluginInfo extends AbstractModuleInfo {

	private DynamicPlugin module;
	private PluginInfo<? extends DynamicPlugin> info;

	protected DynamicPluginInfo() {
		super(null);
	}

	// -- Internal methods --

	protected void setModule(final DynamicPlugin module) {
		this.module = module;
		final PluginService pluginService = ImageJ.get(PluginService.class);
		info = pluginService.getPluginsOfClass(module.getClass()).get(0);
	}

	// -- ModuleInfo methods --

	@Override
	public String getDelegateClassName() {
		return module.getClass().getName();
	}

	@Override
	public Module createModule() throws ModuleException {
		throw new ModuleException("Unsupported operation");
	}

	// -- UIDetails methods --

	@Override
	public String getTitle() {
		return info.getTitle();
	}

	@Override
	public MenuPath getMenuPath() {
		return info.getMenuPath();
	}

	@Override
	public String getIconPath() {
		return info.getIconPath();
	}

	@Override
	public int getPriority() {
		return info.getPriority();
	}

	@Override
	public boolean isSelectable() {
		return info.isSelectable();
	}

	@Override
	public String getSelectionGroup() {
		return info.getSelectionGroup();
	}

	@Override
	public boolean isSelected() {
		return info.isSelected();
	}

	@Override
	public boolean isEnabled() {
		return info.isEnabled();
	}

	@Override
	public void setMenuPath(final MenuPath menuPath) {
		info.setMenuPath(menuPath);
	}

	@Override
	public void setIconPath(final String iconPath) {
		info.setIconPath(iconPath);
	}

	@Override
	public void setPriority(final int priority) {
		info.setPriority(priority);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		info.setEnabled(enabled);
	}

	@Override
	public void setSelectable(final boolean selectable) {
		info.setSelectable(selectable);
	}

	@Override
	public void setSelectionGroup(final String selectionGroup) {
		info.setSelectionGroup(selectionGroup);
	}

	@Override
	public void setSelected(final boolean selected) {
		info.setSelected(selected);
	}

	// -- BasicDetails methods --

	@Override
	public String getName() {
		return info.getName();
	}

	@Override
	public String getLabel() {
		return info.getLabel();
	}

	@Override
	public String getDescription() {
		return info.getDescription();
	}

	@Override
	public void setName(final String name) {
		info.setName(name);
	}

	@Override
	public void setLabel(final String label) {
		info.setLabel(label);
	}

	@Override
	public void setDescription(final String description) {
		info.setDescription(description);
	}

}
