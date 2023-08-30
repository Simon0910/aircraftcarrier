package com.aircraftcarrier.framework.tookit;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * 封装LogUtils实现延迟加载与日志等级判断
 * <a href="https://dioxide-cn.ink/archives/why-package-logger">...</a>
 *
 * @author Dioxide.CN
 * @date 2023/5/24
 */
public class LoggerUtil {

    private LoggerUtil() {
    }

    public static void info(Logger logger, String message, Supplier<?>... suppliers) {
        log(Level.INFO, logger, message, suppliers);
    }

    public static void debug(Logger logger, String message, Supplier<?>... suppliers) {
        log(Level.DEBUG, logger, message, suppliers);
    }

    public static void warn(Logger logger, String message, Supplier<?>... suppliers) {
        log(Level.WARN, logger, message, suppliers);
    }

    public static void error(Logger logger, String message, Supplier<?>... suppliers) {
        log(Level.ERROR, logger, message, suppliers);
    }

    public static void trace(Logger logger, String message, Supplier<?>... suppliers) {
        log(Level.TRACE, logger, message, suppliers);
    }

    private static void log(Level level, Logger logger, String message, Supplier<?>... suppliers) {
        // 判断日志级别是否被启用
        switch (level) {
            case DEBUG -> {
                if (logger.isDebugEnabled()) {
                    logger.debug(formatLogMessage(getCallerStackTrace(), message), getLogArgs(suppliers));
                }
            }
            case INFO -> {
                if (logger.isInfoEnabled()) {
                    logger.info(formatLogMessage(getCallerStackTrace(), message), getLogArgs(suppliers));
                }
            }
            case ERROR -> {
                if (logger.isErrorEnabled()) {
                    logger.error(formatLogMessage(getCallerStackTrace(), message), getLogArgs(suppliers));
                }
            }
            case WARN -> {
                if (logger.isWarnEnabled()) {
                    logger.warn(formatLogMessage(getCallerStackTrace(), message), getLogArgs(suppliers));
                }
            }
            case TRACE -> {
                if (logger.isTraceEnabled()) {
                    logger.trace(message, getLogArgs(suppliers));
                }
            }
            default -> throw new IllegalArgumentException("Unexpected Level: " + level);
        }
    }

    private static Object[] getLogArgs(Supplier<?>... suppliers) {
        if (suppliers == null || suppliers.length == 0) {
            return new Object[]{};
        } else {
            return Arrays.stream(suppliers).map(Supplier::get).toArray();
        }
    }

    private static StackTraceElement getCallerStackTrace() {
        return Thread.currentThread().getStackTrace()[4];
    }

    private static String formatLogMessage(StackTraceElement caller, String message) {
        return caller.getMethodName() +
                " (" + caller.getFileName() + ":" + caller.getLineNumber() + ") " +
                LogUtil.getFullTid() +
                message;
    }

}