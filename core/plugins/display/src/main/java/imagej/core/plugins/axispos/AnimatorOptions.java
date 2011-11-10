//
// AnimatorOptions.java
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

package imagej.core.plugins.axispos;

import net.imglib2.img.Axis;

/**
 * This class is used to define a structure for passing around a group of
 * parameters related to the {@link Animator} and {@link AnimatorOptionsPlugin}
 * classes.
 * 
 * @author Barry DeZonia
 */
public class AnimatorOptions {

	private Axis axis;
	private double fps;
	private long first;
	private long last;
	private boolean backAndForth;

	public AnimatorOptions(final Axis axis, final double fps, final long first,
		final long last, final boolean backAndForth)
	{
		this.axis = axis;
		this.fps = fps;
		this.first = first;
		this.last = last;
		this.backAndForth = backAndForth;
	}

	public Axis getAxis() {
		return axis;
	}

	public void setAxis(final Axis axis) {
		this.axis = axis;
	}

	public double getFps() {
		return fps;
	}

	public void setFps(final double fps) {
		this.fps = fps;
	}

	public long getFirst() {
		return first;
	}

	public void setFirst(final long first) {
		this.first = first;
	}

	public long getLast() {
		return last;
	}

	public void setLast(final long last) {
		this.last = last;
	}

	public boolean isBackAndForth() {
		return backAndForth;
	}

	public void setBackAndForth(final boolean backAndForth) {
		this.backAndForth = backAndForth;
	}
}
