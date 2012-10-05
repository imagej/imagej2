package fiji.build.minimaven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class ReadInto extends Thread {
	protected BufferedReader reader;
	protected PrintStream err;
	protected StringBuilder buffer = new StringBuilder();

	public ReadInto(InputStream in, PrintStream err) {
		reader = new BufferedReader(new InputStreamReader(in));
		this.err = err;
		start();
	}

	public void run() {
		for (;;) try {
			String line = reader.readLine();
			if (line == null)
				break;
			if (err != null)
				err.println(line);
			buffer.append(line);
			Thread.sleep(0);
		}
		catch (InterruptedException e) { /* just stop */ }
		catch (IOException e) { /* just stop */ }
		try {
			reader.close();
		}
		catch (IOException e) { /* just stop */ }
	}

	public String toString() {
		return buffer.toString();
	}
}
