package imagej.plugin;

public class PluginEntry {

	private String pluginClass;
	private String parentMenu;
	private String label;
	private String args;
	private String menu;
	
	public void setPluginClass(String pluginClass) {
		this.pluginClass = pluginClass;
	}
	
	public String getPluginClass() {
		return pluginClass;
	}
	
	public void setParentMenu( String parentMenu ) 
	{
		this.parentMenu = parentMenu;
	}
	
	public String getParentMenu() {
		return parentMenu;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setArgs(String args) {
		this.args = args;
	}
	
	public String getArgs() {
		return args;
	}
	
	public void setMenu(String menu) {
		this.menu = menu;
	}
	
	public String getMenu() {
		return menu;
	}
	
	public PluginEntry( String pluginClass, String parentMenu, String label )
	{
		this.pluginClass = pluginClass;
		this.parentMenu = parentMenu;
		this.label = label;
		stripArgsFromClass();
	}
	
	/**
	 * Used to represent IJ1 plugin meta information
	 * @param pluginClass - full class name
	 * @param label - the menu label in IJ 1.43u
	 */
	public PluginEntry(String pluginClass, String label) {
		this.pluginClass = pluginClass;
		this.parentMenu = "";
		this.label = label;
		stripArgsFromClass();
	}
	// Strips the String between the "" which is what IJ uses
	// as arguments.
	private void stripArgsFromClass()
	{
		if(this.pluginClass.contains("\""))
		{
			int firstInstance = this.pluginClass.indexOf('\"');
			int secondInstance = this.pluginClass.indexOf('\"', firstInstance + 1);
			this.args = this.pluginClass.substring(firstInstance + 1, secondInstance);
			//System.out.println("The args are " + this.args + " for string " + this.pluginClass + " at index " + firstInstance + " " + secondInstance);
			this.pluginClass = this.pluginClass.replace('\"' + this.args + '\"', "");
		}
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(pluginClass);
		sb.append(" [menu = ");
		sb.append(parentMenu);
		sb.append(" > ");		
		sb.append(label);
		sb.append("]");
		return sb.toString();
	}
	
}
