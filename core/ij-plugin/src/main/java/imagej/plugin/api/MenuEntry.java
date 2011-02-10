package imagej.plugin.api;

import imagej.plugin.Menu;

public class MenuEntry {

	private String name;
	private double weight = Menu.DEFAULT_WEIGHT;
	private char mnemonic;
	private String accelerator;
	private String icon;

	public MenuEntry(String name) {
		this.name = name;
	}

	public MenuEntry(String name, double weight) {
		this.name = name;
		this.weight = weight;
	}

	public MenuEntry(String name, double weight,
		char mnemonic, String accelerator, String icon)
	{
		this.name = name;
		this.weight = weight;
		this.mnemonic = mnemonic;
		this.accelerator = accelerator;
		this.icon = icon;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	public void setMnemonic(char mnemonic) {
		this.mnemonic = mnemonic;
	}

	public char getMnemonic() {
		return mnemonic;
	}

	public void setAccelerator(String accelerator) {
		this.accelerator = accelerator;
	}

	public String getAccelerator() {
		return accelerator;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
	}

	/**
	 * Updates any default properties of this menu entry
	 * to match those of the given menu entry.
	 */
	public void assignProperties(MenuEntry entry) {
		if (name == null) name = entry.getName();
		if (weight == Menu.DEFAULT_WEIGHT) weight = entry.getWeight();
		if (mnemonic == '\0') mnemonic = entry.getMnemonic();
		if (accelerator == null) accelerator = entry.getAccelerator();
		if (icon == null) icon = entry.getIcon();
	}

	@Override
	public String toString() {
		return name;
	}

}
