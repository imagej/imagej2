package imagej.plugin;

import java.util.List;

public class PluginEntry {

	private String pluginClass;
	private List<String> menuPath;
	private String label;
	private String arg;

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
		sb.append(" [");
		sb.append("label = ");
		sb.append(label);
		sb.append(", arg = \"");
		sb.append(arg);
		sb.append("\"");
		sb.append(", menu = ");
		boolean first = true;
		for (String menu : menuPath) {
			if (first) first = false;
			else sb.append(" > ");
			sb.append(menu);
		}
		sb.append("]");
		return sb.toString();
	}

}
