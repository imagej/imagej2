package imagej.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginEntry {

	private String pluginClass;
	private List<String> menuPath;
	private String label;
	private String arg;

	/**
	 * Used to represent IJ1 plugin meta information
	 * @param pluginClass - full class name
	 * @param label - the menu label in IJ 1.43u
	 */
	public PluginEntry(String pluginClass, String label) {
		this(pluginClass, new ArrayList<String>(), label, "");
	}

	public PluginEntry(String pluginClass, List<String> menuPath,
		String label, String arg)
	{
		this.pluginClass = pluginClass;
		this.menuPath = menuPath;
		this.label = label;
		this.arg = arg;
	}

	public void setPluginClass(String pluginClass) {
		this.pluginClass = pluginClass;
	}

	public String getPluginClass() {
		return pluginClass;
	}

	public void setMenuPath(List<String> menuPath) {
		this.menuPath = menuPath;
	}

	public List<String> getMenuPath() {
		return menuPath;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public String getArg() {
		return arg;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(pluginClass);
		sb.append(" [menu = ");
		for (String menu : menuPath) {
			sb.append(menu);
			sb.append(" > ");
		}
		sb.append(label);
		sb.append("]");
		return sb.toString();
	}

}
