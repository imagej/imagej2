package imagej.plugin;

import java.lang.reflect.Field;

import imagej.module.ModuleItem;

public class PluginModuleItem implements ModuleItem {

	private Field field;

	public PluginModuleItem(final Field field) {
		this.field = field;
	}

	// -- ModuleItem methods --

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public String getLabel() {
		return getParameter().label();
	}

	@Override
	public Class<?> getType() {
		return field.getClass();
	}

	@Override
	public Object getDefaultValue() {
		// FIXME no way to know default value without instantiating the plugin
		return null;
	}

	// -- Helper methods --

	private Parameter getParameter() {
		return field.getAnnotation(Parameter.class);
	}

}
