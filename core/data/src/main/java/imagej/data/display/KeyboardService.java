
package imagej.data.display;

import imagej.event.EventService;
import imagej.service.IService;

public interface KeyboardService extends IService {

	EventService getEventService();

	boolean isAltDown();

	boolean isAltGrDown();

	boolean isCtrlDown();

	boolean isMetaDown();

	boolean isShiftDown();

}
