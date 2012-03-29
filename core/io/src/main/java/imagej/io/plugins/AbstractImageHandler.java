package imagej.io.plugins;

import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import loci.common.StatusListener;

public abstract class AbstractImageHandler implements ImageJPlugin, StatusListener {


  @Parameter(persist = false)
  protected EventService eventService;
  
  private long lastTime;

  @Override
  public void statusUpdated(final loci.common.StatusEvent e) {
    final long time = System.currentTimeMillis();
    final int progress = e.getProgressValue();
    final int maximum = e.getProgressMaximum();
    final String message = e.getStatusMessage();
    final boolean warn = e.isWarning();

    // don't update more than 20 times/sec
    if (time - lastTime < 50 && progress > 0 && progress < maximum && !warn) {
      return;
    }
    lastTime = time;

    eventService.publish(new StatusEvent(progress, maximum, message, warn));
  }
  
}
