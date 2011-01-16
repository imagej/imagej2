package imagej.plugin;

public class MenuEntry {

	private String name;
	private double weight = Double.POSITIVE_INFINITY;
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

	@Override
	public String toString() {
		return name;
	}

}
