package imagej.model;

public class Metadata {

	private String name;
	private AxisLabel[] axes;
	
	public Metadata() {
		this.name = "Untitled";
		this.axes = new AxisLabel[0];
	}
	
	/** Gets the name of dataset. */
	public String getName() { return name; }

	/** Sets the name of dataset. */
	public void setName(final String name) { this.name = name; }

	/** Returns the order of the axes. */
	public AxisLabel[] getAxes() { return axes; }

	/** Sets the order of the axes. */
	public void setAxes(final AxisLabel[] axes) { this.axes = axes; }

	/**
	 * Extracts metadata, including axis types,
	 * from the given encoded image name.
	 */
	public static Metadata createMetadata(String encodedImageName) {
		final String imageName = decodeName(encodedImageName);
		final String[] imageTypes = decodeTypes(encodedImageName);
		final AxisLabel[] axisLabels = new AxisLabel[imageTypes.length];
		for (int i=0; i<imageTypes.length; i++) {
			axisLabels[i] = AxisLabel.getAxisLabel(imageTypes[i]);
		}
		final Metadata md = new Metadata();
		md.setName(imageName);
		md.setAxes(axisLabels);
		return md;
	}

	// CTR TODO - Code below is duplicated from imglib-io ImageOpener class.
	// This functionality should live in a common utility place somewhere instead.

	/** Converts the given image name back to a list of dimensional axis types. */
	public static String decodeName(String name) {
		final int lBracket = name.lastIndexOf(" [");
		return name.substring(0, lBracket);
	}

	/** Converts the given image name back to a list of dimensional axis types. */
	public static String[] decodeTypes(String name) {
		final int lBracket = name.lastIndexOf(" [");
		if (lBracket < 0) return new String[0];
		final int rBracket = name.lastIndexOf("]");
		if (rBracket < lBracket) return new String[0];
		return name.substring(lBracket + 2, rBracket).split(" ");
	}

}
