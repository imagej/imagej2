//
// AWTWindows.java
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

package imagej.util.awt;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * Utility methods for working with AWT {@link Window}s.
 * 
 * @author Curtis Rueden
 */
public final class AWTWindows {

	private AWTWindows() {
		// prevent instantiation of utility class
	}

	/** Centers the given window on the screen. */
	public static void centerWindow(final Window window) {
		final Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
		centerWindow(new Rectangle(0, 0, s.width, s.height), window);
	}

	/** Centers the given window within the specified parent window. */
	public static void centerWindow(final Window parent, final Window window) {
		centerWindow(parent.getBounds(), window);
	}

	/** Centers the given window within the specified bounds. */
	public static void centerWindow(final Rectangle bounds, final Window window)
	{
		final Dimension w = window.getSize();
		int x = bounds.x + (bounds.width - w.width) / 2;
		int y = bounds.y + (bounds.height - w.height) / 2;
		if (x < 0) x = 0;
		if (y < 0) y = 0;
		window.setLocation(x, y);
	}

}
