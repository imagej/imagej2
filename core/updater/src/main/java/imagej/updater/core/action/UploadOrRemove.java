package imagej.updater.core.action;

import imagej.updater.core.FileObject;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection;
import imagej.updater.core.GroupAction;

import java.util.Collection;

public class UploadOrRemove implements GroupAction {

	private String updateSite;

	public UploadOrRemove(final String updateSite) {
		this.updateSite = updateSite;
	}

	@Override
	public boolean isValid(FilesCollection files, FileObject file) {
		final Status status = file.getStatus();
		if (!updateSite.equals(file.updateSite)) {
			if (status == Status.LOCAL_ONLY) return true;
			return false;
		}
		if (!status.isValid(Action.UPLOAD) && !status.isValid(Action.REMOVE)) return false;
		final Collection<String> sites = files.getSiteNamesToUpload();
		return sites.size() == 0 || sites.contains(updateSite);
	}

	@Override
	public void setAction(FilesCollection files, FileObject file) {
		file.updateSite = updateSite;
		file.setFirstValidAction(files, Action.UPLOAD, Action.REMOVE);
	}

	@Override
	public String getLabel(FilesCollection files, Iterable<FileObject> selected) {
		boolean upload = false, remove = false;
		for (final FileObject file : selected) {
			final Status status = file.getStatus();
			upload = upload || status.isValid(Action.UPLOAD);
			remove = remove || status.isValid(Action.REMOVE);
		}
		if (!(upload ^ remove)) return "Upload to " + updateSite + " / Mark Obsolete";
		return remove ? "Mark Obsolete (" + updateSite + ")": ("Upload to " + updateSite);
	}

	@Override
	public String toString() {
		return "Upload to " + updateSite + " / Mark Obsolete";
	}

}
