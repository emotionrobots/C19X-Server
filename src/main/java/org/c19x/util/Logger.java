package org.c19x.util;

import org.slf4j.LoggerFactory;

public class Logger {
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(Logger.class);
	private final static short DEBUG = 0;
	private final static short INFO = 1;
	private final static short WARN = 2;
	private final static short ERROR = 3;
	private static short level = DEBUG;

	public final static void debug(final String tag, final String message, final Object... values) {
		if (level <= DEBUG) {
			output(DEBUG, tag, message, values);
		}
	}

	public final static void info(final String tag, final String message, final Object... values) {
		if (level <= INFO) {
			output(INFO, tag, message, values);
		}
	}

	public final static void warn(final String tag, final String message, final Object... values) {
		if (level <= WARN) {
			output(WARN, tag, message, values);
		}
	}

	public final static void error(final String tag, final String message, final Object... values) {
		if (level <= ERROR) {
			output(ERROR, tag, message, values);
		}
	}

	private final static void output(final int level, final String tag, final String message, final Object... values) {
		final Throwable throwable = getThrowable(values);
		switch (level) {
		case DEBUG: {
			if (throwable == null) {
				logger.debug("{}: {}", tag, render(message, values));
			} else {
				logger.debug("{}: {}", tag, render(message, values), throwable);
			}
			break;
		}
		case INFO: {
			if (throwable == null) {
				logger.info("{}: {}", tag, render(message, values));
			} else {
				logger.info("{}: {}", tag, render(message, values), throwable);
			}
			break;
		}
		case WARN: {
			if (throwable == null) {
				logger.warn("{}: {}", tag, render(message, values));
			} else {
				logger.warn("{}: {}", tag, render(message, values), throwable);
			}
			break;
		}
		case ERROR: {
			if (throwable == null) {
				logger.error("{}: {}", tag, render(message, values));
			} else {
				logger.error("{}: {}", tag, render(message, values), throwable);
			}
			break;
		}
		}
	}

	private final static Throwable getThrowable(final Object... values) {
		if (values.length > 0 && values[values.length - 1] instanceof Throwable) {
			return (Throwable) values[values.length - 1];
		} else {
			return null;
		}
	}

	private final static String render(final String message, final Object... values) {
		if (values.length == 0) {
			return message;
		} else {
			final StringBuilder stringBuilder = new StringBuilder();

			int valueIndex = 0;
			int start = 0;
			int end = message.indexOf("{}");
			while (end > 0) {
				stringBuilder.append(message.substring(start, end));
				if (values.length > valueIndex) {
					if (values[valueIndex] == null) {
						stringBuilder.append("NULL");
					} else {
						stringBuilder.append(values[valueIndex].toString());
					}
				}
				valueIndex++;
				start = end + 2;
				end = message.indexOf("{}", start);
			}
			stringBuilder.append(message.substring(start));

			return stringBuilder.toString();
		}
	}
}
