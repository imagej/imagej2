
package imagej.updater.core;

public class Dependency {

	public String filename;
	public long timestamp;
	public boolean overrides;

	public Dependency(final String filename, final long timestamp,
		final boolean overrides)
	{
		this.filename = filename;
		this.timestamp = timestamp;
		this.overrides = overrides;
	}

	@Override
	public String toString() {
		return filename;
	}
}
