//
// ToolUtils.java
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

package imagej.tool;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Utility class for discovering and querying tools.
 *
 * @author Curtis Rueden
 */
public final class ToolUtils {

	private ToolUtils() {
		// prohibit instantiation of utility class
	}

	public static List<ToolEntry> findTools() {
		final Index<Tool, ITool> toolIndex = Index.load(Tool.class, ITool.class);
		final List<ToolEntry> tools = new ArrayList<ToolEntry>();
		for (final IndexItem<Tool, ITool> item : toolIndex) {
			tools.add(createEntry(item));
		}
		return tools;
	}

	private static ToolEntry createEntry(final IndexItem<Tool, ITool> item) {
		final String className = item.className();
		final Tool tool = item.annotation();

		final ToolEntry entry = new ToolEntry(className);
		entry.setName(tool.name());
		entry.setLabel(tool.label());
		entry.setDescription(tool.description());
		entry.setIconPath(tool.iconPath());
		entry.setPriority(tool.priority());

		return entry;
	}

}
