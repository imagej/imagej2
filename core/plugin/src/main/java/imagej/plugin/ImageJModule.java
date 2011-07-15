//
// ImageJModule.java
//

package imagej.plugin;

import imagej.module.AbstractModule;
import imagej.module.AbstractModuleInfo;
import imagej.module.Module;
import imagej.module.ModuleException;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public abstract class ImageJModule extends AbstractModule implements
	ImageJPlugin
{

	private ImageJModuleInfo info;

	public ImageJModule() {
		super(new ImageJModuleInfo());
		info = (ImageJModuleInfo) super.getInfo();
		info.setDelegateClassName(getClass().getName());
	}

	@Override
	public ImageJModuleInfo getInfo() {
		return info;
	}

	public static class ImageJModuleInfo extends AbstractModuleInfo {

		private String delegateClassName;

		public ImageJModuleInfo() {
			super(null);
		}

		protected void setDelegateClassName(final String delegateClassName) {
			this.delegateClassName = delegateClassName;
		}

		@Override
		public String getDelegateClassName() {
			return delegateClassName;
		}

		@Override
		public Module createModule() throws ModuleException {
			throw new ModuleException("Cannot instantiate ImageJModule");
		}

	}

}
