package fiji.build.minimaven;

public class Coordinate {
	protected String groupId, artifactId, version, systemPath, classifier, scope, snapshotVersion;
	protected boolean optional;

	public Coordinate() {}

	public Coordinate(String groupId, String artifactId, String version) {
		this(groupId, artifactId, version, null, false, null, null);
	}

	public Coordinate(String groupId, String artifactId, String version, String scope, boolean optional, String systemPath, String classifier) {
		this.groupId = normalize(groupId);
		this.artifactId = normalize(artifactId);
		this.version = normalize(version);
		this.scope = normalize(scope);
		this.optional = optional;
		this.systemPath = normalize(systemPath);
		this.classifier = classifier;
	}

	public String normalize(String s) {
		return "".equals(s) ? null : s;
	}

	public String getJarName() {
		return getJarName(false);
	}

	public String getJarName(boolean withProjectPrefix) {
		return getFileName(withProjectPrefix, true, "jar");
	}

	public String getPOMName() {
		return getPOMName(false);
	}

	public String getPOMName(boolean withProjectPrefix) {
		return getFileName(withProjectPrefix, false, "pom");
	}

	public String getFileName(boolean withClassifier, String fileExtension) {
		return getFileName(false, withClassifier, fileExtension);
	}

	public String getFileName(boolean withProjectPrefix, boolean withClassifier, String fileExtension) {
		return (withProjectPrefix ? groupId + "/" : "")
			+ artifactId + "-" + getVersion()
			+ (withClassifier && classifier != null ? "-" + classifier : "")
			+ (fileExtension != null ? "." + fileExtension : "");
	}

	public String getKey() {
		return groupId + ">" + artifactId + (classifier == null ? "" : ">" + classifier);
	}

	public void setSnapshotVersion(String version) {
		snapshotVersion = version;
	}

	public String getVersion() {
		return snapshotVersion != null ? snapshotVersion : version;
	}

	@Override
	public String toString() {
		String extra = "";
		if (optional)
			extra += " optional";
		if (scope != null)
			extra += " scope=" + scope;
		if (extra.startsWith(" "))
			extra = "{" + extra.substring(1) + "}";
		return getFileName(true, true, null) + extra;
	}
}
