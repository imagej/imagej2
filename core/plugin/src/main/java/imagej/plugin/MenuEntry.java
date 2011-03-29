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

package imagej.plugin;


/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class MenuEntry {

	private String name;
	private double weight = Menu.DEFAULT_WEIGHT;
	private char mnemonic;
	private String accelerator;
	private String icon;

	public MenuEntry(String name) {
		this.name = name;
	}

	public MenuEntry(String name, double weight) {
		this.name = name;
		this.weight = weight;
	}

	public MenuEntry(String name, double weight,
		char mnemonic, String accelerator, String icon)
	{
		this.name = name;
		this.weight = weight;
		this.mnemonic = mnemonic;
		this.accelerator = accelerator;
		this.icon = icon;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	public void setMnemonic(char mnemonic) {
		this.mnemonic = mnemonic;
	}

	public char getMnemonic() {
		return mnemonic;
	}

	public void setAccelerator(String accelerator) {
		this.accelerator = accelerator;
	}

	public String getAccelerator() {
		return accelerator;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
	}

	/**
	 * Updates any default properties of this menu entry
	 * to match those of the given menu entry.
	 */
	public void assignProperties(MenuEntry entry) {
		if (name == null) name = entry.getName();
		if (weight == Menu.DEFAULT_WEIGHT) weight = entry.getWeight();
		if (mnemonic == '\0') mnemonic = entry.getMnemonic();
		if (accelerator == null) accelerator = entry.getAccelerator();
		if (icon == null) icon = entry.getIcon();
	}

	@Override
	public String toString() {
		return name;
	}

}
