package imagej.core.commands.upload;

import imagej.ImageJ;
import imagej.command.Command;
import imagej.command.CommandInfo;
import imagej.command.CommandService;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.event.StatusService;
import imagej.log.LogService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.iharder.Base64;

@Plugin(menuPath = "Help>Upload Sample Image")
public class SampleImageUploader implements Command {
	@Parameter
	private File sampleImage;

	@Parameter
	private StatusService status;

	@Parameter
	private LogService log;

	private static String baseURL = "http://upload.imagej.net/";

	@Override
	public void run() {
		try {
			uploadFile(sampleImage);
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void uploadFile(final File file) throws IOException, MalformedURLException {
		upload(baseURL + file.getName(), new BufferedInputStream(new FileInputStream(file)), file.length());
	}

	private void upload(final String url, final InputStream in, final long totalLength) throws IOException, MalformedURLException {
		if (status != null) status.showStatus("Uploading " + url);

		final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		final String authentication = "ij2-sample-upload:password";
		connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes(authentication.getBytes()));
		connection.setRequestProperty("Content-Type", "application/octet-stream");
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		final OutputStream out = connection.getOutputStream();
		byte[] buffer = new byte[65536];
		long count = 0;
		for (;;) {
			int count2 = in.read(buffer);
			if (count2 < 0) break;
			out.write(buffer, 0, count2);
			count += count2;
			if (totalLength > 0 && status != null) status.showProgress((int)count, (int)totalLength);
		}
		out.close();
		in.close();

		final BufferedReader response = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		for (;;) {
			String line = response.readLine();
			if (line == null) break;
			System.err.println(line);
		}
		response.close();

		if (status != null) status.clearStatus();
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Thread.currentThread().setContextClassLoader(SampleImageUploader.class.getClassLoader());
		try {
			final ImageJ context = ImageJ.createContext(CommandService.class, StatusService.class, EventService.class);
			context.getService(EventService.class).subscribe(new Object() {
				@SuppressWarnings("unused")
				@EventHandler
				protected void onEvent(final StatusEvent e) {
					final int value = e.getProgressValue();
					final int maximum = e.getProgressMaximum();
					final String message = e.getStatusMessage();
					if (maximum > 0) System.err.print("(" + value + "/" + maximum + ")");
					if (message != null) System.err.print(" " + message);
					System.err.println();
				}
			});
			final CommandInfo info = new CommandInfo(SampleImageUploader.class.getName()) {
				@Override
				public Class<Command> loadClass() {
					return (Class<Command>)(Class<?>)SampleImageUploader.class;
				}
			};
			context.getService(CommandService.class).run(info, "sampleImage", new File("/tmp/test.tif"));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}