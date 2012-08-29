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

package imagej.core.plugins.debug;

import imagej.ImageJ;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.RunnablePlugin;
import imagej.module.ItemIO;
import imagej.util.Manifest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Dumps Java system properties.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Debug>System Information", headless = true)
public class SystemInformation implements RunnablePlugin {

	// -- Constants --

	private static final String NL = System.getProperty("line.separator");

	// -- Parameters --

	@Parameter
	private ImageJ context;

	@Parameter(label = "System Information", type = ItemIO.OUTPUT)
	private String info;

	// -- Runnable methods --

	@Override
	public void run() {
		final StringBuilder sb = new StringBuilder();

		sb.append(context.getInfo(false) + NL);

		final Manifest manifest = context.getManifest();
		if (manifest != null) {
			sb.append(NL);
			sb.append("-- Manifest details --" + NL);
			sb.append(getManifestData(manifest));
		}

		sb.append(NL);
		sb.append("-- System properties --" + NL);
		sb.append(getSystemProperties());

		info = sb.toString();
	}

	// -- Utility methods --

	public static String getSystemProperties() {
		return mapToString(System.getProperties());
	}
	
	public String getManifestData(final Manifest manifest) {
		if (manifest == null) return null;
		return mapToString(manifest.getAll());
	}

	public static String mapToString(final Map<Object, Object> map) {
		final StringBuilder sb = new StringBuilder();

		// sort keys
		final ArrayList<String> keys = new ArrayList<String>();
		for (Object key : map.keySet()) {
			keys.add(key.toString());
		}
		Collections.sort(keys);

		for (final String key : keys) {
			if (key == null) continue;
			final Object o = map.get(key);
			final String value = o == null ? "(null)" : o.toString();

			if (key.endsWith(".dirs") || key.endsWith(".path")) {
				// split path and display values as a list
				final String[] dirs = value.split(Pattern.quote(File.pathSeparator));
				sb.append(key + " = {" + NL);
				for (final String dir : dirs) {
					sb.append("\t" + dir + NL);
				}
				sb.append("}" + NL);
			}
			else {
				// display a single key/value pair
				sb.append(key + " = " + value + NL);
			}
		}
		return sb.toString();
	}

}
