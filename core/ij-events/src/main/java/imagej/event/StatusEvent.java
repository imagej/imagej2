package imagej.event;

/**
 * A event indicating a status update.
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
