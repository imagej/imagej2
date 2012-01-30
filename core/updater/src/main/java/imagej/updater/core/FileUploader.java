
package imagej.updater.core;

import imagej.updater.util.Progressable;
import imagej.updater.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/*
 * This FileUploader is highly specialized to upload plugins and XML
 * information over to Pacific. There is a series of steps to follow. Any
 * exception means entire upload process is considered invalid.
 *
 * 1.) Set db.xml.gz to read-only
 * 2.) Verify db.xml.gz has not been modified, if not, upload process cancelled
 * 3.) Upload db.xml.gz.lock (Lock file, prevent others from writing it ATM)
 * 4.) If all goes well, force rename db.xml.gz.lock to db.xml.gz
 */
public class FileUploader extends Progressable {

	protected final String uploadDir;
	protected int total;
	protected long timestamp;

	public FileUploader() {
		this(Util.UPDATE_DIRECTORY);
	}

	public FileUploader(final String uploadDir) {
		this.uploadDir = uploadDir;
	}

	public void calculateTotalSize(final List<SourceFile> sources) {
		total = 0;
		for (final SourceFile source : sources)
			total += (int) source.getFilesize();
	}

	// Steps to accomplish entire upload task
	public synchronized void upload(final List<SourceFile> sources,
		final List<String> locks) throws IOException
	{
		timestamp = Long.parseLong(Util.timestamp(System.currentTimeMillis()));
		setTitle("Uploading");

		calculateTotalSize(sources);
		int count = 0;

		final byte[] buffer = new byte[65536];
		for (final SourceFile source : sources) {
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

	public interface SourceFile {

		public String getFilename();

		public String getPermissions();

		public long getFilesize();

		public InputStream getInputStream() throws IOException;
	}
}
