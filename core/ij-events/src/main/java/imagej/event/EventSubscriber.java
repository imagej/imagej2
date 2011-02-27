package imagej.event;

/**
 * Interface for subscribers that wish to be notified of ImageJ events.
 *
 * @author Curtis Rueden
 *
 * @param <E> Type of event for which to listen
 */
public interface EventSubscriber<E extends ImageJEvent>
	extends org.bushe.swing.event.EventSubscriber<E>
{

	@Override
	void onEvent(E event);

}
