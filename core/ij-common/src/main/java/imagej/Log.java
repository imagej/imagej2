package imagej;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public final class Log {

	private Log() {
		// prevent instantiation of utility class
	}

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

	public static void debug(Throwable t) {
		debug("Exception", t);
	}

	public static void debug(String msg, Throwable t) {
		logger.debug(msg, t);
	}

	public static void error(String msg) {
		logger.error(msg);
	}

	public static void error(Throwable t) {
		error("Exception", t);
	}

	public static void error(String msg, Throwable t) {
		logger.error(msg, t);
	}

	public static void info(String msg) {
		logger.info(msg);
	}

	public static void info(Throwable t) {
		info("Exception", t);
	}

	public static void info(String msg, Throwable t) {
		logger.info(msg, t);
	}
	
	public static void trace(String msg) {
		logger.trace(msg);
	}

	public static void trace(Throwable t) {
		trace("Exception", t);
	}

	public static void trace(String msg, Throwable t) {
		logger.trace(msg, t);
	}

	public static void warn(String msg) {
		logger.warn(msg);
	}

	public static void warn(Throwable t) {
		warn("Exception", t);
	}

	public static void warn(String msg, Throwable t) {
		logger.warn(msg, t);
	}

	public static boolean isDebug() {
		return logger.isDebugEnabled();
	}

	public static boolean isError() {
		return logger.isErrorEnabled();
	}

	public static boolean isInfo() {
		return logger.isInfoEnabled();
	}

	public static boolean isTrace() {
		return logger.isTraceEnabled();
	}

	public static boolean isWarn() {
		return logger.isWarnEnabled();
	}

}
