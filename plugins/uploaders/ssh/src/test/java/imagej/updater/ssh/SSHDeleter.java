package imagej.updater.ssh;

import static org.junit.Assert.assertTrue;
import imagej.updater.core.AbstractUploaderTestBase.Deleter;

import java.io.IOException;

public class SSHDeleter extends SSHFileUploader implements Deleter {
	private final String host, uploadDirectory;
	private boolean loggedIn;

	public SSHDeleter(final String host, final String uploadDirectory) {
		this.host = host;
		this.uploadDirectory = uploadDirectory + (uploadDirectory.endsWith("/") ? "" : "/");
	}

	@Override
	public void delete(final String file) throws IOException {
		if (!loggedIn) {
			assertTrue(debugLogin(host));
			loggedIn = true;
		}
		final boolean isDirectory = file.endsWith("/");
		setCommand("rm " + (isDirectory ? "-r " : "") + "'" + uploadDirectory + file + "'");
	}
}