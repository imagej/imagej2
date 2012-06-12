package imagej.updater.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SkipHashedLines extends BufferedInputStream {
	protected boolean atLineStart;

	public SkipHashedLines(final InputStream in) {
		super(in, 1024);
		atLineStart = true;
	}

	@Override
	public int read() throws IOException {
		int ch = super.read();
		if (atLineStart) {
			if (ch == '#')
				while ((ch = read()) != '\n' && ch != -1)
					; // do nothing
			else
				atLineStart = false;
		}
		else if (ch == '\n')
			atLineStart = true;
		return ch;
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		int count = 0;
		while (count < len) {
			int ch = read();
			if (ch < 0)
				return count == 0 ? -1 : count;
			b[off + count] = (byte)ch;
			count++;
		}
		return count;
	}

	@Override
	public long skip(final long n) throws IOException {
		throw new IOException("unsupported skip");
	}
}
