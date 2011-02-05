package imagej.plugin.api;

import java.util.List;

public class PluginEntry {

	private String pluginClass;
	private List<MenuEntry> menuPath;
	private String arg;

	public PluginEntry(String pluginClass, List<MenuEntry> menuPath, String arg) {
		this.pluginClass = pluginClass;
		this.menuPath = menuPath;
		this.arg = arg;
	}

	public void setPluginClass(String pluginClass) {
		this.pluginClass = pluginClass;
	}

	public String getPluginClass() {
		return pluginClass;
	}

	public void setMenuPath(List<MenuEntry> menuPath) {
		this.menuPath = menuPath;
	}

	public List<MenuEntry> getMenuPath() {
		return menuPath;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public String getArg() {
		return arg;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(pluginClass);
		sb.append(" [");
		boolean firstField = true;

		if (arg != null && !arg.isEmpty()) {
			if (firstField) firstField = false;
			else sb.append("; ");
			sb.append("arg = \"");
			sb.append(arg);
			sb.append("\"");
		}
		
		if (menuPath != null && !menuPath.isEmpty()) {
			if (firstField) firstField = false;
			else sb.append("; ");
			sb.append("menu = ");
			boolean firstMenu = true;
			for (final MenuEntry menu : menuPath) {
				if (firstMenu) firstMenu = false;
				else sb.append(" > ");
				sb.append(menu);
			}
			sb.append("]");
		}

		return sb.toString();
	}

}
