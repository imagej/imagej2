//
// MacOSXAppListener.java
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

package imagej.platform.macosx;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.AppForegroundEvent;
import com.apple.eawt.AppEvent.AppHiddenEvent;
import com.apple.eawt.AppEvent.AppReOpenedEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.PrintFilesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.AppEvent.ScreenSleepEvent;
import com.apple.eawt.AppEvent.SystemSleepEvent;
import com.apple.eawt.AppEvent.UserSessionEvent;
import com.apple.eawt.AppForegroundListener;
import com.apple.eawt.AppHiddenListener;
import com.apple.eawt.AppReOpenedListener;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.PrintFilesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.ScreenSleepListener;
import com.apple.eawt.SystemSleepListener;
import com.apple.eawt.UserSessionListener;

import imagej.event.Events;
import imagej.platform.Platform;
import imagej.platform.event.AppAboutEvent;
import imagej.platform.event.AppFocusEvent;
import imagej.platform.event.AppPreferencesEvent;
import imagej.platform.event.AppPrintEvent;
import imagej.platform.event.AppQuitEvent;
import imagej.platform.event.AppReOpenEvent;
import imagej.platform.event.AppScreenSleepEvent;
import imagej.platform.event.AppSystemSleepEvent;
import imagej.platform.event.AppUserSessionEvent;
import imagej.platform.event.AppVisibleEvent;

/**
 * An adapter for handling Mac OS X application issues:
 * <ul>
 * <li>Application events are rebroadcast as ImageJ events.</li>
 * <li>Mac OS X screen menu bar is enabled.</li>
 * <li>Special screen menu bar menu items are handled.</li> 
 * </ul>
 * 
 * @author Curtis Rueden
 */
@Platform(osName = "Mac OS X")
public class MacOSXAppListener implements AboutHandler,
	AppForegroundListener, AppHiddenListener, AppReOpenedListener,
	PreferencesHandler, PrintFilesHandler, QuitHandler, ScreenSleepListener,
	SystemSleepListener, UserSessionListener
{

	// -- AboutHandler methods --

	@Override
	public void handleAbout(final AboutEvent e) {
		Events.publish(new AppAboutEvent());
	}

	// -- PreferencesHandler methods --

	@Override
	public void handlePreferences(final PreferencesEvent e) {
		Events.publish(new AppPreferencesEvent());
	}

	// -- PrintFilesHandler --

	@Override
	public void printFiles(final PrintFilesEvent e) {
		Events.publish(new AppPrintEvent());
	}

	// -- QuitHandler methods --

	@Override
	public void handleQuitRequestWith(final QuitEvent e, final QuitResponse r) {
		Events.publish(new AppQuitEvent());
	}

	// -- UserSessionListener methods --

	@Override
	public void userSessionActivated(final UserSessionEvent e) {
		Events.publish(new AppUserSessionEvent(true));
	}

	@Override
	public void userSessionDeactivated(final UserSessionEvent e) {
		Events.publish(new AppUserSessionEvent(false));
	}

	// -- SystemSleepListener methods --

	@Override
	public void systemAboutToSleep(final SystemSleepEvent e) {
		Events.publish(new AppSystemSleepEvent(true));
	}

	@Override
	public void systemAwoke(final SystemSleepEvent e) {
		Events.publish(new AppSystemSleepEvent(false));
	}

	// -- ScreenSleepListener methods --

	@Override
	public void screenAboutToSleep(final ScreenSleepEvent e) {
		Events.publish(new AppScreenSleepEvent(true));
	}

	@Override
	public void screenAwoke(final ScreenSleepEvent e) {
		Events.publish(new AppScreenSleepEvent(false));
	}

	// -- AppHiddenListener methods --

	@Override
	public void appHidden(final AppHiddenEvent e) {
		Events.publish(new AppVisibleEvent(false));
	}

	@Override
	public void appUnhidden(final AppHiddenEvent e) {
		Events.publish(new AppVisibleEvent(true));
	}

	// -- AppForegroundListener methods --

	@Override
	public void appMovedToBackground(final AppForegroundEvent e) {
		Events.publish(new AppFocusEvent(false));
	}

	@Override
	public void appRaisedToForeground(final AppForegroundEvent e) {
		Events.publish(new AppFocusEvent(true));
	}

	// -- AppReOpenedListener methods --

	@Override
	public void appReOpened(final AppReOpenedEvent e) {
		Events.publish(new AppReOpenEvent());
	}

}
