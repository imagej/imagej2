package calvin;

import imagej.updater.core.AbstractUploader;
import imagej.updater.core.Uploadable;
import imagej.updater.core.Uploader;

import java.util.List;

import org.scijava.plugin.Plugin;

/**
 * Dummy uploader.
 * 
 * The only purpose this uploader has is to verify that ImageJ's updater
 * supports auto-installing uploaders given their protocol.
 *
 * @author Johannes Schindelin
 */
@Plugin(type = Uploader.class)
public class HobbesUploader extends AbstractUploader {
	@Override
	public String getProtocol() {
		return "hobbes";
	}

	@Override
	public void upload(List<Uploadable> files, List<String> locks) {
		throw new UnsupportedOperationException();
	}
}
