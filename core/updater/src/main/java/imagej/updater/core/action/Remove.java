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
import imagej.updater.core.GroupAction;

import java.util.Collection;
import java.util.Collections;

public class Remove implements GroupAction {

	private String updateSite;

	public Remove(final String updateSite) {
		this.updateSite = updateSite;
	}

    /**
	 * Determines whether we can remove (or unshadow) files from a particular
	 * update site.
	 * 
	 * <table>
	 * <tr>
	 * <th>&nbsp;</th>
	 * <th>remove</th>
	 * <th>unshadow</th>
	 * </tr>
	 * <tr>
	 * <td>INSTALLED</td>
	 * <td>NO</td>
	 * <td>OVERRIDES</td>
	 * </tr>
	 * <tr>
	 * <td>LOCAL_ONLY</td>
	 * <td>NO</td>
	 * <td>NO</td>
	 * </tr>
	 * <tr>
	 * <td>MODIFIED</td>
	 * <td>NO</td>
	 * <td>OVERRIDES</td>
	 * </tr>
	 * <tr>
	 * <td>NEW</td>
	 * <td>YES</td>
	 * <td>OVERRIDES</td>
	 * </tr>
	 * <tr>
	 * <td>NOT_INSTALLED</td>
	 * <td>YES</td>>
	 * <td>OVERRIDES</td>
	 * </tr>
	 * <tr>
	 * <td>OBSOLETE</td>
	 * <td>NO</td>
	 * <td>NO</td>
	 * </tr>
	 * <tr>
	 * <td>OBSOLETE_MODIFIED</td>
	 * <td>NO</td>
	 * <td>NO</td>
	 * </tr>
	 * <tr>
	 * <td>OBSOLETE_UNINSTALLED</td>
	 * <td>NO</td>
	 * <td>NO</td>
	 * </tr>
	 * <tr>
	 * <td>UPDATEABLE</td>>
	 * <td>NO</td>
	 * <td>OVERRIDES</td>
	 * </tr>
	 * </table>
	 * 
	 * where <i>OVERRIDES</i> means that the current file must override another
	 * update site's version.
	 */
	@Override
	public boolean isValid(FilesCollection files, FileObject file) {
		final Status status = file.getStatus();
		final boolean canRemove = status.isValid(Action.REMOVE);

		boolean unshadowing = updateSite.equals(file.updateSite) && file.overridesOtherUpdateSite();

		if (!canRemove) {
			if (!unshadowing) return false;
			if (status != Status.INSTALLED && status != Status.MODIFIED && status != Status.UPDATEABLE) {
				return false;
			}
		}

		final Collection<String> sites = files.getSiteNamesToUpload();
		return sites.size() == 0 || sites.contains(updateSite);
	}

	@Override
	public void setAction(FilesCollection files, FileObject file) {
		file.setAction(files, Action.REMOVE);
	}

	@Override
	public String getLabel(FilesCollection files, Iterable<FileObject> selected) {
		boolean unshadowing = false;
		for (final FileObject file : selected) {
			final Status status = file.getStatus();
			if (status.isValid(Action.REMOVE) && file.overridesOtherUpdateSite()) unshadowing = true;
		}
		return "Mark obsolete" + (unshadowing + " (unshadowing)")
				+ " (" + updateSite + ")";
	}

	@Override
	public String toString() {
		return getLabel(null, Collections.<FileObject>emptyList());
	}

}
