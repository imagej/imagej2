package imagej.core.commands.upload;

import imagej.ImageJ;
import imagej.Prioritized;
import imagej.Priority;
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

@Plugin
public class SampleImageUploader {
	@Parameter
	private File sampleImage;

	@Parameter
	private StatusService status;

	@Parameter
	private LogService log;

	private static String baseURL = "http://upload.imagej.net/";

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

	private static class StderrStatusService implements StatusService {
		private double priority;

		@Override
		public int compareTo(Prioritized o) {
			return Priority.compare(this, o);
		}
		
		@Override
		public void setPriority(double priority) {
			this.priority = priority;
		}
		
		@Override
		public double getPriority() {
			return priority;
		}
		
		@Override
		public void setContext(ImageJ context) {
			if (context != null) throw new IllegalArgumentException();
		}
		
		@Override
		public ImageJ getContext() {
			return null;
		}
		
		@Override
		public void initialize() {
			showStatus("Starting stderr status service");
		}
		
		@Override
		public void warn(String message) {
			System.err.println("WARN: " + message);
		}
		
		@Override
		public void showStatus(String message) {
			System.err.println(message);
		}
		
		@Override
		public void showStatus(int progress, int maximum, String message, boolean warn) {
			System.err.print("(" + progress + "/" + maximum + ") ");
			if (message == null) {
				System.err.println();
			} else if (warn) {
				warn(message);
			} else {
				showStatus(message);
			}
		}
		
		@Override
		public void showStatus(int progress, int maximum, String message) {
			showStatus(progress, maximum, message, false);
		}
		
		@Override
		public void showProgress(int value, int maximum) {
			showStatus(value, maximum, null, false);
		}
		
		@Override
		public void clearStatus() {
			System.err.println();
		}
	}

	public static void main(String[] args) {
		try {
			SampleImageUploader uploader = new SampleImageUploader();
			uploader.status = new StderrStatusService();
			uploader.uploadFile(new File("/tmp/test.tif"));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}