//
// ImageJModule.java
//

package imagej.ext.plugin;

import imagej.ext.module.AbstractModule;
import imagej.ext.module.AbstractModuleInfo;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleException;

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
