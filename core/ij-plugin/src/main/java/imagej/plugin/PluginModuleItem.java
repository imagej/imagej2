package imagej.plugin;

import java.lang.reflect.Field;

import imagej.module.ModuleItem;

public class PluginModuleItem implements ModuleItem {

	private Field field;
	private Object defaultValue;

	public PluginModuleItem(final Field field, final Object defaultValue) {
		this.field = field;
		this.defaultValue = defaultValue;
	}

	public Field getField() {
		return field;
	}

	public Parameter getParameter() {
		return field.getAnnotation(Parameter.class);
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
		return field.getType();
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

}
