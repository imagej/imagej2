package imagej2.plugin;

import java.util.List;

public class PluginEntry {

	private String pluginClass;
	private List<String> parentMenu;
	private String label;
	
	public void setPluginClass(String pluginClass) {
		this.pluginClass = pluginClass;
	}
	public String getPluginClass() {
		return pluginClass;
	}
	public void setParentMenu(List<String> parentMenu) {
		this.parentMenu = parentMenu;
	}
	public List<String> getParentMenu() {
		return parentMenu;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getLabel() {
		return label;
	}
	
	public PluginEntry( String pluginClass, List<String> parentMenu, String label)
	{
		this.pluginClass = pluginClass;
		this.parentMenu = parentMenu;
		this.label = label;
	}
	
	
	
}
