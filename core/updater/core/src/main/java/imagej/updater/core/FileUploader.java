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

package imagej.updater.core;

import imagej.updater.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/*
 * This FileUploader is highly specialized to upload files and XML
 * information over to ImageJ2 Update sites. There is a series of steps to follow. Any
 * exception means entire upload process is considered invalid.
 *
 * 1.) Set db.xml.gz to read-only
 * 2.) Verify db.xml.gz has not been modified, if not, upload process cancelled
 * 3.) Upload db.xml.gz.lock (Lock file, prevent others from writing it ATM)
 * 4.) If all goes well, force rename db.xml.gz.lock to db.xml.gz
 */
@Uploader(protocol = "file")
public class FileUploader extends AbstractUploader {

	// Steps to accomplish entire upload task
	@Override
	public synchronized void upload(final List<Uploadable> sources,
		final List<String> locks) throws IOException
	{
		timestamp = Long.parseLong(Util.timestamp(System.currentTimeMillis()));
		setTitle("Uploading");

		calculateTotalSize(sources);
		int count = 0;

		final byte[] buffer = new byte[65536];
		for (final Uploadable source : sources) {
			final File file = new File(uploadDir, source.getFilename());
			final File dir = file.getParentFile();
			if (!dir.exists()) dir.mkdirs();
			final OutputStream out = new FileOutputStream(file);
			final InputStream in = source.getInputStream();
			/*
			 * To get the timestamp of db.xml.gz.lock which
			 * determines its contents, the addItem() call
			 * must be _exactly_ here.
			 */
			addItem(source);
			int currentCount = 0;
			final int currentTotal = (int) source.getFilesize();
			for (;;) {
				final int read = in.read(buffer);
				if (read < 0) break;
				out.write(buffer, 0, read);
				currentCount += read;
				setItemCount(currentCount, currentTotal);
				setCount(count + currentCount, total);
			}
			in.close();
			out.close();
			count += currentCount;
			itemDone(source);
		}

		for (final String lock : locks) {
			final File file = new File(uploadDir, lock);
			final File lockFile = new File(uploadDir, lock + ".lock");
			final File backup = new File(uploadDir, lock + ".old");
			if (backup.exists()) backup.delete();
			file.renameTo(backup);
			lockFile.renameTo(file);
		}

		done();
	}
}
