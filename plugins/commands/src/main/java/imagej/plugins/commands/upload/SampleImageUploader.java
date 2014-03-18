/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.commands.upload;

import imagej.command.Command;

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
import java.net.URLEncoder;

import net.iharder.Base64;

import org.scijava.app.StatusService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Uploads a sample image to the ImageJDev server for further inspection
 * by the developers.
 *
 * @author Johannes Schindelin
 */
@Plugin(type = Command.class, menuPath = "Help>Upload Sample Image")
public class SampleImageUploader implements Command {
	@Parameter
	private File sampleImage;

	@Parameter
	private StatusService status;

	@Parameter
	private LogService log;

	private static String baseURL = "http://upload.imagej.net/";

	/**
	 * This method provides a Java API to upload sample images to the ImageJ2 dropbox.
	 */
	public static void run(final File file, final StatusService status, final LogService log) {
		final SampleImageUploader uploader = new SampleImageUploader();
		uploader.sampleImage = file;
		uploader.status = status;
		uploader.log = log;
		uploader.run();
	}

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

	private void upload(final String unencodedURL, final InputStream in,
		final long totalLength) throws IOException, MalformedURLException
	{
		final String url = URLEncoder.encode(unencodedURL, "UTF-8");

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

		if (status != null) {
			status.clearStatus();
			status.showStatus("Upload complete!");
		}
	}
}
