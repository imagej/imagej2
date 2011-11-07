//
// MenuEntry.java
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

package imagej.ext;

import java.util.regex.Pattern;

/**
 * One component of a menu path, for use with {@link MenuPath}.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class MenuEntry {

	public static final double DEFAULT_WEIGHT = Double.POSITIVE_INFINITY;

	public static final double FILE_WEIGHT = 0;
	public static final double EDIT_WEIGHT = 1;
	public static final double IMAGE_WEIGHT = 2;
	public static final double PROCESS_WEIGHT = 3;
	public static final double ANALYZE_WEIGHT = 4;
	public static final double PLUGINS_WEIGHT = 5;
	public static final double WINDOW_WEIGHT = 6;
	public static final double HELP_WEIGHT = 1e7;

	private String name;
	private double weight = DEFAULT_WEIGHT;
	private char mnemonic;
	private String accelerator;
	private String iconPath;

	public MenuEntry(final String name) {
		this.name = name;
	}

	public MenuEntry(final String name, final double weight) {
		this.name = name;
		this.weight = weight;
	}

	public MenuEntry(final String name, final double weight, final char mnemonic,
		final String accelerator, final String iconPath)
	{
		this.name = name;
		this.weight = weight;
		this.mnemonic = mnemonic;
		setAccelerator(accelerator);
		this.iconPath = iconPath;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setWeight(final int weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	public void setMnemonic(final char mnemonic) {
		this.mnemonic = mnemonic;
	}

	public char getMnemonic() {
		return mnemonic;
	}

	public void setAccelerator(final String accelerator) {
		this.accelerator = normalizeAccelerator(accelerator);
	}

	public String getAccelerator() {
		return accelerator;
	}

	public void setIconPath(final String iconPath) {
		this.iconPath = iconPath;
	}

	public String getIconPath() {
		return iconPath;
	}

	/**
	 * Updates any default properties of this menu entry to match those of the
	 * given menu entry.
	 */
	public void assignProperties(final MenuEntry entry) {
		if (name == null) name = entry.getName();
		if (weight == DEFAULT_WEIGHT) weight = entry.getWeight();
		if (mnemonic == '\0') mnemonic = entry.getMnemonic();
		if (accelerator == null) accelerator = entry.getAccelerator();
		if (iconPath == null) iconPath = entry.getIconPath();
	}

	@Override
	public String toString() {
		return name;
	}

	// -- Helper methods --

	/**
	 * Ensures the accelerator is properly formatted. Two correctly formatted
	 * accelerators that represent the same keystroke must be comparable with
	 * {@link String#equals}.
	 */
	private static String normalizeAccelerator(final String accelerator) {
		if (accelerator == null) return null;

		// allow use of caret for control (e.g., "^X" to represent "control X")
		final String accel = accelerator.replaceAll(Pattern.quote("^"), "control ");

		final String[] components = accel.split(" ");
		if (components.length == 0) return accel;

		// determine which modifiers are used
		boolean alt = false, altGraph = false;
		boolean control = false, meta = false, shift = false;
		for (int i = 0; i < components.length - 1; i++) {
			if (components[i].equalsIgnoreCase("alt")) alt = true;
			else if (components[i].equalsIgnoreCase("altGraph")) altGraph = true;
			else if (components[i].equalsIgnoreCase("ctrl") ||
				components[i].equalsIgnoreCase("control"))
			{
				control = true;
			}
			else if (components[i].equalsIgnoreCase("meta")) meta = true;
			else if (components[i].equalsIgnoreCase("shift")) shift = true;
		}

		// sort the modifiers alphabetically
		final StringBuilder builder = new StringBuilder();
		if (alt) builder.append("alt ");
		if (altGraph) builder.append("altGraph ");
		if (control) builder.append("control ");
		if (meta) builder.append("meta ");
		if (shift) builder.append("shift ");

		// upper case the key code
		builder.append(components[components.length - 1].toUpperCase());
		
		final String normalAccel = builder.toString();

		// TODO - isolate/refactor platform-specific logic
		// on Mac, use Command instead of Control for keyboard shortcuts
		if (isMac() && normalAccel.indexOf("meta ") < 0) {
			// only if meta not already in use
			return normalAccel.replaceAll("control ", "meta ");
		}

		return normalAccel;
	}

	private static boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac");
	}

}
