/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.core.commands.debug;

import imagej.app.ImageJApp;
import imagej.command.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.app.App;
import org.scijava.app.AppService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.util.ClassUtils;
import org.scijava.util.Manifest;

/**
 * Dumps Java system properties.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menuPath = "Plugins>Debug>System Information", headless = true)
public class SystemInformation implements Command {

	// -- Constants --

	private static final String NL = System.getProperty("line.separator");

	// -- Parameters --

	@Parameter
	private Context context;

	@Parameter
	private AppService appService;

	@Parameter(label = "System Information", type = ItemIO.OUTPUT)
	private String info;

	// -- Runnable methods --

	@Override
	public void run() {
		final StringBuilder sb = new StringBuilder();

		// dump basic version information (similar to the status bar)
		sb.append(appService.getApp(ImageJApp.NAME).getInfo(false) + NL);

		// dump information about available SciJava applications
		final Map<String, App> apps = appService.getApps();
		for (final String name : apps.keySet()) {
			final App app = apps.get(name);
			final Manifest manifest = app.getManifest();
			sb.append(NL);
			sb.append("-- Application: " + name + " --" + NL);
			sb.append("Title = " + app.getTitle() + NL);
			sb.append("Version = " + app.getVersion() + NL);
			sb.append("groupId = " + app.getGroupId() + NL);
			sb.append("artifactId = " + app.getArtifactId() + NL);
			if (manifest != null) {
				sb.append(getManifestData(manifest));
			}
		}

		// compute the set of known plugin types
		final List<PluginInfo<?>> plugins = context.getPluginIndex().getAll();
		final HashSet<Class<? extends SciJavaPlugin>> pluginTypeSet =
			new HashSet<Class<? extends SciJavaPlugin>>();
		for (final PluginInfo<?> plugin : plugins) {
			pluginTypeSet.add(plugin.getPluginType());
		}

		// convert to a list of plugin types, sorted by fully qualified class name
		final ArrayList<Class<? extends SciJavaPlugin>> pluginTypes =
			new ArrayList<Class<? extends SciJavaPlugin>>(pluginTypeSet);
		Collections.sort(pluginTypes, new Comparator<Class<?>>() {

			@Override
			public int compare(final Class<?> c1, final Class<?> c2) {
				return ClassUtils.compare(c1, c2);
			}

		});

		// dump the list of available plugins, organized by plugin type
		for (final Class<? extends SciJavaPlugin> pluginType : pluginTypes) {
			dumpPlugins(sb, pluginType);
		}

		// dump system properties
		sb.append(NL);
		sb.append("-- System properties --" + NL);
		sb.append(getSystemProperties());

		info = sb.toString();
	}

	// -- Utility methods --

	public static String getSystemProperties() {
		return mapToString(System.getProperties());
	}

	public static String getManifestData(final Manifest manifest) {
		if (manifest == null) return null;
		return mapToString(manifest.getAll());
	}

	public static String mapToString(final Map<Object, Object> map) {
		final StringBuilder sb = new StringBuilder();

		// sort keys by string representation
		final ArrayList<Object> keys = new ArrayList<Object>(map.keySet());
		Collections.sort(keys, new Comparator<Object>() {

			@Override
			public int compare(final Object o1, final Object o2) {
				if (o1 == null && o2 == null) return 0;
				if (o1 == null) return -1;
				if (o2 == null) return 1;
				return o1.toString().compareTo(o2.toString());
			}

		});

		for (final Object key : keys) {
			if (key == null) continue;
			final Object value = map.get(key);
			final String sKey = key.toString();
			final String sValue = value == null ? "(null)" : value.toString();

			if (sKey.endsWith(".dirs") || sKey.endsWith(".path")) {
				// split path and display values as a list
				final String[] dirs = sValue.split(Pattern.quote(File.pathSeparator));
				sb.append(sKey + " = {" + NL);
				for (final String dir : dirs) {
					sb.append("\t" + dir + NL);
				}
				sb.append("}" + NL);
			}
			else {
				// display a single key/value pair
				sb.append(sKey + " = " + sValue + NL);
			}
		}
		return sb.toString();
	}

	// -- Helper methods --

	private <PT extends SciJavaPlugin> void dumpPlugins(final StringBuilder sb,
		final Class<PT> pluginType)
	{
		final List<PluginInfo<PT>> plugins =
			context.getPluginIndex().getPlugins(pluginType);

		// count the number of plugins whose type matches exactly (not sub-types)
		int pluginCount = 0;
		for (final PluginInfo<PT> plugin : plugins) {
			if (pluginType == plugin.getPluginType()) pluginCount++;
		}
		if (pluginCount == 0) return;

		sb.append(NL);
		sb.append("-- " + pluginCount + " " + pluginType.getName() +
			" plugins --" + NL);
		for (final PluginInfo<PT> plugin : plugins) {
			if (pluginType != plugin.getPluginType()) continue;
			sb.append(plugin + NL);
		}
	}

}
