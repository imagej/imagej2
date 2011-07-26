//
// DynamicPlugin.java
//

package imagej.ext.plugin;

import imagej.ext.module.AbstractModule;
import imagej.ext.module.AbstractModuleInfo;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleException;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleItem;

/**
 * A class which can be extended to provide an ImageJ plugin with a variable
 * number of inputs and outputs. This class provides greater configurability,
 * but also greater complexity, than implementing the {@link ImageJPlugin}
 * interface and using the @ {@link Parameter} annotations on instance fields.
 * 
 * @author Curtis Rueden
 */
public abstract class DynamicPlugin extends AbstractModule implements
	ImageJPlugin
{

	private final DynamicPluginInfo info;

	public DynamicPlugin() {
		super(new DynamicPluginInfo());
		info = (DynamicPluginInfo) super.getInfo();
		info.setModule(this);
	}

	// -- DynamicPlugin methods --

	/** Adds an input to the list. */
	public <T> void addInput(final String name, final Class<T> type) {
		addInput(new DefaultModuleItem<T>(this, name, type));
	}

	/** Adds an input to the list. */
	public void addInput(final ModuleItem<?> input) {
		getInfo().addInput(input);
	}

	/** Adds an output to the list. */
	public <T> void addOutput(final String name, final Class<T> type) {
		addOutput(new DefaultModuleItem<T>(this, name, type));
	}

	/** Adds an output to the list. */
	public void addOutput(final ModuleItem<?> output) {
		getInfo().addOutput(output);
	}

	/** Removes an input from the list. */
	public void removeInput(final ModuleItem<?> input) {
		getInfo().removeInput(input);
	}

	/** Removes an output from the list. */
	public void removeOutput(final ModuleItem<?> output) {
		getInfo().removeOutput(output);
	}

	// -- Module methods --

	@Override
	public DynamicPluginInfo getInfo() {
		return info;
	}

	// -- Helper classes --

	/**
	 * Helper class for maintaining the dynamic plugin's associated
	 * {@link ModuleInfo}.
	 */
	public static class DynamicPluginInfo extends AbstractModuleInfo {

		private DynamicPlugin module;

		public DynamicPluginInfo() {
			super(null);
		}

		// -- Internal methods --

		protected void setModule(final DynamicPlugin module) {
			this.module = module;
			final Plugin plugin = module.getClass().getAnnotation(Plugin.class);
			PluginInfo.populate(this, plugin);
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

	}

}
