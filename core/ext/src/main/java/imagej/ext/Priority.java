//
// Priority.java
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

public class Priority {

	/**
	 * Priority for processors that must go first in the processor chain.
	 * Examples: {@link DebugPreprocessor}, {@link DebugPostprocessor}
	 */
	public static final double FIRST_PRIORITY = Double.NEGATIVE_INFINITY;

	/**
	 * Priority for processors that strongly prefer to be early in the processor
	 * chain. Examples: {@link ActiveDisplayPreprocessor},
	 * {@link ServicePreprocessor}
	 */
	public static final double VERY_HIGH_PRIORITY = -10000;

	/**
	 * Priority for processors that prefer to be earlier in the processor chain.
	 * Example: {@link InitPreprocessor}
	 */
	public static final double HIGH_PRIORITY = -100;

	/** Default priority for processors. */
	public static final double NORMAL_PRIORITY = 0;

	/** Priority for processors that prefer to be later in the processor chain. */
	public static final double LOW_PRIORITY = 100;

	/**
	 * Priority for processors that strongly prefer to be late in the processor
	 * chain. Examples: {@link DisplayPostprocessor}, UI-specific subclasses of
	 * {@link AbstractInputHarvesterPlugin}.
	 */
	public static final double VERY_LOW_PRIORITY = 10000;

	/** Priority for processors that must go at the end of the processor chain. */
	public static final double LAST_PRIORITY = Double.POSITIVE_INFINITY;

}
