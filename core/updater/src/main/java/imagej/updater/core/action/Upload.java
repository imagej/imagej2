/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.updater.core.action;

import imagej.updater.core.FileObject;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection;
import imagej.updater.core.FilesCollection.UpdateSite;
import imagej.updater.core.GroupAction;

import java.util.Collection;
import java.util.Collections;

public class Upload implements GroupAction {

	private String updateSite;

	public Upload(final String updateSite) {
		this.updateSite = updateSite;
	}

    /**
	 * Determines whether a particular set of files can be uploaded to this
	 * update site.
	 * 
	 * <p>
	 * Uploading to a higher-ranked update site is called <i>shadowing</i>. This
	 * table indicates what actions are valid:
	 * <table>
	 * <tr>
	 * <th>&nbsp;</th>
	 * <th>upload</th>
	 * <th>shadow</th>
	 * </tr>
	 * <tr>
	 * <td>INSTALLED</td>
	 * <td>NO</td>
	 * <td>NO</td>
	 * </tr>
	 * <tr>
	 * <td>LOCAL_ONLY</td>
	 * <td>YES</td>
	 * <td>NO</td>
	 * </tr>
	 * <tr>
	 * <td>MODIFIED</td>
	 * <td>YES</td>
	 * <td>RANK</td>
	 * </tr>
	 * <tr>
	 * <td>NEW</td>
	 * <td>NO</td>
	 * <td>NO</td>
	 * </tr>
	 * <tr>
	 * <td>NOT_INSTALLED</td>
	 * <td>NO</td>
	 * <td>NO</td>
	 * </tr>
	 * <tr>
	 * <td>OBSOLETE</td>
	 * <td>YES</td>
	 * <td>RANK</td>
	 * </tr>
	 * <tr>
	 * <td>OBSOLETE_MODIFIED</td>
	 * <td>YES</td>
	 * <td>RANK</td>
	 * </tr>
	 * <tr>
	 * <td>OBSOLETE_UNINSTALLED</td>
	 * <td>NO</td>
	 * <td>NO</td>
	 * </tr>
	 * <tr>
	 * <td>UPDATEABLE</td>
	 * <td>YES</td>
	 * <td>RANK</td>
	 * </tr>
	 * </table>
	 * 
	 * where <i>RANK</i> means that the rank of the update site to upload to
	 * must be greater than the rank of the file's current update site.
	 * </p>
	 */
	@Override
	public boolean isValid(FilesCollection files, FileObject file) {
		final Status status = file.getStatus();
		final boolean canUpload = status.isValid(Action.UPLOAD);

		boolean shadowing = file.updateSite != null && !updateSite.equals(file.updateSite);

		if (!canUpload) return false;

		final Collection<String> sites = files.getSiteNamesToUpload();
		if (sites.size() > 0 && !sites.contains(updateSite)) return false;

		if (shadowing) {
			final UpdateSite shadowingSite = files.getUpdateSite(updateSite);
			final UpdateSite shadowedSite = files.getUpdateSite(file.updateSite);
			if (shadowingSite.rank < shadowedSite.rank) return false;
		}

		return true;
	}

	@Override
	public void setAction(FilesCollection files, FileObject file) {
		if (file.updateSite != null && !file.updateSite.equals(updateSite) &&
				file.originalUpdateSite == null) {
			file.originalUpdateSite = file.updateSite;
		}
		file.updateSite = updateSite;
		file.setAction(files, Action.UPLOAD);
	}

	@Override
	public String getLabel(FilesCollection files, Iterable<FileObject> selected) {
		boolean shadowing = false;
		for (final FileObject file : selected) {
			final Status status = file.getStatus();
			if (status.isValid(Action.UPLOAD)) {
				if (file.updateSite != null && !file.updateSite.equals(updateSite)) {
					shadowing = true;
				}
			}
		}
		return "Upload" + (shadowing ? " (shadowing)" : "") + " to " + updateSite;
	}

	@Override
	public String toString() {
		return getLabel(null, Collections.<FileObject>emptyList());
	}

}
