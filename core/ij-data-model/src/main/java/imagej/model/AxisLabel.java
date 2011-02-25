package imagej.model;

import java.util.Hashtable;

// TODO - unify this enum with the axis labels used in
// Bio-Formats (loci.formats.FormatTools) and
// imglib-io (mpicbg.imglib.io.ImageOpener).

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public enum AxisLabel {
	X("X"),
	Y("Y"),
	Z("Z"),
	TIME("Time"),
	CHANNEL("Channel"),
	LIFETIME("Lifetime"),
	OTHER("Other");

	private static Hashtable<String, AxisLabel> axisLabels =
		new Hashtable<String, AxisLabel>();

	static {
		for (final AxisLabel axisLabel : AxisLabel.values()) {
			axisLabels.put(axisLabel.getLabel(), axisLabel);
		}
	}

	public static AxisLabel getAxisLabel(String label) {
		return axisLabels.get(label);
	}

	private String label;

	private AxisLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}

}
