
package imagej.updater.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStream2OutputStream extends Thread {

	protected InputStream in;
	protected OutputStream out;

	public InputStream2OutputStream(final InputStream in, final OutputStream out)
	{
		this.in = in;
		this.out = out;
		start();
	}

	@Override
	public void run() {
		final byte[] buffer = new byte[16384];
		try {
			for (;;) {
				final int count = in.read(buffer);
				if (count < 0) break;
				out.write(buffer, 0, count);
			}
			in.close();
		}
		catch (final IOException e) {
			UserInterface.get().handleException(e);
		}
	}
}
