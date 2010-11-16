package ijx;

import java.util.Properties;

/**
 * Interface for classes that have preferences to be saved upon application exit.
 *
 * Annotate a class that implements SavesPrefs with
 * @ServiceProvider(service=SavesPrefs.class)
 *
 * @author GBH
 */
public interface SavesPrefs {
	void savePreferences(Properties prefs);
}
 