//
// StatusEvent.java
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

package imagej.event;

/**
 * An event indicating a status update.
 *
 * @author Curtis Rueden
 */
public class StatusEvent extends ImageJEvent {

  /** Current progress value. */
  private final int progress;

  /** Current progress maximum. */
  private final int maximum;

  /** Current status message. */
  private final String status;

  /** Whether or not this is a warning event. */
  private final boolean warning;

  /** Constructs a status event. */
  public StatusEvent(final String message) {
    this(-1, -1, message);
  }

  /** Constructs a status event. */
  public StatusEvent(final String message, final boolean warn) {
    this(-1, -1, message, warn);
  }

  /** Constructs a status event. */
  public StatusEvent(final int progress, final int maximum) {
    this(progress, maximum, null);
  }

  /** Constructs a status event. */
  public StatusEvent(final int progress, final int maximum,
  	final String message)
  {
    this(progress, maximum, message, false);
  }

  /** Constructs a status event. */
  public StatusEvent(final int progress, final int maximum,
  	final String message, final boolean warn)
  {
    this.progress = progress;
    this.maximum = maximum;
    status = message;
    warning = warn;
  }

  /** Gets progress value. Returns -1 if progress is unknown. */
  public int getProgressValue() { return progress; }

  /** Gets progress maximum. Returns -1 if progress is unknown. */
  public int getProgressMaximum() { return maximum; }

  /** Gets status message. */
  public String getStatusMessage() { return status; }

  /** Returns whether or not this is a warning event. */
  public boolean isWarning() { return warning; }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Status");
    sb.append(": progress=" + progress);
    sb.append(", maximum=" + maximum);
    sb.append(", warning=" + warning);
    sb.append(", status='" + status + "'");
    return sb.toString();
  }

}
