//
// OptionsMemoryAndThreads.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.options.plugins;

import imagej.ImageJ;
import imagej.ext.menu.MenuConstants;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsPlugin;
import imagej.util.FileUtils;
import imagej.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs the Edit::Options::Memory &amp; Threads dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Memory & Threads...", weight = 12) })
public class OptionsMemoryAndThreads extends OptionsPlugin {

	// -- instance variables that are Parameters --
	
	@Parameter(label = "Maximum memory (MB)", persist=false)
	private int maxMemory = 0;

	@Parameter(label = "Parallel threads for stacks")
	private int stackThreads = 2;

	@Parameter(label = "Keep multiple undo buffers")
	private boolean multipleBuffers = false;

	@Parameter(label = "Run garbage collector on status bar click")
	private boolean runGcOnClick = true;

	// -- private static variables --

	private static final String CONFIG_FILE = "ImageJ.cfg";
	
	// -- private instance variables --

	private LauncherParams params = null;
	
	// -- OptionsMemoryAndThreads methods --

	/** Default constructor */
	public OptionsMemoryAndThreads() {
		load(); // NB: Load persisted values *after* field initialization.
	}

	/** Loads the instance variable fields from persistent storage */
	@Override
	public void load() {
		super.load();
		loadMaxMemory();
	}
	
	/** Saves the instance variable fields to persistent storage */
	@Override
	public void save() {
		super.save();
		saveMaxMemory();
	}

	/**
	 * Returns the number of megabytes of memory that should be allocated for
	 * use by ImageJ
	 */
	public int getMaxMemory() {
		return maxMemory;
	}

	/**
	 * Returns the number of stack threads that should be allocated for use by
	 * ImageJ
	 */
	public int getStackThreads() {
		return stackThreads;
	}

	/**
	 * Returns true of ImageJ will maintain multiple undo buffers
	 */
	public boolean isMultipleBuffers() {
		return multipleBuffers;
	}

	/**
	 * Returns true if ImageJ will run the garbage collector when user clicks
	 * on the status area.
	 */
	public boolean isRunGcOnClick() {
		return runGcOnClick;
	}

	/**
	 * Sets the number of megabytes of memory that should be allocated for
	 * use by ImageJ
	 */
	public void setMaxMemory(final int maxMemory) {
		this.maxMemory = maxMemory;
		saveMaxMemory();
	}

	/**
	 * Sets the number of stack threads that should be allocated for use by
	 * ImageJ
	 */
	public void setStackThreads(final int stackThreads) {
		this.stackThreads = stackThreads;
	}

	/**
	 * Sets whether ImageJ will maintain multiple undo buffers
	 */
	public void setMultipleBuffers(final boolean multipleBuffers) {
		this.multipleBuffers = multipleBuffers;
	}

	/**
	 * Sets whether ImageJ will run the garbage collector when user clicks
	 * on the status area.
	 */
	public void setRunGcOnClick(final boolean runGcOnClick) {
		this.runGcOnClick = runGcOnClick;
	}

	// -- private helpers --

	/** loads the maxMemory instance variable from persistent storage */
	private void loadMaxMemory() {
		if (params == null)
			params = new LauncherParams(getCfgFileName());
		maxMemory = params.getMemoryInMB();
	}
	
	/** saves the maxMemory instance variable to persistent storage */
	private void saveMaxMemory() {
		if (maxMemory != params.getMemoryInMB())
			params.setMemoryInMB(maxMemory);
	}
	
	/** Finds the name/location of the launcher config file */
	private String getCfgFileName() {
		final File directory = FileUtils.getImageJDirectory();
		return new File(directory, CONFIG_FILE).getAbsolutePath();
	}

	// TODO - break out private class into a publicly accessible one somewhere
	
	/**
	 * This class reads launcher configuration parameters from a file and allows
	 * them to be maintained.
	 *  
	 * @author Barry DeZonia
	 */
	private class LauncherParams {

		// -- constants --
		
		private final Integer MINIMUM_MEMORY = 256;  // in megabytes
		private static final String SENTINEL = "ImageJ startup properties";
		private static final String MEMORY_KEY = "maxheap.mb";
		private static final String JVMARGS_KEY = "jvmargs";

		// -- private instance variables --
		
		private final Map<String,String> dataMap;
		private final String filename;
		
		// -- constructor --

		/** Constructs a LauncherParams object. Uses filename for loading/saving
		 * it's values.
		 */
		public LauncherParams(String filename) {
			this.dataMap = new HashMap<String,String>();
			this.filename = filename;
			initialize();
		}
		
		// -- public interface --

		/** Returns the value of the number of megabytes of ram to allocate that is
		 * specified in the launcher config file. Will never return less than a
		 * minimum number (currently 256).
		 */
		public int getMemoryInMB() {
			final String memVal = dataMap.get(MEMORY_KEY);
			Integer val = 0;
			try {
				val = Integer.parseInt(memVal);
			} catch (NumberFormatException e) {
				Log.warn("Launcher configuration file " + filename + " has key " + MEMORY_KEY +
									" that is not in an integer format");
			}
			if (val < MINIMUM_MEMORY) val = MINIMUM_MEMORY;
			return val;
		}
		
		/** Sets the value of the number of megabytes of ram to allocate. Saves this
		 * value in the launcher config file. Will not allow values less than a
		 * minimum number (currently 256).
		 */
		public void setMemoryInMB(int numMegabytes) {
			Integer memory = numMegabytes;
			if (memory < MINIMUM_MEMORY) {
				memory = MINIMUM_MEMORY;
				Log.warn("Max Java heap size can be no smaller than "+MINIMUM_MEMORY+
									" megabytes.");
			}
			dataMap.put(MEMORY_KEY, memory.toString());
			save();
		}

		/** Initializes launcher config values. If possible loads values from
		 * launcher config file. If launcher config file does not exist or is
		 * outdated or faulty this method will save a valid set of parameters in
		 * the launcher config file.
		 */
		public void initialize() {
			setDefaultValues(dataMap);
			final boolean needSave;
			if (isLegacyConfigFile(filename)) {
				loadLegacyConfigValues(dataMap, filename);
				needSave = true;
			}
			else
				needSave = !loadModernConfigValues(dataMap, filename);
			if (needSave) save();
		}

		/** Saves current values to the launcher config file */
		public void save() {
			saveConfigValues(dataMap, filename);
		}

		// -- private helpers --

		/** initializes launcher config file values to valid defaults */
		private void setDefaultValues(Map<String,String> map) {
			map.clear();
			map.put(MEMORY_KEY, MINIMUM_MEMORY.toString());
			map.put(JVMARGS_KEY, "");
		}
		
		/** returns true if specified config file is an old legacy style launcher
		 * config file */
		private boolean isLegacyConfigFile(String fname) {
			try {
				final FileInputStream fstream = new FileInputStream(fname);
			  final DataInputStream din = new DataInputStream(fstream);
			  final InputStreamReader in = new InputStreamReader(din);
			  final BufferedReader br = new BufferedReader(in);
				final String firstLine = br.readLine();
				in.close();
				return !firstLine.contains(SENTINEL);
			} catch (Exception e) {
				return false;
			}
		}

		/** loads launcher config file values from an old legacy style file */
		private boolean loadLegacyConfigValues(Map<String,String> map, String fname) {
			try {
				final FileInputStream fstream = new FileInputStream(fname);
			  final DataInputStream din = new DataInputStream(fstream);
			  final InputStreamReader in = new InputStreamReader(din);
			  final BufferedReader br = new BufferedReader(in);
			  // ignore first line: a path ... something like "."
				br.readLine();
			  // ignore second line: path to java.exe
				br.readLine();
				// everything we want is on third line
				final String argString = br.readLine();
				in.close();
				final Integer memSize = memorySize(argString);
				final String jvmArgs = jvmArgs(argString);
				map.put(MEMORY_KEY, memSize.toString());
				map.put(JVMARGS_KEY, jvmArgs);
				return true;
			}
			catch (Exception e) {
				Log.warn("Could not load legacy launcher config file "+fname);
				return false;
			}
		}
		
		/** loads launcher config file values from a modern IJ2 style file */
		private boolean loadModernConfigValues(Map<String,String> map, String fname) {
			try {
				final FileInputStream fstream = new FileInputStream(fname);
			  final DataInputStream din = new DataInputStream(fstream);
			  final InputStreamReader in = new InputStreamReader(din);
			  final BufferedReader br = new BufferedReader(in);
				final Pattern keyValuePairPattern = Pattern.compile("\\s*(.*)\\s*=\\s*(.*)");
			  // skip first line : sentinel
			  br.readLine();
			  while (br.ready()) {
			  	final String s = br.readLine();
					final Matcher matcher = keyValuePairPattern.matcher(s);
					if (matcher.matches()) {
						final String key = matcher.group(1).trim();
						final String value = matcher.group(2).trim();
						map.put(key, value);
					}
					
			  }
				return true;
			}
			catch (IOException e) {
				Log.warn("Could not load launcher config file "+fname);
				return false;
			}
		}
		
		/** writes launcher config values to an IJ2 style launcher config file */
		private void saveConfigValues(Map<String,String> map, String fname) {
			try {
			  final FileOutputStream fos = new FileOutputStream(fname);
			  final OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
			  final BufferedWriter out = new BufferedWriter(osw);
			  out.write("#"+SENTINEL + " ("+ImageJ.VERSION+")");
		  	out.newLine();
			  for (String key : map.keySet()) {
			  	String value = map.get(key);
			  	// make sure we don't write out something that breaks file structure
			  	value = value.replaceAll("\n", "");
			  	value = value.replaceAll("\r", "");
			  	out.write(key + " = " + map.get(key));
			  	out.newLine();
			  }
				out.close();
			} catch (IOException e) {
				Log.warn("Could not save launcher config file values to "+fname);
			}
		}

		/** returns the number of megabytes specified in a text line from a legacy
		 * launcher config file (3rd line).
		 */
		private int memorySize(String argList) {
			// NB - In old IJ1 cfg files heap use is encoded as a single argument:
			// 	"-XmxNNNNNNm" where N are numbers. 
			// Find this argument and parse it
			final String[] args = argList.split("\\s+");
			for (String arg : args) {
				if (arg.startsWith("-Xmx")) {
					String numString = arg.substring(4);
					numString = numString.substring(0,numString.length()-1);
					try {
						return Integer.parseInt(numString);
					} catch (NumberFormatException e) {
						return MINIMUM_MEMORY;
					}
				}
			}
			return MINIMUM_MEMORY;
		}

		/** returns a string containing all the command line arguments from
		 * a legacy launcher config file (3rd line). Ignores memory specification
		 * as that is handled by memorySize().
		 */
		private String jvmArgs(String argList) {
			
			// NB - From old IJ1 cfg we return the list of arguments except the heap
			// specification (which we've handled elsewhere) and the name of the
			// main class to launch (since ours will be different). 

			String value = "";
			final String[] args = argList.split("\\s+");
			for (String arg : args) {
				if (arg.startsWith("-Xmx")) continue;  // skip heap size specification
				if (arg.equals("ij.ImageJ")) continue; // skip name of main class
				// if here then we are interested in this parameter
				if (value.length() > 0)
					value += " ";
				value += arg;
				// TODO This last addition could include class path info. Is that a
				// problem? We may not want the legacy file's class path info.
			}
			return value;
		}
	}
}
