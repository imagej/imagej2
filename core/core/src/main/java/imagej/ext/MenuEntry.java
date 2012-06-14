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

package imagej.ext;

/**
 * One component of a menu path, for use with {@link MenuPath}.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class MenuEntry {

	public static final double DEFAULT_WEIGHT = Double.POSITIVE_INFINITY;

	private String name;
	private double weight = DEFAULT_WEIGHT;
	private char mnemonic;
	private Accelerator accelerator;
	private String iconPath;

	public MenuEntry(final String name) {
		setName(name);
	}

	public MenuEntry(final String name, final double weight) {
		setName(name);
		setWeight(weight);
	}

	public MenuEntry(final String name, final double weight,
		final char mnemonic, final Accelerator acc, final String iconPath)
	{
		setName(name);
		setWeight(weight);
		setMnemonic(mnemonic);
		setAccelerator(acc);
		setIconPath(iconPath);
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setWeight(final double weight) {
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

	public void setAccelerator(final Accelerator accelerator) {
		this.accelerator = accelerator;
	}

	public Accelerator getAccelerator() {
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

}
