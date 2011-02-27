package imagej.event;

import org.bushe.swing.event.EventBus;

/**
 * Simple utility class for subscribing to ImageJ events,
 * as well as publishing them.
 *
 * @author Curtis Rueden
 */
public final class Events {

	private Events() {
		// prevent instantiation of utility class
	}

	public static <E extends ImageJEvent> void publish(final E e) {
		EventBus.publish(e.getClass(), e);
	}

	public static <E extends ImageJEvent> void subscribe(
		final Class<E> c, final EventSubscriber<E> subscriber)
	{
		EventBus.subscribe(c, subscriber);
	}

}
