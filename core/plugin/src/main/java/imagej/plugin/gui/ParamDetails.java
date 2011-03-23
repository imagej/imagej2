//
// ParamDetails.java
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

package imagej.plugin.gui;

import imagej.plugin.Parameter;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class ParamDetails {

	private final String name;
	private final Class<?> type;
	private final Parameter param;

	private final String label;
	private final String description;
	private final String callback;
	private final WidgetStyle style;

	public ParamDetails(final String name, final Class<?> type,
		final Parameter param)
	{
		this.name = name;
		this.type = type;
		this.param = param;

		label = makeLabel(param.label());
		description = param.description();
		callback = param.callback();
		style = param.style();
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public Parameter getParameter() {
		return param;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public String getCallback() {
		return callback;
	}

	public WidgetStyle getStyle() {
		return style;
	}

	private String makeLabel(final String s) {
		if (s == null || s.isEmpty()) {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		return s;
	}

}
