package imagej;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

	private static Logger logger =
		LoggerFactory.getLogger(Log.class);

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		Log.logger = logger;
	}

	public static void debug(String msg) {
		logger.debug(msg);
	}

	public static void error(String msg) {
		logger.error(msg);
	}

	public static void info(String msg) {
		logger.info(msg);
	}

	public static void warn(String msg) {
		logger.warn(msg);
	}

}
