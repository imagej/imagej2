/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.updater.util;

import java.util.ArrayList;
import java.util.List;

/*
 * This class is the base class for serving Progress instances.  For this
 * reason, it implements the same interface.
 */
public class Progressable implements Progress {

	protected List<Progress> progress;

	public Progressable() {
		progress = new ArrayList<Progress>();
	}

	public void addProgress(final Progress progress) {
		this.progress.add(progress);
	}

	public void removeProgress(final Progress progress) {
		this.progress.remove(progress);
	}

	@Override
	public void setTitle(final String title) {
		for (final Progress progress : this.progress)
			progress.setTitle(title);
	}

	@Override
	public void setCount(final int count, final int total) {
		for (final Progress progress : this.progress)
			progress.setCount(count, total);
	}

	@Override
	public void addItem(final Object item) {
		for (final Progress progress : this.progress)
			progress.addItem(item);
	}

	@Override
	public void setItemCount(final int count, final int total) {
		for (final Progress progress : this.progress)
			progress.setItemCount(count, total);
	}

	@Override
	public void itemDone(final Object item) {
		for (final Progress progress : this.progress)
			progress.itemDone(item);
	}

	@Override
	public void done() {
		for (final Progress progress : this.progress)
			progress.done();
	}
}
