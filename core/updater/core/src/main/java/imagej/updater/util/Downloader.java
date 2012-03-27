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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/*
 * Direct responsibility: Download a list of files given their respective URLs
 * to their respective destinations. Updates its download status to its
 * Observer as well.
 */
public class Downloader extends Progressable {

	protected int count, total, itemCount, itemTotal;
	protected long lastModified;

	protected String error;
	protected boolean cancelled;

	public Downloader() {}

	public Downloader(final Progress progress) {
		this();
		addProgress(progress);
	}

	public synchronized void cancel() {
		cancelled = true;
	}

	public synchronized void start(final Downloadable justOne) throws IOException
	{
		start(new OneItemIterable<Downloadable>(justOne));
	}

	public void start(final Iterable<Downloadable> files) throws IOException {
		Util.useSystemProxies();
		cancelled = false;

		count = total = itemCount = itemTotal = 0;
		for (final Downloadable file : files) {
			total += file.getFilesize();
			itemTotal++;
		}

		setTitle("Downloading...");

		for (final Downloadable current : files) {
			if (cancelled) break;
			download(current);
		}
		done();
	}

	protected synchronized void download(final Downloadable current)
		throws IOException
	{
		final URLConnection connection = new URL(current.getURL()).openConnection();
		connection.setUseCaches(false);
		lastModified = connection.getLastModified();
		int currentTotal = connection.getContentLength();
		if (currentTotal < 0) currentTotal = (int) current.getFilesize();

		final File destination = current.getDestination();
		addItem(current);

		final File parentDirectory = destination.getParentFile();
		if (parentDirectory != null) parentDirectory.mkdirs();
		final InputStream in = connection.getInputStream();
		final OutputStream out = new FileOutputStream(destination);

		int currentCount = 0;
		int total = this.total;
		if (total == 0) total =
			(count + currentTotal) * itemTotal / (itemCount + 1);

		final byte[] buffer = new byte[65536];
		for (;;) {
			if (cancelled) break;
			final int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
			currentCount += count;
			this.count += count;
			setCount(this.count, total);
			setItemCount(currentCount, currentTotal);
		}
		in.close();
		out.close();
		itemDone(current);
	}

	public long getLastModified() {
		return lastModified;
	}
}
